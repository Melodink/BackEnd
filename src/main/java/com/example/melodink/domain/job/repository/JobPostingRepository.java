package com.example.melodink.domain.job.repository;

import com.example.melodink.domain.job.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JobPostingRepository
        extends JpaRepository<JobPosting, Long>,
        JobPostingRepositoryCustom {

    Optional<JobPosting> findByPublicId(UUID publicId);

    @Query("""
        SELECT j FROM JobPosting j
        JOIN FETCH j.director
        WHERE j.publicId = :publicId
        """)
    Optional<JobPosting> findByPublicIdWithDirector(@Param("publicId") UUID publicId);

    @Query("""
        SELECT j FROM JobPosting j
        WHERE j.publicId = :publicId
          AND j.director.id = :userId
        """)
    Optional<JobPosting> findByPublicIdAndDirectorId(
            @Param("publicId") UUID publicId,
            @Param("userId") Long userId
    );
}