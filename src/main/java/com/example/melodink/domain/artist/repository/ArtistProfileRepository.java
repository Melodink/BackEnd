package com.example.melodink.domain.artist.repository;

import com.example.melodink.domain.artist.entity.ArtistProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ArtistProfileRepository
        extends JpaRepository<ArtistProfile, Long>, ArtistProfileRepositoryCustom {

    Optional<ArtistProfile> findByPublicId(UUID publicId);
    Optional<ArtistProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    // 공개 프로필 상세 (skills + works fetch join — N+1 방지)
    @Query("""
        SELECT DISTINCT ap FROM ArtistProfile ap
        LEFT JOIN FETCH ap.skills
        LEFT JOIN FETCH ap.works
        WHERE ap.publicId = :publicId AND ap.isPublic = true
        """)
    Optional<ArtistProfile> findPublicProfileWithDetails(@Param("publicId") UUID publicId);

    // 본인 프로필 (비공개 포함)
    @Query("""
        SELECT DISTINCT ap FROM ArtistProfile ap
        LEFT JOIN FETCH ap.skills
        LEFT JOIN FETCH ap.works
        WHERE ap.user.id = :userId
        """)
    Optional<ArtistProfile> findMyProfileWithDetails(@Param("userId") Long userId);
}