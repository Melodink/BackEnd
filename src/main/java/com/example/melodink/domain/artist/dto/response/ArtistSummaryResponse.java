package com.example.melodink.domain.artist.dto.response;

import com.example.melodink.domain.artist.entity.ArtistProfile;

import java.util.List;
import java.util.UUID;

/**
 * 아티스트 검색 결과 카드 응답 (목록용 요약)
 */
public record ArtistSummaryResponse(
        UUID publicId,
        String stageName,
        String location,
        String profileImageUrl,
        List<SkillResponse> skills
) {
    public static ArtistSummaryResponse from(ArtistProfile p) {
        return new ArtistSummaryResponse(
                p.getPublicId(),
                p.getStageName(),
                p.getLocation(),
                p.getProfileImageUrl(),
                p.getSkills().stream()
                        .map(SkillResponse::from)
                        .toList()
        );
    }
}