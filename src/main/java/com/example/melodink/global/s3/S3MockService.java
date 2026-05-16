package com.example.melodink.global.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 로컬 개발용 S3 Mock 구현체
 * cloud.aws.s3.enabled=false (기본값) 일 때 등록
 *
 * 실제 S3 호출 없이 가상의 URL을 반환해서
 * AWS 자격증명 없이도 전체 기능 개발·테스트 가능
 */
@Slf4j
@Service
@ConditionalOnProperty(
        name = "cloud.aws.s3.enabled",
        havingValue = "false",
        matchIfMissing = true   // 설정 자체가 없을 때도 Mock 등록
)
public class S3MockService implements S3Service {

    private static final String MOCK_BASE_URL = "http://localhost:8080/mock-s3";

    @Override
    public PresignedUploadResponse generatePresignedUrl(S3FileType fileType,
                                                        String originalFilename,
                                                        String contentType) {
        String objectKey = fileType.getDirectory() + "/"
                + UUID.randomUUID().toString().replace("-", "")
                + getExtension(originalFilename);

        String mockPresignedUrl = MOCK_BASE_URL + "/presigned/" + objectKey;
        String mockCdnUrl = MOCK_BASE_URL + "/" + objectKey;

        log.info("[S3 Mock] Presigned URL 발급: fileType={}, objectKey={}", fileType, objectKey);

        return new PresignedUploadResponse(
                mockPresignedUrl,
                objectKey,
                mockCdnUrl,
                900
        );
    }

    @Override
    public String upload(MultipartFile file, S3FileType fileType) {
        String objectKey = fileType.getDirectory() + "/"
                + UUID.randomUUID().toString().replace("-", "")
                + getExtension(file.getOriginalFilename());

        String mockCdnUrl = MOCK_BASE_URL + "/" + objectKey;

        log.info("[S3 Mock] 업로드: filename={}, cdnUrl={}", file.getOriginalFilename(), mockCdnUrl);

        return mockCdnUrl;
    }

    @Override
    public void delete(String cdnUrlOrKey) {
        log.info("[S3 Mock] 삭제: key={}", cdnUrlOrKey);
    }

    private String getExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return "." + filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        }
        return "";
    }
}