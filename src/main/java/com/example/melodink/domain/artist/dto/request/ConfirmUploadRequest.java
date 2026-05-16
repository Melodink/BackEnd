package com.example.melodink.domain.artist.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Presigned 업로드 완료 확인 요청
 * 클라이언트가 S3 직접 업로드 완료 후 objectKey를 서버에 전달할 때 사용
 */
public record ConfirmUploadRequest(

        @NotBlank(message = "objectKey를 입력해주세요.")
        String objectKey
) {}