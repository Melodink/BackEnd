package com.example.melodink.domain.community.dto.response;

import com.example.melodink.domain.community.entity.Post;
import com.example.melodink.domain.community.entity.Post.PostCategory;

import java.time.LocalDateTime;
import java.util.UUID;

// ── 게시글 목록 카드 응답 ─────────────────────────────────
public record PostSummaryResponse(
        UUID publicId,
        String authorNickname,
        String authorProfileImageUrl,
        String contentPreview,     // 내용 앞 100자만
        PostCategory category,
        int likeCount,
        int commentCount,
        LocalDateTime createdAt
) {
    public static PostSummaryResponse from(Post p) {
        String content = p.getContent();
        String preview = content.length() > 100
                ? content.substring(0, 100) + "..."
                : content;

        return new PostSummaryResponse(
                p.getPublicId(),
                p.getUser().getNickname(),
                p.getUser().getPictureUrl(),
                preview,
                p.getCategory(),
                p.getLikeCount(),
                p.getCommentCount(),
                p.getCreatedAt()
        );
    }
}