package com.example.melodink.global.mail.service;

import com.example.melodink.domain.user.dto.request.SignUpRequest;
import com.example.melodink.global.mail.dto.response.EmailCodeSendResponse;
import com.example.melodink.global.mail.dto.response.EmailCodeVerifyResponse;
import com.example.melodink.global.mail.dto.response.LookupResponse;
import com.example.melodink.global.mail.tokenPayload.EmailTokenPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public interface EmailAuthService {
    LookupResponse sendUsernameLookupEmail(String email, String ip, String ua);
    Long signup(SignUpRequest request, String ip, String ua);
    void resendEmailVerification(String email, String ip, String ua);

    void resetPassword(String email, String newRawPassword);

    EmailCodeSendResponse sendCode(@NotBlank @Email String email, @NotBlank EmailTokenPurpose purpose, String ip);

    EmailCodeVerifyResponse verifyCode(@NotBlank @Email String email, @NotNull EmailTokenPurpose purpose, @NotBlank @Pattern(regexp = "^\\d{6}$", message = "6자리 숫자 코드여야 합니다") String code, String ip);
}
