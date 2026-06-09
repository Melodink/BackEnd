package com.example.melodink.domain.notification.event;

import java.util.UUID;

public record FollowCreatedEvent(
        UUID followerPublicId,
        UUID followingPublicId
) {
}
