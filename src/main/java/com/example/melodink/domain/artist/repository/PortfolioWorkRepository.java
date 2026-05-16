package com.example.melodink.domain.artist.repository;

import com.example.melodink.domain.artist.entity.PortfolioWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioWorkRepository extends JpaRepository<PortfolioWork, Long> {

    Optional<PortfolioWork> findByPublicId(UUID publicId);

    List<PortfolioWork> findByArtistProfileIdOrderByCreatedAtDesc(Long artistProfileId);

    List<PortfolioWork> findByArtistProfileIdAndIsFeaturedTrueOrderByCreatedAtDesc(Long artistProfileId);

    // publicId + userId 교차 검증 (소유자 확인)
    @Query("""
        SELECT pw FROM PortfolioWork pw
        WHERE pw.publicId = :publicId
          AND pw.artistProfile.user.id = :userId
        """)
    Optional<PortfolioWork> findByPublicIdAndUserId(
            @Param("publicId") UUID publicId,
            @Param("userId") Long userId
    );

    // 조회수 직접 UPDATE (더티체킹 없이 단건 처리)
    @Modifying
    @Query("UPDATE PortfolioWork pw SET pw.viewCount = pw.viewCount + 1 WHERE pw.id = :id")
    void incrementViewCount(@Param("id") Long id);
}