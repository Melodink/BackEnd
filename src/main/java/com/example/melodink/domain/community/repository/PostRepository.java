package com.example.melodink.domain.community.repository;

import com.example.melodink.domain.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    Optional<Post> findByPublicId(UUID publicId);

    // 게시글 + 작성자 fetch join (N+1 방지)
    @Query("""
        SELECT p FROM Post p
        JOIN FETCH p.user
        WHERE p.publicId = :publicId
        """)
    Optional<Post> findByPublicIdWithUser(@Param("publicId") UUID publicId);

    // 본인 게시글 소유 확인
    @Query("""
        SELECT p FROM Post p
        WHERE p.publicId = :publicId
          AND p.user.id = :userId
        """)
    Optional<Post> findByPublicIdAndUserId(
            @Param("publicId") UUID publicId,
            @Param("userId") Long userId
    );

    // 좋아요 수 직접 증가 (더티체킹 없이 단건 UPDATE)
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    void increaseLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :id AND p.likeCount > 0")
    void decreaseLikeCount(@Param("id") Long id);

    // 댓글 수 직접 증가
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :id")
    void increaseCommentCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :id AND p.commentCount > 0")
    void decreaseCommentCount(@Param("id") Long id);
}