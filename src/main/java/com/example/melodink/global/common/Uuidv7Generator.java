package com.example.melodink.global.common;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

// UUID v7 생성기
public final class Uuidv7Generator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Uuidv7Generator() {}

    public static UUID generate() {
        long epochMs = Instant.now().toEpochMilli();

        // 상위 64bit: [48bit timestamp][4bit version=7][12bit random]
        long msb = (epochMs << 16)
                | (0x7000L)                          // version 7
                | (RANDOM.nextLong() & 0x0FFFL);     // 12bit random

        // 하위 64bit: [2bit variant=10][62bit random]
        long lsb = (RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL)
                | 0x8000000000000000L;               // variant bits

        return new UUID(msb, lsb);
    }

    /** UUID에서 생성 시각(밀리초) 추출 */
    public static long extractEpochMs(UUID uuidV7) {
        return uuidV7.getMostSignificantBits() >>> 16;
    }
}