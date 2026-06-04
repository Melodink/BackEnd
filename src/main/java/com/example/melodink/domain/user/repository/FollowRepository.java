package com.example.melodink.domain.user.repository;

import com.example.melodink.domain.user.dto.response.FollowUserResponse;
import com.example.melodink.domain.user.entity.Follow;
import com.example.melodink.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    @Query("""
    select f.follower
         from Follow f
             where f.following.id= :followerId

    """)
    List<FollowUserResponse> findFollowerId(UUID followerId);

    @Query("""
    select f.following
         from Follow f
             where f.follower.id= :followingId

    """)
    List<FollowUserResponse> findFollowingId(UUID followingId);

    long countFollowers(Long followerId);

    long countFollowing(Long followingId);

    boolean existsByFollowerAndFollowing(User follower, User following);

    void deleteByFollowerIdAndPublicId(Long followerId, UUID followingId);
}
