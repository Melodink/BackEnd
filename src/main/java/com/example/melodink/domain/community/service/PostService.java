package com.example.melodink.domain.community.service;

import com.example.melodink.domain.community.dto.request.PostCreateRequest;
import com.example.melodink.domain.community.dto.request.PostSearchRequest;
import com.example.melodink.domain.community.dto.request.PostUpdateRequest;
import com.example.melodink.domain.community.dto.response.PostResponse;
import com.example.melodink.domain.community.dto.response.PostSummaryResponse;
import com.example.melodink.domain.community.entity.Post;
import com.example.melodink.domain.community.repository.PostRepository;
import com.example.melodink.domain.user.entity.User;
import com.example.melodink.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // ── 게시글 작성 ───────────────────────────────────────────

    @Transactional
    public PostResponse createPost(Long userId, PostCreateRequest request) {
        User user = findUser(userId);

        Post post = Post.builder()
                .user(user)
                .content(request.content())
                .category(request.category())
                .build();

        return PostResponse.from(postRepository.save(post));
    }

    // ── 게시글 목록 조회 (검색) ───────────────────────────────

    public Page<PostSummaryResponse> searchPosts(PostSearchRequest condition) {
        PageRequest pageable = PageRequest.of(condition.page(), condition.size());
        return postRepository.searchPosts(condition, pageable)
                .map(PostSummaryResponse::from);
    }

    // ── 게시글 상세 조회 ──────────────────────────────────────

    public PostResponse getPost(UUID publicId) {
        Post post = postRepository.findByPublicIdWithUser(publicId)
                .orElseThrow(() -> new CommunityException("게시글을 찾을 수 없습니다."));
        return PostResponse.from(post);
    }

    // ── 게시글 수정 ───────────────────────────────────────────

    @Transactional
    public PostResponse updatePost(Long userId, UUID publicId, PostUpdateRequest request) {
        Post post = findPostByOwner(userId, publicId);
        post.updateContent(request.content());
        return PostResponse.from(post);
    }

    // ── 게시글 삭제 ───────────────────────────────────────────

    @Transactional
    public void deletePost(Long userId, UUID publicId) {
        Post post = findPostByOwner(userId, publicId);
        postRepository.delete(post);
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CommunityException("유저를 찾을 수 없습니다."));
    }

    Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CommunityException("게시글을 찾을 수 없습니다."));
    }

    Post findPostByPublicId(UUID publicId) {
        return postRepository.findByPublicId(publicId)
                .orElseThrow(() -> new CommunityException("게시글을 찾을 수 없습니다."));
    }

    private Post findPostByOwner(Long userId, UUID publicId) {
        return postRepository.findByPublicIdAndUserId(publicId, userId)
                .orElseThrow(() -> new CommunityException("게시글을 찾을 수 없거나 수정 권한이 없습니다."));
    }

    public static class CommunityException extends RuntimeException {
        public CommunityException(String message) { super(message); }
    }
}