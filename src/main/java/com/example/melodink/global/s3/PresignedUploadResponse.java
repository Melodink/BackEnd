package com.example.melodink.global.s3;

// ── Presigned URL 응답 ────────────────────────────────────

public record PresignedUploadResponse (
    String presignedUrl,   // S3 직접 업로드용 PUT URL
    String objectKey,      // 업로드 완료 후 서버에 전달할 키
    String cdnUrl,         // 업로드 후 접근 가능한 CDN URL
    int expiresInSeconds  // URL 만료 시간 (초)
) {
}