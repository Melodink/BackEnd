package com.example.melodink.global.security.auth;

import com.example.melodink.domain.user.entity.AccountStatus;
import com.example.melodink.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OAuth2User {

    @Getter
    private final User user;
    private final Map<String, Object> attributes;

    // 일반 로그인
    public CustomUserDetails(User user) {
        this.user = user;
        this.attributes = Collections.emptyMap();
    }

    // OAuth2
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = (attributes == null) ? Collections.emptyMap() : Map.copyOf(attributes);
    }

    @Override
    public Map<String, Object> getAttributes() { return attributes; }

    @SuppressWarnings("unchecked")
    @Override
    public <A> A getAttribute(String name) {
        if (attributes.isEmpty() || name == null) return null;

        // 1) 1차 시도: 동일 키로 바로 조회
        Object v = attributes.get(name);
        if (v != null) return (A) v;

        // 2) 표준 키 보정(id/email/name/picture) - 구글/카카오/네이버 호환
        switch (name) {
            case "id":
                v = firstNonNull(
                        attributes.get("id"),                  // kakao
                        attributes.get("sub"),                 // google(OIDC)
                        getDeep(attributes, "response.id")     // naver
                );
                break;

            case "email":
                v = firstNonNull(
                        attributes.get("email"),                       // google(프로필에 포함될 수 있음)
                        getDeep(attributes, "kakao_account.email"),    // kakao
                        getDeep(attributes, "response.email")          // naver
                );
                break;

            case "name":
                v = firstNonNull(
                        attributes.get("name"),                        // google
                        getDeep(attributes, "properties.nickname"),    // kakao
                        getDeep(attributes, "response.name")           // naver
                );
                break;

            case "picture":
                v = firstNonNull(
                        attributes.get("picture"),                                 // google
                        getDeep(attributes, "properties.profile_image"),           // kakao (구)
                        getDeep(attributes, "kakao_account.profile.profile_image_url"), // kakao (신)
                        getDeep(attributes, "response.profile_image")              // naver
                );
                break;

            default:
                // 3) 점 표기 경로 지원: "response.email", "kakao_account.profile.profile_image_url" 등
                v = getDeep(attributes, name);
        }

        return (A) v;
    }

    private static Object firstNonNull(Object... values) {
        for (Object v : values) if (v != null) return v;
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String raw = String.valueOf(user.getRole());          // 예: USER / ROLE_USER
        String role = raw.startsWith("ROLE_") ? raw : "ROLE_" + raw;
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override public String getPassword()  { return user.getPassword(); }
    @Override public String getUsername()  { return user.getEmail(); }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return user.getStatus() != AccountStatus.SUSPENDED; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return user.getStatus() == AccountStatus.ACTIVE; }

    @Override
    public String getName() {
        // OAuth2 주 식별자 → 없으면 엔티티/이메일로 폴백
        Object id = getAttribute("id");
        if (id != null) return String.valueOf(id);
        if (user.getId() != null) return String.valueOf(user.getId());
        return user.getEmail();
    }

    // Member ID를 반환하는 메서드 추가
    public Long getMemberId() {
        return user.getId();
    }

    public Long getId() {
        return user.getId();
    }

}
