package com.example.melodink.domain.job.repository;

import com.example.melodink.domain.job.dto.request.JobSearchRequest;
import com.example.melodink.domain.job.entity.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface JobPostingRepositoryCustom {
    Page<JobPosting> searchPostings(JobSearchRequest condition, PageRequest pageable);
}