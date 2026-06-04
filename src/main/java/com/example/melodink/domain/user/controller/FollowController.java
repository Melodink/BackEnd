package com.example.melodink.domain.user.controller;

import com.example.melodink.domain.user.dto.response.FollowUserResponse;
import com.example.melodink.domain.user.service.FollowService;
import com.example.melodink.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{targetUserId}")
    public void follow(@AuthenticationPrincipal CustomUserDetails user,
                       @PathVariable UUID targetUserId) {
        followService.follow(user.getId(), targetUserId);
    }

    @DeleteMapping("/{targetUserId}")
    public void unfollow(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID targetUserId) {
        followService.unfollow(user.getId(), targetUserId);
    }

    @GetMapping("/follower/{userId}")
    public List<FollowUserResponse> getFollowers(@PathVariable UUID userId) {
        List<FollowUserResponse> followerList = followService.getFollowers(userId);
        return followerList;
    }

    @GetMapping("/following/{userId}")
    public List<FollowUserResponse> getFollowing(@PathVariable UUID userId) {
        List<FollowUserResponse> followingList = followService.getFollowings(userId);

        return followingList;
    }
}
