package com.example.melodink.domain.community.service;

import com.example.melodink.domain.community.dto.request.CommentCreateRequest;
import com.example.melodink.domain.community.dto.request.CommentUpdateRequest;
import com.example.melodink.domain.community.dto.response.CommentResponse;
import com.example.melodink.domain.community.entity.Comment;
import com.example.melodink.domain.community.entity.Post;
import com.example.melodink.domain.community.exception.NotFoundPostException;
import com.example.melodink.domain.community.repository.CommentRepository;
import com.example.melodink.domain.community.repository.PostRepository;
import com.example.melodink.domain.notification.event.CommentReplyEvent;
import com.example.melodink.domain.notification.event.PostCommentEvent;
import com.example.melodink.domain.user.entity.User;
import com.example.melodink.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Transactional
@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CommentResponse createComment(Long userId, UUID postPublicId,
                                         CommentCreateRequest request) {
        User commenter = findUser(userId);
        Post post = findPost(postPublicId);

        Comment parent = null;

        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new PostService.CommunityException("부모 댓글을 찾을 수 없습니다."));

            if (parent.isReply()) {
                throw new PostService.CommunityException("대댓글에는 댓글을 달 수 없습니다.");
            }
            if (!parent.getPost().getId().equals(post.getId())) {
                throw new PostService.CommunityException("잘못된 요청입니다.");
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .user(commenter)
                .parent(parent)
                .content(request.content())
                .build();

        commentRepository.save(comment);
        postRepository.increaseCommentCount(post.getId());

        // ── 알림 이벤트 발행 ──────────────────────────────────
        if (parent == null) {
            // 게시글 작성자에게 댓글 알림
            eventPublisher.publishEvent(new PostCommentEvent(
                    post.getPublicId(),     // 게시글 작성자 (수신)
                    post.getUser().getPublicId(),                     // 댓글 작성자 (발신)
                    commenter.getPublicId(),
                    commenter.getNickname()
            ));
        } else {
            // 원댓글 작성자에게 대댓글 알림
            eventPublisher.publishEvent(new CommentReplyEvent(
                    post.getPublicId(),   // 원댓글 작성자 (수신)
                    parent.getUser().getPublicId(),                     // 대댓글 작성자 (발신)
                    commenter.getPublicId(),
                    commenter.getNickname()
            ));
        }

        return comment.isReply()
                ? CommentResponse.fromReply(comment)
                : CommentResponse.from(comment);
    }

    public List<CommentResponse> getComments(UUID postPublicId) {
        Post post = findPost(postPublicId);
        return commentRepository
                .findTopLevelCommentsWithReplies(post.getId())
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse updateComment(Long userId, Long commentId,
                                         CommentUpdateRequest request) {
        Comment comment = findCommentByOwner(userId, commentId);
        comment.updateContent(request.content());
        return comment.isReply()
                ? CommentResponse.fromReply(comment)
                : CommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = findCommentByOwner(userId, commentId);
        Post post = comment.getPost();

        int decreaseCount = 1 + comment.getReplies().size();
        for (int i = 0; i < decreaseCount; i++) {
            postRepository.decreaseCommentCount(post.getId());
        }

        commentRepository.delete(comment);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new PostService.CommunityException("유저를 찾을 수 없습니다."));
    }

    private Post findPost(UUID publicId) {
        return postRepository.findByPublicId(publicId)
                .orElseThrow(NotFoundPostException::new);
    }

    private Comment findCommentByOwner(Long userId, Long commentId) {
        return commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new PostService.CommunityException("댓글을 찾을 수 없거나 수정 권한이 없습니다."));
    }
}
