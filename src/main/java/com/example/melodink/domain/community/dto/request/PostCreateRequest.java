package com.example.melodink.domain.community.dto.request;

import com.example.melodink.domain.community.entity.Post.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// ── 게시글 작성 ───────────────────────────────────────────
public record PostCreateRequest(

        @NotBlank(message = "내용을 입력해주세요.")
        @Size(max = 5000, message = "내용은 5000자 이하로 입력해주세요.")
        String content,

        @NotNull(message = "카테고리를 선택해주세요.")
        PostCategory category
) {}