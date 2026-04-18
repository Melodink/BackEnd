package com.example.melodink.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    ADMIN("관리자"),
    USER("일반유저"),
    ARTIST("아티스트"),
    DIRECTOR("디렉터");

    private final String description;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
