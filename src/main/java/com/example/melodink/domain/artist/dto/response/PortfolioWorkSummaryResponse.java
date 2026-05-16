package com.example.melodink.domain.artist.dto.response;

import com.example.melodink.domain.artist.entity.PortfolioWork;
import com.example.melodink.domain.artist.entity.WorkType;

import java.util.UUID;

/**
 * 포트폴리오 작품 요약 응답 (목록 조회용)
 * 상세 정보 없이 카드 렌더링에 필요한 최소 필드만 포함
 */
public record PortfolioWorkSummaryResponse(
        UUID publicId,
        String title,
        WorkType workType,
        String thumbnailUrl,
        int viewCount
) {
    public static PortfolioWorkSummaryResponse from(PortfolioWork w) {
        return new PortfolioWorkSummaryResponse(
                w.getPublicId(),
                w.getTitle(),
                w.getWorkType(),
                w.getThumbnailUrl(),
                w.getViewCount()
        );
    }
}