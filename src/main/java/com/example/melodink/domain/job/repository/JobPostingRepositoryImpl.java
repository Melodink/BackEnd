package com.example.melodink.domain.job.repository;

import com.example.melodink.domain.job.dto.request.JobSearchRequest;
import com.example.melodink.domain.job.entity.JobPosting;
import com.example.melodink.domain.job.entity.JobPosting.JobStatus;
import com.example.melodink.domain.job.entity.QJobPosting;
import com.example.melodink.domain.user.entity.QUser;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * JobPosting QueryDSL 동적 검색 구현체
 *
 * Specification 대신 QueryDSL 사용 이유:
 * - director fetch join과 동적 조건을 함께 처리할 때
 *   Specification은 count 쿼리 분기 처리가 번거로움
 * - QueryDSL은 fetch join / 일반 join을 명시적으로 분리 가능
 * - 복잡한 예산 범위 조건도 가독성 높게 표현 가능
 */
@RequiredArgsConstructor
public class JobPostingRepositoryImpl implements JobPostingRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QJobPosting posting = QJobPosting.jobPosting;
    private static final QUser director = QUser.user;

    @Override
    public Page<JobPosting> searchPostings(JobSearchRequest condition, PageRequest pageable) {

        // ── 데이터 조회 (director fetch join 포함) ────────────
        List<JobPosting> content = queryFactory
                .selectFrom(posting)
                .join(posting.director, director).fetchJoin()
                .where(
                        keywordContains(condition.keyword()),
                        jobTypeEq(condition.jobType()),
                        locationContains(condition.location()),
                        budgetInRange(condition.budgetMin(), condition.budgetMax()),
                        onlyOpenEq(condition.onlyOpen())
                )
                .orderBy(posting.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // ── 카운트 쿼리 (fetch join 제외) ─────────────────────
        Long total = queryFactory
                .select(posting.count())
                .from(posting)
                .where(
                        keywordContains(condition.keyword()),
                        jobTypeEq(condition.jobType()),
                        locationContains(condition.location()),
                        budgetInRange(condition.budgetMin(), condition.budgetMax()),
                        onlyOpenEq(condition.onlyOpen())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    // ── 조건 메서드 ───────────────────────────────────────────

    /** 제목 또는 내용 키워드 검색 */
    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) return null;
        String pattern = keyword.toLowerCase();
        return posting.title.containsIgnoreCase(pattern)
                .or(posting.description.containsIgnoreCase(pattern));
    }

    /** 직종 필터 */
    private BooleanExpression jobTypeEq(JobPosting.JobType jobType) {
        return jobType != null ? posting.jobType.eq(jobType) : null;
    }

    /** 지역 필터 */
    private BooleanExpression locationContains(String location) {
        return StringUtils.hasText(location)
                ? posting.location.containsIgnoreCase(location) : null;
    }

    /**
     * 예산 범위 필터 (범위 겹침 방식)
     * 공고 예산 범위와 검색 예산 범위가 겹치는 경우만 조회
     * 조건: 검색 budgetMin <= 공고 budgetMax AND 검색 budgetMax >= 공고 budgetMin
     */
    private BooleanExpression budgetInRange(Integer budgetMin, Integer budgetMax) {
        if (budgetMin == null && budgetMax == null) return null;

        BooleanExpression minCondition = budgetMin != null
                ? posting.budgetMax.goe(budgetMin) : null;
        BooleanExpression maxCondition = budgetMax != null
                ? posting.budgetMin.loe(budgetMax) : null;

        if (minCondition != null && maxCondition != null) {
            return minCondition.and(maxCondition);
        }
        return minCondition != null ? minCondition : maxCondition;
    }

    /** 모집 중 공고만 필터 */
    private BooleanExpression onlyOpenEq(boolean onlyOpen) {
        return onlyOpen ? posting.status.eq(JobStatus.OPEN) : null;
    }
}