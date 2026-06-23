package com.example.melodink.global.security.oauth2;

import com.example.melodink.domain.auth.service.DualStoreRefreshTokenService;
import com.example.melodink.domain.user.entity.ProviderType;
import com.example.melodink.domain.user.entity.User;
import com.example.melodink.domain.user.repository.UserRepository;
import com.example.melodink.global.security.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final DualStoreRefreshTokenService refreshTokenService;
    private final String frontRedirectBase;

    public OAuth2LoginSuccessHandler(
            JwtUtil jwtUtil,
            UserRepository userRepository,
            DualStoreRefreshTokenService refreshTokenService,
            @Value("${app.front-redirect-base}") String frontRedirectBase   // .env에서 경로 가져온다.
    ) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.frontRedirectBase = frontRedirectBase;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId(); // "google"/"kakao"/"naver"
        OAuth2User oAuth2User = (OAuth2User) oauthToken.getPrincipal();
        Map<String, Object> attrs = oAuth2User.getAttributes();

        // provider / providerId 식별
        ProviderType provider = toProviderType(registrationId);
        String providerId = resolveProviderId(attrs);

        // 멤버 조회 (CustomOAuth2UserService에서 upsert 했으므로 반드시 존재)
        User user = userRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new IllegalStateException("OAuth2 user upsert missing: " + provider + ":" + providerId));


        String role = user.getRole().name(); // 예: USER → ROLE_USER
        long v = user.getTokenVersion();
        UUID publicId = user.getPublicId();

        // 3) JWT 생성
        String access  = jwtUtil.createToken("access", role, publicId, v, 10 * 60 * 1000L);      // 10분
        String refresh = jwtUtil.createToken("refresh", role, publicId, v, 24 * 60 * 60 * 1000L); // 24시간


        // 기존 사용자 토큰 일괄 삭제 후 저장(회전/일원화)
        refreshTokenService.revokeAllByUser(publicId);
        refreshTokenService.save(publicId, refresh, Duration.ofDays(1));

        // 응답 구성
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + access);
        response.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization");

        // Refresh 토큰은 HttpOnly 쿠키로
        ResponseCookie refreshCookie = ResponseCookie.from("refresh", refresh)
                .httpOnly(true)
                .secure(true)               // 로컬 http 테스트면 false, https 환경에서 true
                .sameSite("None")           // 프론트/백이 다른 오리진이면 필수
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());


        // 6) 프론트로 리다이렉트
        // - 헤더는 리다이렉트 후 JS에서 못 읽으므로, 짧게는 해시(#)에 access를 실어 전달 가능
        // - 더 안전한 방식은 코드(1회용 key)를 발급하고 프론트가 /api/auth/exchange 로 교환하는 방식

        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontRedirectBase)
                .fragment("access=" + access)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private ProviderType toProviderType(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> ProviderType.GOOGLE;
            case "kakao"  -> ProviderType.KAKAO;
            case "naver"  -> ProviderType.NAVER;
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }

    // 구글/카카오/네이버 대응: attributes에서 표준 id를 찾아냄
    private String resolveProviderId(Map<String, Object> attrs) {
        Object id = firstNonNull(
                attrs.get("id"),
                attrs.get("sub"),
                getDeep(attrs, "response.id")
        );
        if (id == null) throw new IllegalStateException("OAuth2 attributes missing id/sub/response.id");
        return String.valueOf(id);
    }

    private static Object firstNonNull(Object... vals) {
        for (Object v : vals) if (v != null) return v;
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Object getDeep(Map<String, Object> map, String path) {
        if (map == null || path == null) return null;
        String[] keys = path.split("\\.");
        Object cur = map;
        for (String k : keys) {
            if (!(cur instanceof Map)) return null;
            cur = ((Map<String, Object>) cur).get(k);
            if (cur == null) return null;
        }
        return cur;
    }
}
