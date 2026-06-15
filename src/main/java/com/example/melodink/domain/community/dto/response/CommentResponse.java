package com.example.melodink.domain.community.dto.response;

import com.example.melodink.domain.community.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long id,
        String authorNickname,
        String authorProfileImageUrl,
        String content,
        boolean isReply,
        List<CommentResponse> replies,    // 대댓글 목록 (최상위 댓글일 때만)
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 최상위 댓글 변환 (대댓글 포함)
     */
    public static CommentResponse from(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getUser().getNickname(),
                c.getUser().getPictureUrl(),
                c.getContent(),
                c.isReply(),
                c.getReplies().stream()
                        .map(CommentResponse::fromReply)
                        .toList(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    /**
     * 대댓글 변환 (replies는 빈 리스트 — 2depth 제한)
     */
    public static CommentResponse fromReply(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getUser().getNickname(),
                c.getUser().getPictureUrl(),
                c.getContent(),
                true,
                List.of(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
