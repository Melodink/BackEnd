package com.example.melodink.domain.job.dto.response;

import com.example.melodink.domain.job.entity.JobPosting;
import com.example.melodink.domain.job.entity.JobPosting.JobStatus;
import com.example.melodink.domain.job.entity.JobPosting.JobType;

import java.time.LocalDateTime;
import java.util.UUID;

// ── 채용 공고 목록 카드 응답 ──────────────────────────────
public record JobPostingSummaryResponse(
        UUID publicId,
        String directorNickname,
        String title,
        JobType jobType,
        JobStatus status,
        String location,
        Integer budgetMin,
        Integer budgetMax,
        LocalDateTime deadlineAt,
        LocalDateTime createdAt
) {
    public static JobPostingSummaryResponse from(JobPosting j) {
        return new JobPostingSummaryResponse(
                j.getPublicId(),
                j.getDirector().getNickname(),
                j.getTitle(),
                j.getJobType(),
                j.getStatus(),
                j.getLocation(),
                j.getBudgetMin(),
                j.getBudgetMax(),
                j.getDeadlineAt(),
                j.getCreatedAt()
        );
    }
}