package com.example.melodink.domain.job.dto.response;

import com.example.melodink.domain.job.entity.JobApplication;
import com.example.melodink.domain.job.entity.JobPosting.JobType;
import com.example.melodink.domain.job.entity.JobStatus;

import java.time.LocalDateTime;
import java.util.UUID;

// ── 본인 지원 내역 응답 (아티스트용) ─────────────────────
public record MyApplicationResponse(
        Long id,
        UUID jobPublicId,
        String jobTitle,
        String directorNickname,
        JobType jobType,
        String location,
        JobStatus status,
        LocalDateTime appliedAt
) {
    public static MyApplicationResponse from(JobApplication app) {
        return new MyApplicationResponse(
                app.getId(),
                app.getJobPosting().getPublicId(),
                app.getJobPosting().getTitle(),
                app.getJobPosting().getDirector().getNickname(),
                app.getJobPosting().getJobType(),
                app.getJobPosting().getLocation(),
                app.getStatus(),
                app.getAppliedAt()
        );
    }
}