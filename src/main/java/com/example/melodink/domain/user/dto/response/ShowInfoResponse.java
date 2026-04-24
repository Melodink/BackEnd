package com.example.melodink.domain.user.dto.response;

import com.example.melodink.domain.user.entity.ProviderType;
import com.example.melodink.domain.user.entity.Role;
import com.example.melodink.domain.user.entity.User;

import java.time.LocalDateTime;

public record ShowInfoResponse (
        Long userId,
        String email,
        String name,
        String nickname,
        String pictureUrl,
        Role role,
        ProviderType provider,
        LocalDateTime createAt
){

    public static ShowInfoResponse of(User user) {
        return new ShowInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getPictureUrl(),
                user.getRole(),
                user.getProvider(),
                user.getCreateAt()
        );
    }
}
