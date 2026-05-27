package com.example.melodink.domain.user.dto.request;

import com.example.melodink.domain.user.entity.ProviderType;

public record SocialRecoverRequest(
        String email,
        ProviderType provider
) {
}
