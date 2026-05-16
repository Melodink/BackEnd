package com.example.melodink.domain.artist.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "artist_skills",
        indexes = {
                @Index(name = "idx_artist_skills_artist_id", columnList = "artist_id"),
                @Index(name = "idx_artist_skills_type", columnList = "skill_type")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArtistSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private ArtistProfile artistProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_type", nullable = false, length = 20)
    private SkillType skillType;

    @Column(nullable = false, length = 50)
    private String name;

    @Builder
    public ArtistSkill(ArtistProfile artistProfile, SkillType skillType, String name) {
        this.artistProfile = artistProfile;
        this.skillType = skillType;
        this.name = name;
    }
}