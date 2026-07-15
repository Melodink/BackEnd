package com.example.melodink.domain.job.dto.request;

import com.example.melodink.domain.job.entity.JobStatus;
import jakarta.validation.constraints.NotNull;

// ── 지원 상태 변경 (디렉터용) ─────────────────────────────
public record JobStatusUpdateRequest(

        @NotNull(message = "변경할 상태를 선택해주세요.")
        JobStatus status
) {}