package com.example.melodink.domain.job.dto.request;

import com.example.melodink.domain.job.entity.JobPosting.JobType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

// ── 채용 공고 등록 ────────────────────────────────────────
public record JobPostingCreateRequest(

        @NotBlank(message = "공고 제목을 입력해주세요.")
        @Size(max = 100)
        String title,

        @NotBlank(message = "공고 내용을 입력해주세요.")
        String description,

        @NotNull(message = "직종을 선택해주세요.")
        JobType jobType,

        @Size(max = 100)
        String location,

        @Min(value = 0, message = "예산은 0원 이상이어야 합니다.")
        Integer budgetMin,

        @Min(value = 0, message = "예산은 0원 이상이어야 합니다.")
        Integer budgetMax,

        LocalDateTime deadlineAt
) {}