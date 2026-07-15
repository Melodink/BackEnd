package com.example.melodink.domain.job.dto.response;

import com.example.melodink.domain.job.entity.JobPosting;
import com.example.melodink.domain.job.entity.JobPosting.JobStatus;
import com.example.melodink.domain.job.entity.JobPosting.JobType;

import java.time.LocalDateTime;
import java.util.UUID;

// ── 채용 공고 상세 응답 ───────────────────────────────────
public record JobPostingResponse(
        UUID publicId,
        String directorNickname,
        String directorProfileImageUrl,
        String title,
        String description,
        JobType jobType,
        JobStatus status,
        String location,
        Integer budgetMin,
        Integer budgetMax,
        LocalDateTime deadlineAt,
        int applicationCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static JobPostingResponse from(JobPosting j) {
        return new JobPostingResponse(
                j.getPublicId(),
                j.getDirector().getNickname(),
                j.getDirector().getPictureUrl(),
                j.getTitle(),
                j.getDescription(),
                j.getJobType(),
                j.getStatus(),
                j.getLocation(),
                j.getBudgetMin(),
                j.getBudgetMax(),
                j.getDeadlineAt(),
                j.getApplications().size(),
                j.getCreatedAt(),
                j.getUpdatedAt()
        );
    }
}