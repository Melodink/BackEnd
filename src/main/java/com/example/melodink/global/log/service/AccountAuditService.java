package com.example.melodink.global.log.service;

import com.example.melodink.global.log.entity.AccountEventLog;
import com.example.melodink.global.log.entity.AccountEventType;
import com.example.melodink.global.log.repository.AccountEventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountAuditService {

    private final AccountEventLogRepository repo;

    public void log(AccountEventType type, Long memberId, String email,
                    String ip, String ua, String purpose, boolean success, String message) {
        repo.save(AccountEventLog.builder()
                .eventType(type)
                .memberId(memberId)
                .email(email)
                .ip(ip)
                .ua(ua)
                .purpose(purpose)
                .success(success)
                .message(message)
                .build());
    }
}
