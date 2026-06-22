package com.example.melodink.domain.notification.handler;

import com.example.melodink.domain.notification.event.FollowCreatedEvent;
import com.example.melodink.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FollowNotificationHandler {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener( phase = TransactionPhase.AFTER_COMMIT)
    public void handle(FollowCreatedEvent event ) {

        notificationService.createFollowNotification(
                event.followerPublicId(),
                event.followingPublicId()
        );
    }
}
