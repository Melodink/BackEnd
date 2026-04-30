package com.example.melodink.domain.auth.service;

import com.example.melodink.global.security.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ReissueService {
    private final JwtUtil jwtUtil;
    private final RefreshTokenStore refreshTokenStore;

    public void reissue(HttpServletRequest request, HttpServletResponse response) {

        String refresh = extractRefreshToken(request);

        if (refresh == null) {
            throw new RuntimeException("REFRESH_TOKEN_NOT_FOUND");
        }

        validateRefreshToken(refresh);

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);
        Long userId = jwtUtil.getUserId(refresh);
        Long tokenVersion = jwtUtil.getTokenVersion(refresh);

        if (!refreshTokenStore.exists(refresh)) {
            throw new RuntimeException("REFRESH_TOKEN_REUSE_DETECTED");
        }

        String newAccess = jwtUtil.createToken(
                "access",
                username,
                role,
                userId,
                tokenVersion,
                1000 * 60 * 10L
        );

        String newRefresh = jwtUtil.createToken(
                "refresh",
                username,
                role,
                userId,
                tokenVersion,
                1000 * 60 * 60 * 24L
        );

        // rotation
        refreshTokenStore.revoke(refresh);
        refreshTokenStore.save(username, newRefresh, Duration.ofDays(1));

        // 응답 세팅
        response.setHeader("Authorization", "Bearer " + newAccess);
        response.addCookie(createCookie("refresh", newRefresh));
    }

    /**
     * refresh token 추출
     */
    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * refresh token 검증
     */
    private void validateRefreshToken(String refresh) {
        try {
            jwtUtil.isTokenExpired(refresh);
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("REFRESH_TOKEN_EXPIRED");
        }

        String category = jwtUtil.getCategory(refresh);
        if (!"refresh".equals(category)) {
            throw new RuntimeException("INVALID_REFRESH_TOKEN");
        }
    }

    /**
     * 쿠키 생성
     */
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // 운영 필수
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }
}
