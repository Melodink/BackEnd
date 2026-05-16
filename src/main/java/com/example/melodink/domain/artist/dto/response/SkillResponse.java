package com.example.melodink.domain.artist.dto.response;

import com.example.melodink.domain.artist.entity.ArtistSkill;
import com.example.melodink.domain.artist.entity.SkillType;

/**
 * 스킬 응답
 */
public record SkillResponse(
        Long id,
        SkillType skillType,
        String name
) {
    public static SkillResponse from(ArtistSkill s) {
        return new SkillResponse(
                s.getId(),
                s.getSkillType(),
                s.getName()
        );
    }
}