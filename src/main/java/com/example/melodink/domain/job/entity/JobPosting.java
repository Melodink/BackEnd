package com.example.melodink.domain.job.entity;

import com.example.melodink.domain.user.entity.User;
import com.example.melodink.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "job_postings",
        indexes = {
                @Index(name = "idx_job_postings_director_id", columnList = "director_id"),
                @Index(name = "idx_job_postings_public_id", columnList = "public_id"),
                @Index(name = "idx_job_postings_status", columnList = "status"),
                @Index(name = "idx_job_postings_job_type", columnList = "job_type")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPosting extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id", nullable = false)
    private User director;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 20)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status;

    @Column(length = 100)
    private String location;

    @Column(name = "budget_min")
    private Integer budgetMin;

    @Column(name = "budget_max")
    private Integer budgetMax;

    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;

    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobApplication> applications = new ArrayList<>();

    @Builder
    public JobPosting(User director, String title, String description, JobType jobType,
                      String location, Integer budgetMin, Integer budgetMax, LocalDateTime deadlineAt) {
        this.director = director;
        this.title = title;
        this.description = description;
        this.jobType = jobType;
        this.status = JobStatus.OPEN;
        this.location = location;
        this.budgetMin = budgetMin;
        this.budgetMax = budgetMax;
        this.deadlineAt = deadlineAt;
    }

    public void update(String title, String description, String location,
                       Integer budgetMin, Integer budgetMax, LocalDateTime deadlineAt) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.budgetMin = budgetMin;
        this.budgetMax = budgetMax;
        this.deadlineAt = deadlineAt;
    }

    public void close() { this.status = JobStatus.CLOSED; }
    public void complete() { this.status = JobStatus.COMPLETED; }
    public boolean isOpen() { return this.status == JobStatus.OPEN; }

    public enum JobType {
        FULL_TIME, PART_TIME, FREELANCE, PROJECT, SESSION
    }

    public enum JobStatus {
        OPEN, CLOSED, COMPLETED
    }
}