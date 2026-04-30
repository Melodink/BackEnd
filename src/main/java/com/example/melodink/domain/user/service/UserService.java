package com.example.melodink.domain.user.service;

import com.example.melodink.domain.user.dto.response.ShowInfoResponse;
import com.example.melodink.domain.user.entity.User;
import com.example.melodink.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


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

    public void editPassword(Long userId, String password, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User Not Found"));
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

    public void editNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User Not Found"));
        if(!RESERVED_NICKNAMES.contains(nickname) ) {
            throw new AccessDeniedException("Invalid nickname");
        }
        if (user == null) { throw new AccessDeniedException("User Not Found"); }
        user.setNickname(nickname);
        userRepository.save(user);
    }


}
