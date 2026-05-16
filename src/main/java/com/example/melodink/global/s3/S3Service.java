package com.example.melodink.global.s3;

import org.springframework.web.multipart.MultipartFile;

/**
 * S3 서비스 인터페이스
 *
 * S3 활성화 여부에 따라 실제 구현체(S3ServiceImpl) 또는
 * 로컬 Mock 구현체(S3MockService)가 주입됨
 */
public interface S3Service {
    PresignedUploadResponse generatePresignedUrl(S3FileType fileType,
                                                 String originalFilename,
                                                 String contentType);
    String upload(MultipartFile file, S3FileType fileType);
    void delete(String cdnUrlOrKey);
}