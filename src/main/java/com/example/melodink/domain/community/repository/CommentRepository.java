package com.example.melodink.domain.community.repository;


import com.example.melodink.domain.community.entity.Comment;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    /**
     * 게시글의 최상위 댓글 + 대댓글 한 번에 fetch
     * parent가 null인 댓글만 조회하고 replies는 fetch join
     * → N+1 없이 댓글 트리 구성 가능
     */
    @Query("""
        SELECT DISTINCT c FROM Comment c
        LEFT JOIN FETCH c.replies r
        LEFT JOIN FETCH c.user
        LEFT JOIN FETCH r.user
        WHERE c.post.id = :postId
          AND c.parent IS NULL
        ORDER BY c.createdAt ASC
        """)
    List<Comment> findTopLevelCommentsWithReplies(@Param("postId") Long postId);

    // 본인 댓글 소유 확인
    @Query("""
        SELECT c FROM Comment c
        WHERE c.id = :commentId
          AND c.user.id = :userId
        """)
    Optional<Comment> findByIdAndUserId(
            @Param("commentId") Long commentId,
            @Param("userId") Long userId
    );

    // 게시글의 전체 댓글 수 (대댓글 포함)
    long countByPostId(Long postId);
}
