package com.example.melodink.domain.artist.dto.response;

import com.example.melodink.domain.artist.entity.ArtistProfile;
import com.example.melodink.domain.artist.entity.PortfolioWork;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 아티스트 프로필 상세 응답
 * record는 불변이므로 from() 정적 팩토리에서 한 번에 생성
 */
public record ArtistProfileResponse(
        UUID publicId,
        String stageName,
        String bio,
        String location,
        String websiteUrl,
        String profileImageUrl,
        boolean isPublic,
        List<SkillResponse> skills,
        List<PortfolioWorkSummaryResponse> featuredWorks,
        LocalDateTime createdAt
) {
    public static ArtistProfileResponse from(ArtistProfile p) {
        return new ArtistProfileResponse(
                p.getPublicId(),
                p.getStageName(),
                p.getBio(),
                p.getLocation(),
                p.getWebsiteUrl(),
                p.getProfileImageUrl(),
                p.isPublic(),
                p.getSkills().stream()
                        .map(SkillResponse::from)
                        .toList(),
                p.getWorks().stream()
                        .filter(PortfolioWork::isFeatured)
                        .map(PortfolioWorkSummaryResponse::from)
                        .toList(),
                p.getCreatedAt()
        );
    }
}