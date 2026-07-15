package com.example.melodink.domain.job.controller;

import com.example.melodink.domain.job.dto.request.*;
import com.example.melodink.domain.job.dto.response.*;
import com.example.melodink.domain.job.service.JobService;
import com.example.melodink.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    // ══════════════════════════════════════════════════════════
    // 채용 공고 CRUD
    // ══════════════════════════════════════════════════════════

    /**
     * POST /api/v1/jobs
     * 채용 공고 등록 (DIRECTOR 전용)
     */
    @PostMapping
    public ResponseEntity<JobPostingResponse> createPosting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid JobPostingCreateRequest request
    ) {
        JobPostingResponse response = jobService.createPosting(userDetails.getId(), request);
        return ResponseEntity
                .created(URI.create("/api/v1/jobs/" + response.publicId()))
                .body(response);
    }

    /**
     * GET /api/v1/jobs?keyword=&jobType=&location=&budgetMin=&budgetMax=&onlyOpen=&page=&size=
     * 채용 공고 검색 (비인증 허용)
     */
    @GetMapping
    public ResponseEntity<Page<JobPostingSummaryResponse>> searchPostings(
            @ModelAttribute JobSearchRequest condition
    ) {
        return ResponseEntity.ok(jobService.searchPostings(condition));
    }

    /**
     * GET /api/v1/jobs/{publicId}
     * 채용 공고 상세 조회 (비인증 허용)
     */
    @GetMapping("/{publicId}")
    public ResponseEntity<JobPostingResponse> getPosting(
            @PathVariable UUID publicId
    ) {
        return ResponseEntity.ok(jobService.getPosting(publicId));
    }

    /**
     * PUT /api/v1/jobs/{publicId}
     * 채용 공고 수정 (본인 공고만)
     */
    @PutMapping("/{publicId}")
    public ResponseEntity<JobPostingResponse> updatePosting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID publicId,
            @RequestBody @Valid JobPostingUpdateRequest request
    ) {
        return ResponseEntity.ok(
                jobService.updatePosting(userDetails.getId(), publicId, request)
        );
    }

    /**
     * PATCH /api/v1/jobs/{publicId}/close
     * 채용 공고 마감 처리 (본인 공고만)
     */
    @PatchMapping("/{publicId}/close")
    public ResponseEntity<Void> closePosting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID publicId
    ) {
        jobService.closePosting(userDetails.getId(), publicId);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/v1/jobs/{publicId}
     * 채용 공고 삭제 (지원자 없는 공고만)
     */
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deletePosting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID publicId
    ) {
        jobService.deletePosting(userDetails.getId(), publicId);
        return ResponseEntity.noContent().build();
    }

    // ══════════════════════════════════════════════════════════
    // 지원 관련
    // ══════════════════════════════════════════════════════════

    /**
     * POST /api/v1/jobs/{publicId}/apply
     * 채용 공고 지원 (ARTIST 전용)
     */
    @PostMapping("/{publicId}/apply")
    public ResponseEntity<JobApplicationResponse> apply(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID publicId,
            @RequestBody @Valid JobApplicationRequest request
    ) {
        return ResponseEntity.ok(
                jobService.apply(userDetails.getId(), publicId, request)
        );
    }

    /**
     * DELETE /api/v1/jobs/{publicId}/apply
     * 지원 취소 (PENDING 상태만)
     */
    @DeleteMapping("/{publicId}/apply")
    public ResponseEntity<Void> cancelApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID publicId
    ) {
        jobService.cancelApplication(userDetails.getId(), publicId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/jobs/{publicId}/applications
     * 지원자 목록 조회 (디렉터 본인 공고만)
     */
    @GetMapping("/{publicId}/applications")
    public ResponseEntity<List<JobApplicationResponse>> getApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID publicId
    ) {
        return ResponseEntity.ok(
                jobService.getApplications(userDetails.getId(), publicId)
        );
    }

    /**
     * GET /api/v1/jobs/my-applications
     * 본인 지원 내역 조회 (ARTIST 전용)
     */
    @GetMapping("/my-applications")
    public ResponseEntity<List<MyApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                jobService.getMyApplications(userDetails.getId())
        );
    }

    /**
     * PATCH /api/v1/jobs/applications/{applicationId}/status
     * 지원 상태 변경 (DIRECTOR 전용)
     * PENDING → REVIEWED → ACCEPTED / REJECTED
     */
    @PatchMapping("/applications/{applicationId}/status")
    public ResponseEntity<JobApplicationResponse> updateApplicationStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long applicationId,
            @RequestBody @Valid JobStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(
                jobService.updateApplicationStatus(
                        userDetails.getId(), applicationId, request
                )
        );
    }
}