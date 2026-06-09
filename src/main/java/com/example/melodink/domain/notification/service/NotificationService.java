package com.example.melodink.domain.notification.service;

import com.example.melodink.domain.notification.entity.Notification;
import com.example.melodink.domain.notification.entity.NotificationType;
import com.example.melodink.domain.notification.repository.NotificationRepository;
import com.example.melodink.domain.user.entity.User;
import com.example.melodink.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void createFollowNotification(
            UUID followerPublicId,
            UUID followingPublicId
    ) {

        User follower =
                userRepository.findByPublicId(followerPublicId);

        User following =
                userRepository.findByPublicId(followingPublicId);

        notificationRepository.save(
                Notification.builder()
                        .user(following)
                        .type(NotificationType.FOLLOW)
                        .message(
                                follower.getNickname()
                                        + "님이 회원님을 팔로우했습니다."
                        )
                        .targetPublicId(
                                follower.getPublicId()
                        )
                        .build()
        );
    }
}
