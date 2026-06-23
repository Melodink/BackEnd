package com.example.melodink.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;


    public JwtUtil(@Value("${jwt.secret}") String secret) {

        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS512.key().build().getAlgorithm());
    }

    private Claims parseClaims(String token) {
        try{
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {   // 토큰 만료시에도 claim 필요한 경우가 있어 반환
            log.error(e.getMessage());
            return e.getClaims();
        }
    }

    public Boolean isTokenExpired(String token) { return parseClaims(token).getExpiration().before(new Date()); }

//    public String getUsername(String token) { return parseClaims(token).get("username", String.class); }

    public String getRole(String token) { return parseClaims(token).get("role", String.class); }

    public String getCategory(String token) { return parseClaims(token).get("category", String.class); }

//    public Long getUserId(String token) { return parseClaims(token).get("userId", Long.class); }

    public UUID getPublicId(String token) { return parseClaims(token).get("publicId", UUID.class); }

    public long getTokenVersion(String token) {
        Object claims = parseClaims(token).get("tokenVersion");
        if (claims == null ) return 0;
        if(claims instanceof Number n) return n.longValue();
        try{
            return Long.parseLong(claims.toString());
        }catch (Exception e) { return 0; }
    }

    public String createToken(String category, String role, UUID publicId ,Long tokenVersion, Long expirationTime) {
        return Jwts.builder()
                .claim("category", category)
                .claim("role", role)
                .claim("publicId", publicId)
                .claim("tokenVersion", tokenVersion)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }
}
