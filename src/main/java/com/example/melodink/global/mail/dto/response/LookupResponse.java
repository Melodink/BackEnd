package com.example.melodink.global.mail.dto.response;

public record LookupResponse(
        String email
) {
    public static LookupResponse from(String email) {
        return new LookupResponse(email);
    }
}
