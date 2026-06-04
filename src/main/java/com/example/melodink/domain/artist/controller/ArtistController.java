package com.example.melodink.domain.artist.controller;

import com.example.melodink.domain.artist.dto.request.*;
import com.example.melodink.domain.artist.dto.response.*;
import com.example.melodink.domain.artist.service.ArtistService;
import com.example.melodink.global.s3.PresignedUploadResponse;
import com.example.melodink.global.s3.S3FileType;
import com.example.melodink.global.s3.S3Service;
import com.example.melodink.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    private final S3Service s3Service;

    // ══════════════════════════════════════════════════════════
    // 아티스트 프로필
    // ══════════════════════════════════════════════════════════

    /**
     * POST /api/v1/artists
     * 아티스트 프로필 최초 등록
     */
    @PostMapping
    public ResponseEntity<ArtistProfileResponse> registerProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ArtistProfileCreateRequest request
    ) {
        ArtistProfileResponse response = artistService.registerProfile(
                userDetails.getId(), request
        );
        return ResponseEntity
                .created(URI.create("/api/v1/artists/" + response.publicId()))
                .body(response);
    }

    /**
     * GET /api/v1/artists?keyword=&skillType=&skillName=&location=&page=&size=
     * 아티스트 검색 (비인증 허용)
     */
    @GetMapping
    public ResponseEntity<Page<ArtistSummaryResponse>> searchArtists(
            @ModelAttribute ArtistSearchRequest condition
    ) {
        return ResponseEntity.ok(artistService.searchArtists(condition));
    }

    /**
     * GET /api/v1/artists/{publicId}
     * 공개 프로필 상세 조회 (비인증 허용)
     */
    @GetMapping("/{publicId}")
    public ResponseEntity<ArtistProfileResponse> getProfile(
            @PathVariable UUID publicId
    ) {
        return ResponseEntity.ok(artistService.getPublicProfile(publicId));
    }

    /**
     * GET /api/v1/artists/me
     * 본인 프로필 조회 (비공개 포함)
     */
    @GetMapping("/me")
    public ResponseEntity<ArtistProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(artistService.getMyProfile(userDetails.getId()));
    }

    /**
     * PUT /api/v1/artists/me
     * 프로필 수정
     */
    @PutMapping("/me")
    public ResponseEntity<ArtistProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ArtistProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(
                artistService.updateProfile(userDetails.getId(), request)
        );
    }

    /**
     * PATCH /api/v1/artists/me/visibility
     * 공개/비공개 토글
     */
    @PatchMapping("/me/visibility")
    public ResponseEntity<Void> changeVisibility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam boolean isPublic
    ) {
        artistService.changeVisibility(userDetails.getId(), isPublic);
        return ResponseEntity.noContent().build();
    }

    // ══════════════════════════════════════════════════════════
    // 프로필 이미지 업로드
    // ══════════════════════════════════════════════════════════

    /**
     * POST /api/v1/artists/me/profile-image
     * 프로필 이미지 업로드 (서버 → S3 직접 전송, 소용량 전용)
     * 반환된 imageUrl을 프로필 등록/수정 시 profileImageUrl 필드에 사용
     */
    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileImageUploadResponse> uploadProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart MultipartFile file
    ) {
        String cdnUrl = s3Service.upload(file, S3FileType.PROFILE_IMAGE);
        return ResponseEntity.ok(new ProfileImageUploadResponse(cdnUrl));
    }

    // ══════════════════════════════════════════════════════════
    // 포트폴리오 작품 — Presigned URL (대용량 파일)
    // ══════════════════════════════════════════════════════════

    /**
     * POST /api/v1/artists/me/works/presigned
     * 작품 파일 업로드용 Presigned URL 발급
     *
     * 대용량 파일(음원·영상) 업로드 흐름:
     * 1. 이 API → presignedUrl + objectKey 수령
     * 2. presignedUrl로 S3에 직접 PUT 업로드
     * 3. objectKey를 작품 등록 API(POST /me/works)에 포함
     */
    @PostMapping("/me/works/presigned")
    public ResponseEntity<PresignedUploadResponse> getWorkPresignedUrl(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam S3FileType fileType,
            @RequestParam String filename,
            @RequestParam String contentType
    ) {
        validateWorkFileType(fileType);
        return ResponseEntity.ok(
                s3Service.generatePresignedUrl(fileType, filename, contentType)
        );
    }

    /**
     * POST /api/v1/artists/me/works/presigned/thumbnail
     * 썸네일 Presigned URL 발급
     */
    @PostMapping("/me/works/presigned/thumbnail")
    public ResponseEntity<PresignedUploadResponse> getThumbnailPresignedUrl(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String filename,
            @RequestParam String contentType
    ) {
        return ResponseEntity.ok(
                s3Service.generatePresignedUrl(S3FileType.THUMBNAIL, filename, contentType)
        );
    }

    // ══════════════════════════════════════════════════════════
    // 포트폴리오 작품 CRUD
    // ══════════════════════════════════════════════════════════

    /**
     * POST /api/v1/artists/me/works
     * 작품 등록 (Presigned 업로드 완료 후 호출)
     * request.mediaObjectKey: Presigned 업로드 완료 후 받은 objectKey
     */
    @PostMapping("/me/works")
    public ResponseEntity<PortfolioWorkResponse> addWork(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PortfolioWorkRequest request
    ) {
        PortfolioWorkResponse response = artistService.addWork(
                userDetails.getId(), request
        );
        return ResponseEntity
                .created(URI.create("/api/v1/artists/works/" + response.publicId()))
                .body(response);
    }

    /**
     * GET /api/v1/artists/{artistPublicId}/works
     * 특정 아티스트 작품 목록 (비인증 허용)
     */
    @GetMapping("/{artistPublicId}/works")
    public ResponseEntity<List<PortfolioWorkSummaryResponse>> getWorks(
            @PathVariable UUID artistPublicId
    ) {
        return ResponseEntity.ok(artistService.getWorks(artistPublicId));
    }

    /**
     * GET /api/v1/artists/works/{workPublicId}
     * 작품 상세 조회 + 조회수 증가 (비인증 허용)
     */
    @GetMapping("/works/{workPublicId}")
    public ResponseEntity<PortfolioWorkResponse> getWork(
            @PathVariable UUID workPublicId
    ) {
        return ResponseEntity.ok(artistService.getWork(workPublicId));
    }

    /**
     * PUT /api/v1/artists/me/works/{workPublicId}
     * 작품 수정
     */
    @PutMapping("/me/works/{workPublicId}")
    public ResponseEntity<PortfolioWorkResponse> updateWork(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID workPublicId,
            @RequestBody @Valid PortfolioWorkRequest request
    ) {
        return ResponseEntity.ok(
                artistService.updateWork(userDetails.getId(), workPublicId, request)
        );
    }

    /**
     * PATCH /api/v1/artists/me/works/{workPublicId}/featured
     * 대표작 토글
     */
    @PatchMapping("/me/works/{workPublicId}/featured")
    public ResponseEntity<Void> toggleFeatured(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID workPublicId
    ) {
        artistService.toggleFeatured(userDetails.getId(), workPublicId);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/v1/artists/me/works/{workPublicId}
     * 작품 삭제 (DB + S3 동시 삭제)
     */
    @DeleteMapping("/me/works/{workPublicId}")
    public ResponseEntity<Void> deleteWork(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID workPublicId
    ) {
        artistService.deleteWork(userDetails.getId(), workPublicId);
        return ResponseEntity.noContent().build();
    }

    // ── 헬퍼 ──────────────────────────────────────────────────

    private void validateWorkFileType(S3FileType fileType) {
        if (fileType == S3FileType.PROFILE_IMAGE) {
            throw new ArtistService.ArtistException(
                    "작품 업로드에 PROFILE_IMAGE 타입은 사용할 수 없습니다."
            );
        }
    }
}