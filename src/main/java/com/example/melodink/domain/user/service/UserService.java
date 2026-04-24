package com.example.melodink.domain.user.service;

import com.example.melodink.domain.user.dto.response.ShowInfoResponse;
import com.example.melodink.domain.user.entity.User;
import com.example.melodink.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    // 유저 정보 불러오기
    public ShowInfoResponse showUserInfo(User user) {
        User userInfo = userRepository.findByEmail(user.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("Invalid user")
        );

        ShowInfoResponse response = ShowInfoResponse.of(userInfo);
        return response;
    }


}
