package com.example.melodink.domain.community.controller;

import com.example.melodink.domain.community.dto.request.CommentCreateRequest;
import com.example.melodink.domain.community.dto.request.CommentUpdateRequest;
import com.example.melodink.domain.community.dto.response.CommentResponse;
import com.example.melodink.domain.community.service.CommentService;
import com.example.melodink.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * POST /api/v1/posts/{postPublicId}/comments
     * 댓글/대댓글 작성
     * request.parentId == null → 최상위 댓글
     * request.parentId != null → 대댓글 (2depth 제한)
     */
    @PostMapping("/api/v1/posts/{postPublicId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID postPublicId,
            @RequestBody @Valid CommentCreateRequest request
    ) {
        CommentResponse response = commentService.createComment(
                userDetails.getId(), postPublicId, request
        );
        return ResponseEntity
                .created(URI.create("/api/v1/posts/" + postPublicId + "/comments"))
                .body(response);
    }

    /**
     * GET /api/v1/posts/{postPublicId}/comments
     * 게시글 댓글 전체 조회 (대댓글 포함, 비인증 허용)
     */
    @GetMapping("/api/v1/posts/{postPublicId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable UUID postPublicId
    ) {
        return ResponseEntity.ok(commentService.getComments(postPublicId));
    }

    /**
     * PUT /api/v1/comments/{commentId}
     * 댓글 수정 (본인만)
     * commentId는 내부 Long PK 사용 (댓글은 public_id 미부여)
     */
    @PutMapping("/api/v1/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequest request
    ) {
        return ResponseEntity.ok(
                commentService.updateComment(userDetails.getId(), commentId, request)
        );
    }

    /**
     * DELETE /api/v1/comments/{commentId}
     * 댓글 삭제 (본인만, 대댓글 포함 삭제)
     */
    @DeleteMapping("/api/v1/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(userDetails.getId(), commentId);
        return ResponseEntity.noContent().build();
    }
}