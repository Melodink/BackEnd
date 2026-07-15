package com.example.melodink.domain.job.dto.request;

import jakarta.validation.constraints.Size;

// ── 채용 공고 지원 ────────────────────────────────────────
public record JobApplicationRequest(

        @Size(max = 3000, message = "자기소개서는 3000자 이하로 입력해주세요.")
        String coverLetter
) {}