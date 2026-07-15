package com.example.melodink.domain.job.repository;

import com.example.melodink.domain.job.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    // 공고의 전체 지원자 목록 (아티스트 + 유저 fetch join)
    @Query("""
        SELECT a FROM JobApplication a
        JOIN FETCH a.artist ap
        JOIN FETCH ap.user
        WHERE a.jobPosting.id = :jobPostingId
        ORDER BY a.appliedAt DESC
        """)
    List<JobApplication> findByJobPostingIdWithArtist(@Param("jobPostingId") Long jobPostingId);

    // 아티스트의 전체 지원 내역 (공고 + 디렉터 fetch join)
    @Query("""
        SELECT a FROM JobApplication a
        JOIN FETCH a.jobPosting j
        JOIN FETCH j.director
        WHERE a.artist.id = :artistProfileId
        ORDER BY a.appliedAt DESC
        """)
    List<JobApplication> findByArtistIdWithJobPosting(@Param("artistProfileId") Long artistProfileId);

    // 중복 지원 확인
    boolean existsByJobPostingIdAndArtistId(Long jobPostingId, Long artistProfileId);

    // 본인 지원 건 조회 (지원 취소용)
    @Query("""
        SELECT a FROM JobApplication a
        WHERE a.jobPosting.publicId = :jobPublicId
          AND a.artist.user.id = :userId
        """)
    Optional<JobApplication> findByJobPublicIdAndUserId(
            @Param("jobPublicId") java.util.UUID jobPublicId,
            @Param("userId") Long userId
    );

    // 디렉터의 공고에 속한 특정 지원 건 조회 (상태 변경용)
    @Query("""
        SELECT a FROM JobApplication a
        WHERE a.id = :applicationId
          AND a.jobPosting.director.id = :directorId
        """)
    Optional<JobApplication> findByIdAndDirectorId(
            @Param("applicationId") Long applicationId,
            @Param("directorId") Long directorId
    );
}