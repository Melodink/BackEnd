package com.example.melodink.domain.community.dto.response;

import com.example.melodink.domain.community.entity.Post;
import com.example.melodink.domain.community.entity.Post.PostCategory;

import java.time.LocalDateTime;
import java.util.UUID;

// ── 게시글 상세 응답 ──────────────────────────────────────
public record PostResponse(
        UUID publicId,
        String authorNickname,
        String authorProfileImageUrl,
        String content,
        PostCategory category,
        int likeCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse from(Post p) {
        return new PostResponse(
                p.getPublicId(),
                p.getUser().getNickname(),
                p.getUser().getPictureUrl(),
                p.getContent(),
                p.getCategory(),
                p.getLikeCount(),
                p.getCommentCount(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}