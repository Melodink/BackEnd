package com.example.melodink.domain.job.dto.request;

import com.example.melodink.domain.job.entity.JobPosting.JobType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// ── 채용 공고 수정 ────────────────────────────────────────
public record JobPostingUpdateRequest(

        @NotBlank(message = "공고 제목을 입력해주세요.")
        @Size(max = 100)
        String title,

        @NotBlank(message = "공고 내용을 입력해주세요.")
        String description,

        @NotNull(message = "직종을 선택해주세요.")
        JobType jobType,

        @Size(max = 100)
        String location,

        @Min(0)
        Integer budgetMin,

        @Min(0)
        Integer budgetMax,

        LocalDateTime deadlineAt
) {}