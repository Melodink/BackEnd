package com.example.melodink.domain.notification.event;

import java.util.UUID;

public record PostCommentEvent(
        UUID postPublicId,
        UUID postOwnerPublicId,
        UUID commenterPublicId,
        String commenterNickname
) {
}
