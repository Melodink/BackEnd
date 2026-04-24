package com.example.melodink.global.security.jwt;

import com.example.melodink.domain.user.entity.AccountStatus;
import com.example.melodink.domain.user.entity.User;
import com.example.melodink.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 화이트리스트
    /** 필요 시 화이트 리스트 추가 **/
    private static final RequestMatcher SKIP_AUTH = new OrRequestMatcher(
            new AntPathRequestMatcher("/api/auth/**"),
            new AntPathRequestMatcher("/oauth2/**"),
            new AntPathRequestMatcher("/login/**"),
            new AntPathRequestMatcher("/public/**")
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if(SKIP_AUTH.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveAccessToken(request);

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        }
        if (token == null) {
            token = request.getHeader("acess");
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try{
            // 토큰 만료 및 카테고리 검사
            if(jwtUtil.isTokenExpired(token)){     // 토큰 만료되었을때
                reject(response, HttpServletResponse.SC_UNAUTHORIZED, "access token expired","TOKEN_EXPIRED" );
                return;
            }
            String category = jwtUtil.getCategory(token);
            if(!category.equals("access")){
                reject(response, HttpServletResponse.SC_UNAUTHORIZED, "invalid access token category","INVALID_ACCESS_TOKEN");
                return;
            }

            String email = jwtUtil.getUsername(token);
            long tokenVersion = jwtUtil.getTokenVersion(token);

            User user = userRepository.findByEmail(email).orElse(null);
            if(user == null){
                reject(response, HttpServletResponse.SC_UNAUTHORIZED, "user not found","USER_NOT_FOUND");
                return;
            }
            if(user.getStatus() != AccountStatus.ACTIVE){
                reject(response, HttpServletResponse.SC_UNAUTHORIZED, "account inactive or deleted","ACCOUNT_INACTIVE");
                return;
            }
            if(user.getTokenVersion() != tokenVersion){
                reject(response, HttpServletResponse.SC_UNAUTHORIZED, "stale token (version mismatch)","TOKEN_STALE");
                return;
            }

            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().getAuthority()));



        } catch (Exception e) {
            log.warn("JWT processing failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            reject(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN", "invalid or malformed token");
        }
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }

    private void reject(HttpServletResponse resp, int status, String msg, String code) throws IOException {
        SecurityContextHolder.clearContext();
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        Map<String,Object> map = new HashMap<>();
        map.put("msg", msg);
        map.put("code", code);
        map.put("status", status);
        objectMapper.writeValue(resp.getWriter(), map);

    }
}
