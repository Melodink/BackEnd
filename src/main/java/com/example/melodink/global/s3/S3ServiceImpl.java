package com.example.melodink.global.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

/**
 * 실제 AWS S3 구현체
 * cloud.aws.s3.enabled=true 일 때만 빈으로 등록
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cloud.aws.s3.enabled", havingValue = "true")
public class S3ServiceImpl implements S3Service {

    private static final int PRESIGNED_URL_EXPIRE_MINUTES = 15;
    private static final int PRESIGNED_URL_EXPIRE_SECONDS = PRESIGNED_URL_EXPIRE_MINUTES * 60;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    @Override
    public PresignedUploadResponse generatePresignedUrl(S3FileType fileType,
                                                        String originalFilename,
                                                        String contentType) {
        validateFileExtension(fileType, originalFilename);

        String objectKey = buildObjectKey(fileType, originalFilename);

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(PRESIGNED_URL_EXPIRE_MINUTES))
                .putObjectRequest(put -> put
                        .bucket(s3Properties.getBucket())
                        .key(objectKey)
                        .contentType(contentType)
                        .build())
                .build();

        String presignedUrl = s3Presigner
                .presignPutObject(presignRequest)
                .url()
                .toString();

        return new PresignedUploadResponse(
                presignedUrl,
                objectKey,
                buildCdnUrl(objectKey),
                PRESIGNED_URL_EXPIRE_SECONDS
        );
    }

    @Override
    public String upload(MultipartFile file, S3FileType fileType) {
        validateFile(file, fileType);

        String objectKey = buildObjectKey(fileType, file.getOriginalFilename());

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Properties.getBucket())
                            .key(objectKey)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new S3Exception("파일 업로드에 실패했습니다.", e);
        }

        log.info("[S3] 업로드 완료: key={}", objectKey);
        return buildCdnUrl(objectKey);
    }

    @Override
    public void delete(String cdnUrlOrKey) {
        String objectKey = extractObjectKey(cdnUrlOrKey);
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(objectKey)
                    .build());
            log.info("[S3] 삭제 완료: key={}", objectKey);
        } catch (Exception e) {
            log.warn("[S3] 삭제 실패: key={}, error={}", objectKey, e.getMessage());
        }
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────

    private String buildObjectKey(S3FileType fileType, String originalFilename) {
        String ext = extractExtension(originalFilename);
        String uniqueName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        return fileType.getDirectory() + "/" + uniqueName;
    }

    private String buildCdnUrl(String objectKey) {
        return s3Properties.getCdnBaseUrl() + "/" + objectKey;
    }

    private String extractObjectKey(String cdnUrlOrKey) {
        String cdnBase = s3Properties.getCdnBaseUrl();
        if (cdnUrlOrKey.startsWith(cdnBase)) {
            return cdnUrlOrKey.substring(cdnBase.length() + 1);
        }
        return cdnUrlOrKey;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new S3Exception("파일 확장자를 확인할 수 없습니다.");
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private void validateFile(MultipartFile file, S3FileType fileType) {
        if (file == null || file.isEmpty()) {
            throw new S3Exception("파일이 비어있습니다.");
        }
        validateFileExtension(fileType, file.getOriginalFilename());
        if (fileType.isExceededSize(file.getSize())) {
            throw new S3Exception(String.format(
                    "파일 크기가 허용 용량을 초과했습니다. 최대: %dMB",
                    fileType.getMaxSizeBytes() / (1024 * 1024)
            ));
        }
    }

    private void validateFileExtension(S3FileType fileType, String filename) {
        String ext = extractExtension(filename);
        if (!fileType.isAllowedExtension(ext)) {
            throw new S3Exception(String.format(
                    "허용되지 않는 파일 형식입니다. 허용 확장자: %s",
                    fileType.getAllowedExtensions()
            ));
        }
    }

    public static class S3Exception extends RuntimeException {
        public S3Exception(String message) { super(message); }
        public S3Exception(String message, Throwable cause) { super(message, cause); }
    }
}