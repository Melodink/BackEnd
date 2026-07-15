package com.example.melodink.domain.job.dto.request;

import com.example.melodink.domain.job.entity.JobPosting.JobType;

// ── 채용 공고 검색 조건 ───────────────────────────────────
public record JobSearchRequest(
        String keyword,         // 제목·내용 키워드 검색
        JobType jobType,        // 직종 필터
        String location,        // 지역 필터
        Integer budgetMin,      // 예산 최소 필터
        Integer budgetMax,      // 예산 최대 필터
        boolean onlyOpen,       // true: 모집 중 공고만
        int page,
        int size
) {
    public JobSearchRequest {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
    }
}