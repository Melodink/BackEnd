package com.example.melodink.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(

        @NotBlank(message = "내용을 입력해주세요.")
        @Size(max = 5000)
        String content
) {}