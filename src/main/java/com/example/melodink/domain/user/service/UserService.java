package com.example.melodink.domain.user.service;

import com.example.melodink.domain.auth.service.RefreshTokenStore;
import com.example.melodink.domain.community.service.PostService;
import com.example.melodink.domain.user.dto.response.DupCheckResponse;
import com.example.melodink.domain.user.dto.response.ShowInfoResponse;
import com.example.melodink.domain.user.entity.AccountStatus;
import com.example.melodink.domain.user.entity.ProviderType;
import com.example.melodink.domain.user.entity.User;
import com.example.melodink.domain.user.repository.UserRepository;
import com.example.melodink.global.s3.S3FileType;
import com.example.melodink.global.s3.S3Service;
import com.example.melodink.global.security.auth.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenStore refreshTokenStore;

    private final PostService postService;
    private final S3Service s3Service;


    private static final Pattern EMAIL_RX = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}$]");
    private static final Pattern NICKNAME_RX = Pattern.compile("^[A-Za-z0-9가-힣._-]{2,20}$");

    private static final Set<String> RESERVED_NICKNAMES = Set.of("admin", "administrator", "owner", "system", "null", "undefined", "root");

    public boolean validatePassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User Not Found"));
        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new AccessDeniedException("Invalid password");
        }
        return user.getPassword().equals(passwordEncoder.encode(password));
    }

    // 유저 정보 불러오기
    public ShowInfoResponse showUserInfo(User user) {
        User userInfo = userRepository.findByEmail(user.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("Invalid user")
        );

        ShowInfoResponse response = ShowInfoResponse.of(userInfo);
        return response;
    }

    // local user delete method
    @Transactional
    public void deactivateUser(Long userId, Duration retention, String password) {
        User u = userRepository.getReferenceById(userId);
        boolean valid = validatePassword(userId, password);
        if(valid) {
           u.setStatus(AccountStatus.DELETED);
           u.setDeletedAt(LocalDateTime.now());
           u.setRetentionUntil(LocalDateTime.now().plus(retention));
           u.setTokenVersion(u.getTokenVersion() + 1);
           refreshTokenStore.revokeAllByUser(u.getEmail());
        } else {
            throw new AccessDeniedException("Invalid user");
        }
    }

    // social user delete method
    @Transactional
    public void deactivateUser(Long userId, Duration retention) {
        User u = userRepository.findById(userId).orElse(null);
        if(u != null) {
            u.setStatus(AccountStatus.DELETED);
            u.setDeletedAt(LocalDateTime.now());
            u.setRetentionUntil(LocalDateTime.now().plus(retention));
            u.setTokenVersion(u.getTokenVersion() + 1);
            refreshTokenStore.revokeAllByUser(u.getEmail());
        } else {
            throw new AccessDeniedException("Invalid user");
        }
    }

    @Transactional
    public void recoverAccount(String email) {
        User u = userRepository.findByEmail(email).orElse(null);
        if(u == null) {
            u = userRepository.findByVerifiedEmail(email).orElseThrow(
                    () -> new IllegalArgumentException("user not found")
            );
        }
        if(u.getStatus() != AccountStatus.DELETED) throw new AccessDeniedException("user isn't deleted");
        if(u.getRetentionUntil() != null && u.getRetentionUntil().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("retention expired");
        }

        u.setStatus(AccountStatus.ACTIVE);
        u.setDeletedAt(null);
        u.setRetentionUntil(null);
        u.setTokenVersion(u.getTokenVersion() + 1);
        refreshTokenStore.revokeAllByUser(u.getEmail());
    }

    @Transactional
    public void recoverSocial(String email, ProviderType provider) {
        User u = userRepository.findByEmailAndProvider(email, provider).orElse(null);
        if (u.getStatus() != AccountStatus.DELETED) throw new IllegalStateException("not deleted");
        if (u.getRetentionUntil() != null && u.getRetentionUntil().isBefore(LocalDateTime.now()))
            throw new IllegalStateException("retention expired");

        u.setStatus(AccountStatus.ACTIVE);
        u.setDeletedAt(null);
        u.setRetentionUntil(null);
        u.setTokenVersion(u.getTokenVersion() + 1); // 새 버전으로 재발급 유도

        // 방어적으로 기존 refresh 전부 제거 (깨끗한 상태로 시작)
        refreshTokenStore.revokeAllByUser(u.getEmail());
    }

    @Transactional
    public void editPassword(CustomUserDetails userDetails, String password, String newPassword) {
        User user = userRepository.findById(userDetails.getUser().getId()).orElseThrow(() -> new IllegalArgumentException("User Not Found"));
        boolean valid = validatePassword(user.getId(), password);
        if(!valid) {
            throw new AccessDeniedException("Invalid password");
        }else {
            user.setPassword(passwordEncoder.encode(newPassword));
            // 비번 변경 시 기존 토큰 무효화 및 재발급
            user.setTokenVersion(user.getTokenVersion() + 1);
            userRepository.save(user);
        }
    }

    public void editNickname(CustomUserDetails userDetails, String nickname) {
        User user = userRepository.findById(userDetails.getUser().getId()).orElseThrow(() -> new IllegalArgumentException("User Not Found"));
        if(!RESERVED_NICKNAMES.contains(nickname) ) {
            throw new AccessDeniedException("Invalid nickname");
        }
        if (user == null) { throw new AccessDeniedException("User Not Found"); }
        user.setNickname(nickname);
        userRepository.save(user);
    }

    public void editImage(CustomUserDetails customUserDetails, MultipartFile file) {

        User user = userRepository.findByEmail(customUserDetails.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("user not found")
        );
        if(user == null) throw new AccessDeniedException("member not found");

        String imageUrl = null;
        imageUrl = s3Service.upload(file, S3FileType.PROFILE_IMAGE);
        user.setPictureUrl(imageUrl);
        userRepository.save(user);
    }

    public DupCheckResponse checkEmail(String rawEmail) {
        if (rawEmail == null) return DupCheckResponse.invalid("email");
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);
        if (email.isEmpty() || !EMAIL_RX.matcher(email).matches()) {
            return DupCheckResponse.invalid("email");
        }
        boolean exists = userRepository.existsByEmailIgnoreCase(email);
        return exists ? DupCheckResponse.dup("email") : DupCheckResponse.ok();
    }

    public DupCheckResponse checkVerifyingEmail(String rawEmail) {
        if (rawEmail == null) return DupCheckResponse.invalid("email");
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);
        if (email.isEmpty() || !EMAIL_RX.matcher(email).matches()) {
            return DupCheckResponse.invalid("email");
        }
        boolean exists = userRepository.existsByVerifiedEmailIgnoreCase(email);
        return exists ? DupCheckResponse.dup("email") : DupCheckResponse.ok();
    }

    public DupCheckResponse checkNickname(String rawNickname) {
        if (rawNickname == null) return DupCheckResponse.invalid("nickname");
        String nick = rawNickname.trim();
        if (nick.isEmpty() || !NICKNAME_RX.matcher(nick).matches()) {
            return DupCheckResponse.invalid("nickname");
        }
        if (RESERVED_NICKNAMES.contains(nick.toLowerCase(Locale.ROOT))) {
            return new DupCheckResponse(false, "RESERVED", "This nickname is reserved");
        }
        boolean exists = userRepository.existsByNicknameIgnoreCase(nick);
        return exists ? DupCheckResponse.dup("nickname") : DupCheckResponse.ok();
    }
}
