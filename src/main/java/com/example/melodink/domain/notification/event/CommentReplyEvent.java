package com.example.melodink.domain.notification.event;

import java.util.UUID;

public record CommentReplyEvent(
    UUID postPublicId,
    UUID parentCommentOwnerPublicId,
    UUID replierPublicId,
    String replierNickname
) {
}
