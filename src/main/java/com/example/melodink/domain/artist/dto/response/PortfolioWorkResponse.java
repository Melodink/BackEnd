package com.example.melodink.domain.artist.dto.response;

import com.example.melodink.domain.artist.entity.PortfolioWork;
import com.example.melodink.domain.artist.entity.WorkType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

// ── 포트폴리오 작품 상세 응답 ─────────────────────────────
@Builder
public record PortfolioWorkResponse(
        UUID publicId,
        String title,
        String description,
        WorkType workType,
        String mediaUrl,
        String thumbnailUrl,
        boolean isFeatured,
        int viewCount,
        LocalDateTime createdAt
) {

    public static PortfolioWorkResponse from(PortfolioWork work) {
        return PortfolioWorkResponse.builder()
                .publicId(work.getPublicId())
                .title(work.getTitle())
                .description(work.getDescription())
                .workType(work.getWorkType())
                .mediaUrl(work.getMediaUrl())
                .thumbnailUrl(work.getThumbnailUrl())
                .isFeatured(work.isFeatured())
                .viewCount(work.getViewCount())
                .createdAt(work.getCreatedAt())
                .build();
    }
}
