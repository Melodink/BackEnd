package com.example.melodink.domain.notification.handler;

import com.example.melodink.domain.notification.event.CommentReplyEvent;
import com.example.melodink.domain.notification.event.PostCommentEvent;
import com.example.melodink.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CommentNotificationHandler {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(PostCommentEvent event ) {

        notificationService.createPostCommentNotification(
                event.postOwnerPublicId(),
                event.commenterPublicId(),
                event.commenterNickname(),
                event.postPublicId()
        );
    }

    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(CommentReplyEvent event) {

        notificationService.createReplyNotification(
                event.parentCommentOwnerPublicId(),
                event.replierPublicId(),
                event.replierNickname(),
                event.postPublicId()
        );
    }
}
