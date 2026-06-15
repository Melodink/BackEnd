package com.example.melodink.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(max = 1000, message = "댓글은 1000자 이하로 입력해주세요.")
        String content,

        /**
         * 대댓글인 경우 부모 댓글 ID (Long)
         * null이면 최상위 댓글로 처리
         */
        Long parentId
) {
}
