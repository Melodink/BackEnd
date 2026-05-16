package com.example.melodink.global.mail.dto.request;

import com.example.melodink.global.mail.tokenPayload.EmailTokenPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailCodeSendRequest(
        @NotBlank @Email String email,
        EmailTokenPurpose purpose
) {}
