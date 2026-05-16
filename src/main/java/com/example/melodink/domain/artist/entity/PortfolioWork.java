package com.example.melodink.domain.artist.entity;

import com.example.melodink.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "portfolio_works",
        indexes = {
                @Index(name = "idx_portfolio_works_artist_id", columnList = "artist_id"),
                @Index(name = "idx_portfolio_works_public_id", columnList = "public_id"),
                @Index(name = "idx_portfolio_works_is_featured", columnList = "is_featured")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioWork extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private ArtistProfile artistProfile;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_type", nullable = false, length = 20)
    private WorkType workType;

    @Column(name = "media_url", nullable = false)
    private String mediaUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured = false;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Builder
    public PortfolioWork(ArtistProfile artistProfile, String title, String description,
                         WorkType workType, String mediaUrl, String thumbnailUrl, boolean isFeatured) {
        this.artistProfile = artistProfile;
        this.title = title;
        this.description = description;
        this.workType = workType;
        this.mediaUrl = mediaUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.isFeatured = isFeatured;
    }

    public void update(String title, String description,
                       String mediaUrl, String thumbnailUrl) {
        this.title = title;
        this.description = description;
        this.mediaUrl = mediaUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void toggleFeatured() { this.isFeatured = !this.isFeatured; }
    public void incrementViewCount() { this.viewCount++; }
}