package com.example.melodink.domain.job.entity;

import com.example.melodink.domain.artist.entity.ArtistProfile;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JobApplication은 공고 상세 조회 내부에서만 노출되므로
 * public_id 불필요 → bigserial PK만 사용
 */
@Entity
@Table(
        name = "job_applications",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"job_id", "artist_id"})
        },
        indexes = {
                @Index(name = "idx_job_applications_job_id", columnList = "job_id"),
                @Index(name = "idx_job_applications_artist_id", columnList = "artist_id"),
                @Index(name = "idx_job_applications_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPosting jobPosting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private ArtistProfile artist;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @CreatedDate
    @Column(name = "applied_at", nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    @Builder
    public JobApplication(JobPosting jobPosting, ArtistProfile artist, String coverLetter) {
        this.jobPosting = jobPosting;
        this.artist = artist;
        this.coverLetter = coverLetter;
        this.status = ApplicationStatus.PENDING;
    }

    public void review() { this.status = ApplicationStatus.REVIEWED; }
    public void accept() { this.status = ApplicationStatus.ACCEPTED; }
    public void reject() { this.status = ApplicationStatus.REJECTED; }

    public enum ApplicationStatus {
        PENDING, REVIEWED, ACCEPTED, REJECTED
    }
}
