package com.example.melodink.domain.user.service;

import com.example.melodink.domain.user.dto.response.FollowUserResponse;
import com.example.melodink.domain.user.entity.Follow;
import com.example.melodink.domain.user.entity.User;
import com.example.melodink.domain.user.repository.FollowRepository;
import com.example.melodink.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public void follow(Long followerId, UUID followingId) {

        User follower = userRepository.getReferenceById(followerId);
        User following = userRepository.findByPublicId(followingId);

        if (follower.getId().equals(following.getId())) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        boolean exists = followRepository.existsByFollowerAndFollowing(follower, following);

        if(exists) {
            throw new IllegalArgumentException("Already exists follower");
        }

        followRepository.save(
                Follow.builder()
                        .follower(follower)
                        .following(following)
                        .build()
        );
    }

    public void unfollow(Long followerId, UUID followingPublicId) {
        followRepository.deleteByFollowerIdAndPublicId(followerId, followingPublicId);
    }

    // 쿼리문 두 번을 사용하지만 join을 피하는 방법 채택
    public Long getFollowerCount(UUID userPublicId) {
        User user = userRepository.findByPublicId(userPublicId);

        long followerCount = followRepository.countFollowers(user.getId());

        return followerCount;
    }

    public Long getFollowingCount(UUID userPublicId) {
        User user = userRepository.findByPublicId(userPublicId);
        long followingCount = followRepository.countFollowing(user.getId());
        return followingCount;
    }

    public List<FollowUserResponse> getFollowers(UUID userPublicId) {
        List<FollowUserResponse> followerList = followRepository.findFollowerId(userPublicId);

        return followerList;
    }

    public List<FollowUserResponse> getFollowings(UUID userPublicId) {
        List<FollowUserResponse> followingList = followRepository.findFollowingId(userPublicId);

        return followingList;
    }

}
