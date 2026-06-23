package com.example.melodink.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {
    private final StringRedisTemplate redis;

    /** email 기반 사용자별 키세트 및 개별 토큰 키  */
    private String tokenKey(String tokenHash) { return "rt:token:" + tokenHash; }

    private String userSet(UUID publicId)       { return "rt:user:" + publicId.toString(); }

    private String hash(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public void save(UUID publicId, String refreshToken, Duration ttl) {
        String h = hash(refreshToken);
        ValueOperations<String, String> ops = redis.opsForValue();
        ops.set(tokenKey(h), publicId.toString(), ttl);
        redis.opsForSet().add(userSet(publicId), h);
        redis.expire(userSet(publicId), ttl.plusDays(1));
    }

    public boolean exists(String refreshToken) {
        String h = hash(refreshToken);
        return Boolean.TRUE.equals(redis.hasKey(tokenKey(h)));
    }

    public void revoke(String refreshToken) {
        String h = hash(refreshToken);
        String publicId = redis.opsForValue().get(tokenKey(h));
        redis.delete(tokenKey(h));
        if (publicId != null) redis.opsForSet().remove(userSet(UUID.fromString(publicId)), h);
    }

    public void revokeAllByUser(UUID publicId) {
        String setKey = userSet(publicId);
        Set<String> users = redis.opsForSet().members(setKey);
        if (users != null) {
            for (String h : users) redis.delete(tokenKey(h));
        }
        redis.delete(setKey);
    }
}
