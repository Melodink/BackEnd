package com.example.melodink.domain.artist.dto.request;

import com.example.melodink.domain.artist.entity.WorkType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 포트폴리오 작품 등록/수정 요청
 *
 * 업로드 흐름:
 * 1. POST /api/v1/artists/me/works/presigned → presignedUrl + objectKey 수령
 * 2. presignedUrl로 S3에 직접 PUT 업로드
 * 3. 이 요청에 mediaObjectKey 포함하여 작품 등록
 */
public record PortfolioWorkRequest(

        @NotBlank(message = "작품명을 입력해주세요.")
        @Size(max = 100)
        String title,

        @Size(max = 2000)
        String description,

        @NotNull(message = "작품 타입을 선택해주세요.")
        WorkType workType,

        /**
         * S3 Presigned 업로드 완료 후 받은 objectKey
         * 서비스에서 CDN URL로 변환 후 저장
         * 예: works/audio/abc123.mp3
         */
        @NotBlank(message = "미디어 파일을 업로드해주세요.")
        String mediaObjectKey,

        /**
         * 썸네일 S3 objectKey (선택)
         * null 전송 시 썸네일 없음으로 처리
         */
        String thumbnailObjectKey,

        boolean isFeatured
) {}