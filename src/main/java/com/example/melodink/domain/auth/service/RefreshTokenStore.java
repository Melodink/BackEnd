package com.example.melodink.domain.auth.service;

import java.time.Duration;

public interface RefreshTokenStore {
    void save(String email, String refreshToken, Duration ttl);
    boolean exists(String refreshToken);
    void revoke(String refreshToken);
    void revokeAllByUser(String email);
}
