package com.example.melodink.domain.user.dto.response;

import java.util.UUID;

public record FollowUserResponse(
        UUID publicId,
        String nickname,
        String profileImageUrl
) {
}
