package com.example.melodink.domain.job.service;

import com.example.melodink.domain.artist.entity.ArtistProfile;
import com.example.melodink.domain.artist.repository.ArtistProfileRepository;
import com.example.melodink.domain.job.dto.request.*;
import com.example.melodink.domain.job.dto.response.*;
import com.example.melodink.domain.job.entity.JobApplication;
import com.example.melodink.domain.job.entity.JobPosting;
import com.example.melodink.domain.job.entity.JobStatus;
import com.example.melodink.domain.job.exception.JobException;
import com.example.melodink.domain.job.repository.JobApplicationRepository;
import com.example.melodink.domain.job.repository.JobPostingRepository;
import com.example.melodink.domain.notification.event.ApplyResultEvent;
import com.example.melodink.domain.notification.event.JobApplyEvent;
import com.example.melodink.domain.user.entity.User;
import com.example.melodink.domain.user.repository.UserRepository;
import com.example.melodink.global.common.RoleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobService {

    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ArtistProfileRepository artistProfileRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RoleValidator roleValidator;

    // ══════════════════════════════════════════════════════════
    // 채용 공고 CRUD
    // ══════════════════════════════════════════════════════════

    /**
     * 채용 공고 등록
     * DIRECTOR 역할만 가능
     */
    @Transactional
    public JobPostingResponse createPosting(Long userId, JobPostingCreateRequest request) {
        User director = findUser(userId);
        roleValidator.validateActive(director);
        roleValidator.validateDirector(director);

        JobPosting posting = JobPosting.builder()
                .director(director)
                .title(request.title())
                .description(request.description())
                .jobType(request.jobType())
                .location(request.location())
                .budgetMin(request.budgetMin())
                .budgetMax(request.budgetMax())
                .deadlineAt(request.deadlineAt())
                .build();

        return JobPostingResponse.from(jobPostingRepository.save(posting));
    }

    /**
     * 채용 공고 검색 (QueryDSL 동적 쿼리)
     * 비인증 허용
     */
    public Page<JobPostingSummaryResponse> searchPostings(JobSearchRequest condition) {
        PageRequest pageable = PageRequest.of(
                condition.page(),
                condition.size(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return jobPostingRepository
                .searchPostings(condition, pageable)
                .map(JobPostingSummaryResponse::from);
    }

    /**
     * 채용 공고 상세 조회
     * 비인증 허용
     */
    public JobPostingResponse getPosting(UUID publicId) {
        return JobPostingResponse.from(findPostingWithDirector(publicId));
    }

    /**
     * 채용 공고 수정
     * DIRECTOR + 본인 공고만 가능
     */
    @Transactional
    public JobPostingResponse updatePosting(Long userId, UUID publicId,
                                            JobPostingUpdateRequest request) {
        User director = findUser(userId);
        roleValidator.validateActive(director);
        roleValidator.validateDirector(director);

        JobPosting posting = findPostingByOwner(userId, publicId);

        if (!posting.isOpen()) {
            throw new JobException("마감된 공고는 수정할 수 없습니다.");
        }

        posting.update(
                request.title(),
                request.description(),
                request.location(),
                request.budgetMin(),
                request.budgetMax(),
                request.deadlineAt()
        );

        return JobPostingResponse.from(posting);
    }

    /**
     * 채용 공고 마감
     * DIRECTOR + 본인 공고만 가능
     */
    @Transactional
    public void closePosting(Long userId, UUID publicId) {
        User director = findUser(userId);
        roleValidator.validateActive(director);
        roleValidator.validateDirector(director);

        findPostingByOwner(userId, publicId).close();
    }

    /**
     * 채용 공고 삭제
     * DIRECTOR + 본인 공고 + 지원자 없는 경우만 가능
     */
    @Transactional
    public void deletePosting(Long userId, UUID publicId) {
        User director = findUser(userId);
        roleValidator.validateActive(director);
        roleValidator.validateDirector(director);

        JobPosting posting = findPostingByOwner(userId, publicId);

        if (!posting.getApplications().isEmpty()) {
            throw new JobException("지원자가 있는 공고는 삭제할 수 없습니다.");
        }

        jobPostingRepository.delete(posting);
    }

    // ══════════════════════════════════════════════════════════
    // 지원 관련
    // ══════════════════════════════════════════════════════════

    /**
     * 채용 공고 지원
     * ARTIST 역할만 가능
     * - 본인 공고 지원 방지
     * - 마감 공고 지원 방지
     * - 중복 지원 방지
     */
    @Transactional
    public JobApplicationResponse apply(Long userId, UUID jobPublicId,
                                        JobApplicationRequest request) {
        User user = findUser(userId);
        roleValidator.validateActive(user);
        roleValidator.validateArtist(user);

        JobPosting posting = findPostingWithDirector(jobPublicId);
        ArtistProfile artist = findArtistProfile(userId);

        if (posting.getDirector().getId().equals(userId)) {
            throw new JobException("본인이 등록한 공고에는 지원할 수 없습니다.");
        }
        if (!posting.isOpen()) {
            throw new JobException("마감된 공고에는 지원할 수 없습니다.");
        }
        if (jobApplicationRepository.existsByJobPostingIdAndArtistId(
                posting.getId(), artist.getId())) {
            throw new JobException("이미 지원한 공고입니다.");
        }

        JobApplication application = JobApplication.builder()
                .jobPosting(posting)
                .artist(artist)
                .coverLetter(request.coverLetter())
                .build();

        jobApplicationRepository.save(application);

        eventPublisher.publishEvent(new JobApplyEvent(
                posting.getDirector().getId(),
                userId,
                user.getNickname(),
                posting.getId()
        ));

        return JobApplicationResponse.from(application);
    }

    /**
     * 지원 취소
     * ARTIST 역할 + PENDING 상태만 가능
     */
    @Transactional
    public void cancelApplication(Long userId, UUID jobPublicId) {
        User user = findUser(userId);
        roleValidator.validateActive(user);
        roleValidator.validateArtist(user);

        JobApplication application = jobApplicationRepository
                .findByJobPublicIdAndUserId(jobPublicId, userId)
                .orElseThrow(() -> new JobException("지원 내역을 찾을 수 없습니다."));

        if (application.getStatus() != JobStatus.PENDING) {
            throw new JobException("검토가 시작된 지원은 취소할 수 없습니다.");
        }

        jobApplicationRepository.delete(application);
    }

    /**
     * 공고의 지원자 목록 조회
     * DIRECTOR + 본인 공고만 가능
     */
    public List<JobApplicationResponse> getApplications(Long userId, UUID jobPublicId) {
        User director = findUser(userId);
        roleValidator.validateDirector(director);

        JobPosting posting = findPostingByOwner(userId, jobPublicId);

        return jobApplicationRepository
                .findByJobPostingIdWithArtist(posting.getId())
                .stream()
                .map(JobApplicationResponse::from)
                .toList();
    }

    /**
     * 본인 지원 내역 조회
     * ARTIST 역할만 가능
     */
    public List<MyApplicationResponse> getMyApplications(Long userId) {
        User user = findUser(userId);
        roleValidator.validateArtist(user);

        ArtistProfile artist = findArtistProfile(userId);

        return jobApplicationRepository
                .findByArtistIdWithJobPosting(artist.getId())
                .stream()
                .map(MyApplicationResponse::from)
                .toList();
    }

    /**
     * 지원 상태 변경
     * DIRECTOR 역할 + 본인 공고의 지원 건만 가능
     * 합격·불합격 처리 시 아티스트에게 알림 발행
     */
    @Transactional
    public JobApplicationResponse updateApplicationStatus(Long userId, Long applicationId,
                                                          JobStatusUpdateRequest request) {
        User director = findUser(userId);
        roleValidator.validateActive(director);
        roleValidator.validateDirector(director);

        JobApplication application = jobApplicationRepository
                .findByIdAndDirectorId(applicationId, userId)
                .orElseThrow(() -> new JobException("지원 건을 찾을 수 없거나 권한이 없습니다."));

        switch (request.status()) {
            case REVIEWED -> application.review();
            case ACCEPTED -> {
                application.accept();
                publishApplyResultEvent(application, true);
            }
            case REJECTED -> {
                application.reject();
                publishApplyResultEvent(application, false);
            }
            default -> throw new JobException("유효하지 않은 상태 변경입니다.");
        }

        return JobApplicationResponse.from(application);
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new JobException("유저를 찾을 수 없습니다."));
    }

    private ArtistProfile findArtistProfile(Long userId) {
        return artistProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new JobException("아티스트 프로필이 없습니다. 먼저 아티스트 등록을 해주세요."));
    }

    private JobPosting findPostingWithDirector(UUID publicId) {
        return jobPostingRepository.findByPublicIdWithDirector(publicId)
                .orElseThrow(() -> new JobException("채용 공고를 찾을 수 없습니다."));
    }

    private JobPosting findPostingByOwner(Long userId, UUID publicId) {
        return jobPostingRepository.findByPublicIdAndDirectorId(publicId, userId)
                .orElseThrow(() -> new JobException("채용 공고를 찾을 수 없거나 수정 권한이 없습니다."));
    }

    private void publishApplyResultEvent(JobApplication application, boolean accepted) {
        eventPublisher.publishEvent(new ApplyResultEvent(
                application.getArtist().getUser().getId(),
                application.getJobPosting().getDirector().getId(),
                accepted,
                application.getJobPosting().getId()
        ));
    }
}