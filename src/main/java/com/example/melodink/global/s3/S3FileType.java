package com.example.melodink.global.s3;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * S3 업로드 파일 타입별 설정
 * - directory : S3 버킷 내 저장 경로 prefix
 * - allowed   : 허용 확장자 목록
 */
@Getter
@RequiredArgsConstructor
public enum S3FileType {

    PROFILE_IMAGE(
            "profiles",
            Set.of("jpg", "jpeg", "png", "webp"),
            10 * 1024 * 1024L   // 10MB
    ),
    PORTFOLIO_AUDIO(
            "works/audio",
            Set.of("mp3", "wav", "flac", "aac", "ogg"),
            100 * 1024 * 1024L  // 100MB
    ),
    PORTFOLIO_VIDEO(
            "works/video",
            Set.of("mp4", "mov", "webm"),
            500 * 1024 * 1024L  // 500MB
    ),
    PORTFOLIO_IMAGE(
            "works/image",
            Set.of("jpg", "jpeg", "png", "webp", "gif"),
            10 * 1024 * 1024L   // 10MB
    ),
    PORTFOLIO_SCORE(
            "works/score",
            Set.of("pdf", "xml", "mxl"),
            20 * 1024 * 1024L   // 20MB
    ),
    THUMBNAIL(
            "thumbnails",
            Set.of("jpg", "jpeg", "png", "webp"),
            5 * 1024 * 1024L    // 5MB
    );

    private final String directory;
    private final Set<String> allowedExtensions;
    private final long maxSizeBytes;

    public boolean isAllowedExtension(String extension) {
        return allowedExtensions.contains(extension.toLowerCase());
    }

    public boolean isExceededSize(long fileSize) {
        return fileSize > maxSizeBytes;
    }
}
