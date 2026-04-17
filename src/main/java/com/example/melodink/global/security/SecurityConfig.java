package com.example.melodink.global.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final CustomOAuth2UserService customOAuth2UserService;
//    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final UserStatusPostChecker userStatusPostChecker;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        // 계정수집 방지: 비번 검증 "전" 상태 체크는 끈다(또는 no-op로 대체)
        provider.setPreAuthenticationChecks(user -> {});

        // 비번 검증 "후" 상태로 분기
        provider.setPostAuthenticationChecks(userStatusPostChecker);

        return new ProviderManager(provider);
    }

    /** 커스텀 로그인 필터 (ID/PW → JWT 발급) */
    @Bean
    public LoginFilter loginFilter(AuthenticationManager authenticationManager) throws Exception {
        LoginFilter loginFilter = new LoginFilter(
                authenticationManager,
                jwtUtil,
                refreshTokenService
        );
        loginFilter.setFilterProcessesUrl("/api/auth/login"); // 로그인 엔드포인트
        return loginFilter;
    }

    // 추후 Nginx에서 X-Forwarded-Proto 제대로 넘기는지 확인
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 프록시 헤더 신뢰 설정
                .requiresChannel(channel ->
                        channel.requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
                                .requiresSecure())
                .headers(headers -> headers
                                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)  // 최신 방식
                        // 또는 .frameOptions().deny() 대신:
                        // .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsMode.DENY))
                )

                // CORS
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    List<String> allowedOrigins = frontendUrl.contains(",")
                            ? List.of(frontendUrl.split(","))
                            : List.of("http://localhost:5173", frontendUrl);
                    config.setAllowedOrigins(allowedOrigins);
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setExposedHeaders(List.of("Authorization", "access", "X-Access-Token"));
                    config.setAllowCredentials(true); // refresh 토큰 쿠키 사용 시 필수
                    config.setMaxAge(3600L);
                    return config;
                }))

                // 기본 인증 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)


                // 인가 정책
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/oauth2/**", "/login/oauth2/code/**", "/oauth2/success").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/api/auth/**", "/reissue").permitAll()
                        .requestMatchers("/api/payments/success", "/api/payments/confirm").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/info", "/api/public/donation-total", "/api/members/dup-check/**").permitAll()
                        // 소프트 삭제 관련 요청
                        .requestMatchers(HttpMethod.POST, "/api/deactivate/**").authenticated()
                        .requestMatchers(HttpMethod.POST,  "/api/account/recover", "/api/recover/social").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/account/create").permitAll()

                        .requestMatchers(HttpMethod.PATCH, "/api/editpass", "/api/changeimage", "/api/editnickname").authenticated()

                        // 공개 조회
                        .requestMatchers(HttpMethod.GET, "/api/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()

                        // 댓글 관련 공개 허용
                        .requestMatchers(HttpMethod.GET, "/api/comments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comments/reports/reasons").permitAll()

                        .anyRequest().authenticated()
                )


                // OAuth2: 성공/실패 핸들러
//                .oauth2Login(o -> o
//                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
//                        .successHandler(oauth2LoginSuccessHandler)
//                        .failureHandler((request, response, ex) -> {
//                            String code = (ex instanceof OAuth2AuthenticationException)
//                                    ? ((OAuth2AuthenticationException) ex).getError().getErrorCode()
//                                    : "oauth2_error";
//                            String provider = request.getParameter("provider"); // 선택
//                            String email = (String) request.getAttribute("candidateEmail"); // 선택
//
//                            // 프론트엔드 라우트로 리다이렉트 (분리 배포)
//                            String target = frontendUrl + "/oauth2/success?error=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
//                                    + (provider != null ? "&provider=" + URLEncoder.encode(provider, StandardCharsets.UTF_8) : "")
//                                    + (email != null ? "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) : "");
//                            response.sendRedirect(target);
//                        })
//                )




                // 필터 체인 (순서 중요)
                // 1) JWT 인증 필터: 모든 요청 전에 토큰 해석/인증
                .addFilterBefore(new JwtFilter(jwtUtil, userRepository), UsernamePasswordAuthenticationFilter.class)
                // 2) 커스텀 로그인 필터: /auth/login 처리하여 JWT 발급
                .addFilterAt(loginFilter(), UsernamePasswordAuthenticationFilter.class)
                // 3) 커스텀 로그아웃 필터: 로그아웃/리프레시토큰 제거 등
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshTokenService), LogoutFilter.class)


                //세션 설정
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))


                // 인증 실패 시 401을 주고, 리다이렉트하지 않게
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((req, res, ex) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
                );


        return http.build();
    }
}

