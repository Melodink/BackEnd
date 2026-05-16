package com.example.melodink.domain.community.controller;

import com.example.melodink.domain.community.dto.request.PostCreateRequest;
import com.example.melodink.domain.community.dto.request.PostSearchRequest;
import com.example.melodink.domain.community.dto.request.PostUpdateRequest;
import com.example.melodink.domain.community.dto.response.PostResponse;
import com.example.melodink.domain.community.dto.response.PostSummaryResponse;
import com.example.melodink.domain.community.service.PostService;
import com.example.melodink.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * POST /api/v1/posts
     * 게시글 작성
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PostCreateRequest request
    ) {
        PostResponse response = postService.createPost(userDetails.getId(), request);
        return ResponseEntity
                .created(URI.create("/api/v1/posts/" + response.publicId()))
                .body(response);
    }

    /**
     * GET /api/v1/posts?keyword=&category=&page=0&size=20
     * 게시글 목록 조회 / 검색 (비인증 허용)
     */
    @GetMapping
    public ResponseEntity<Page<PostSummaryResponse>> searchPosts(
            @ModelAttribute PostSearchRequest condition
    ) {
        return ResponseEntity.ok(postService.searchPosts(condition));
    }

    /**
     * GET /api/v1/posts/{publicId}
     * 게시글 상세 조회 (비인증 허용)
     */
    @GetMapping("/{publicId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable UUID publicId
    ) {
        return ResponseEntity.ok(postService.getPost(publicId));
    }

    /**
     * PUT /api/v1/posts/{publicId}
     * 게시글 수정 (본인만)
     */
    @PutMapping("/{publicId}")
    public ResponseEntity<PostResponse> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID publicId,
            @RequestBody @Valid PostUpdateRequest request
    ) {
        return ResponseEntity.ok(
                postService.updatePost(userDetails.getId(), publicId, request)
        );
    }

    /**
     * DELETE /api/v1/posts/{publicId}
     * 게시글 삭제 (본인만)
     */
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID publicId
    ) {
        postService.deletePost(userDetails.getId(), publicId);
        return ResponseEntity.noContent().build();
    }
}