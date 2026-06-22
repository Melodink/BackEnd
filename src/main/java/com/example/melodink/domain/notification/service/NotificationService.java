package com.example.melodink.domain.notification.service;

import com.example.melodink.domain.notification.dto.response.NotificationResponse;
import com.example.melodink.domain.notification.entity.Notification;
import com.example.melodink.domain.notification.entity.NotificationType;
import com.example.melodink.domain.notification.repository.NotificationRepository;
import com.example.melodink.domain.notification.sse.SseEmitterManager;
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
    private final SseEmitterManager sseEmitterManager;

    public void createFollowNotification(
            UUID followerPublicId,
            UUID followingPublicId
    ) {

        User follower =
                userRepository.findByPublicId(followerPublicId);

        User following =
                userRepository.findByPublicId(followingPublicId);

        Notification notification = notificationRepository.save(
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
        pushNotification(notification);
    }

    public void createPostCommentNotification(
            UUID postOwnerPublicId,
            UUID commenterPublicId,
            String commenterNickname,
            UUID postPublicId
    ){
        User receiver = userRepository.findByPublicId(postOwnerPublicId);

        Notification notification = notificationRepository.save(
                Notification.builder()
                        .user(receiver)
                        .type(NotificationType.POST_COMMENT)
                        .message(
                                commenterNickname + "님이 회원님의 게시글에 댓글을 남겼습니다."
                        )
                        .targetPublicId(postPublicId)
                        .build()
        );

        pushNotification(notification);
    }

    public void createReplyNotification(
            UUID parentOwnerCommentPublicId,
            UUID replierPublicId,
            String replierNickname,
            UUID postPublicId
    ){
        User receiver = userRepository.findByPublicId(parentOwnerCommentPublicId);

        Notification notification = notificationRepository.save(
                Notification.builder()
                        .user(receiver)
                        .type(NotificationType.COMMENT_REPLY)
                        .message(
                                replierNickname + "님이 회원님의 댓글에 답글을 남겼습니다."
                        )
                        .targetPublicId(postPublicId)
                        .build()
        );

        pushNotification(notification);
    }

    private void pushNotification(
            Notification notification
    ){
        NotificationResponse response = NotificationResponse.from(notification);

        sseEmitterManager.send(
                notification.getUser().getPublicId(),
                response
        );
    }

}
