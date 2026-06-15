package com.example.melodink.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(max = 1000)
        String content
) {
}
