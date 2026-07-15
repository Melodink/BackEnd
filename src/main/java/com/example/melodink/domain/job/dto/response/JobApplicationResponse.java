package com.example.melodink.domain.job.dto.response;

import com.example.melodink.domain.job.entity.JobApplication;
import com.example.melodink.domain.job.entity.JobStatus;

import java.time.LocalDateTime;
import java.util.UUID;

// ── 지원 상세 응답 (디렉터용) ─────────────────────────────
public record JobApplicationResponse(
        Long id,
        UUID artistPublicId,
        String artistNickname,
        String artistProfileImageUrl,
        String coverLetter,
        JobStatus status,
        LocalDateTime appliedAt
) {
    public static JobApplicationResponse from(JobApplication app) {
        return new JobApplicationResponse(
                app.getId(),
                app.getArtist().getPublicId(),
                app.getArtist().getUser().getNickname(),
                app.getArtist().getUser().getPictureUrl(),
                app.getCoverLetter(),
                app.getStatus(),
                app.getAppliedAt()
        );
    }
}