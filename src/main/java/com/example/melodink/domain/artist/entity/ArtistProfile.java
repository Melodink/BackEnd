package com.example.melodink.domain.artist.entity;

import com.example.melodink.domain.user.entity.User;
import com.example.melodink.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "artist_profiles",
        indexes = {
                @Index(name = "idx_artist_profiles_user_id", columnList = "user_id"),
                @Index(name = "idx_artist_profiles_public_id", columnList = "public_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArtistProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "stage_name", nullable = false, length = 50)
    private String stageName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 100)
    private String location;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    @OneToMany(mappedBy = "artistProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArtistSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "artistProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioWork> works = new ArrayList<>();

    @Builder
    public ArtistProfile(User user, String stageName, String bio, String location,
                         String websiteUrl, String profileImageUrl, boolean isPublic) {
        this.user = user;
        this.stageName = stageName;
        this.bio = bio;
        this.location = location;
        this.websiteUrl = websiteUrl;
        this.profileImageUrl = profileImageUrl;
        this.isPublic = isPublic;
    }

    public void update(String stageName, String bio, String location,
                       String websiteUrl, String profileImageUrl) {
        this.stageName = stageName;
        this.bio = bio;
        this.location = location;
        this.websiteUrl = websiteUrl;
        this.profileImageUrl = profileImageUrl;
    }

    public void changeVisibility(boolean isPublic) {
        this.isPublic = isPublic;
    }

    // 스킬 교체: 기존 전부 제거 후 새로 추가 (orphanRemoval 활용)
    public void replaceSkills(List<ArtistSkill> newSkills) {
        this.skills.clear();
        this.skills.addAll(newSkills);
    }
}