package com.example.melodink.global.common;

import com.example.melodink.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetentionCleanupJob {
    private final UserService userService;

    // 매일 03:15 (서울) — 필요 시 조정
    @Scheduled(cron = "0 15 3 * * *", zone = "Asia/Seoul")
    public void purge() {
        int total = 0;
        int n;
        do {
            n = userService.hardDeleteExpiredUsersChunk(500);
            total += n;
        } while (n > 0);
        log.info("Hard-deleted {} expired members", total);
    }
}
