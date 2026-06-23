package com.example.melodink.domain.auth.service;

import java.time.Duration;
import java.util.UUID;

public interface RefreshTokenStore {
    void save(UUID publicId, String refreshToken, Duration ttl);
    boolean exists(String refreshToken);
    void revoke(String refreshToken);
    void revokeAllByUser(UUID publicId);
}
