package com.example.melodink.domain.artist.repository;

import com.example.melodink.domain.artist.dto.request.ArtistSearchRequest;
import com.example.melodink.domain.artist.entity.ArtistProfile;
import com.example.melodink.domain.artist.entity.QArtistProfile;
import com.example.melodink.domain.artist.entity.QArtistSkill;
import com.example.melodink.domain.artist.entity.SkillType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class ArtistProfileRepositoryImpl implements ArtistProfileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QArtistProfile artist = QArtistProfile.artistProfile;
    private static final QArtistSkill skill = QArtistSkill.artistSkill;

    @Override
    public Page<ArtistProfile> searchArtists(ArtistSearchRequest condition, PageRequest pageable) {

        boolean hasSkillCondition = condition.skillType() != null
                || StringUtils.hasText(condition.skillName());

        // 데이터 조회
        var query = queryFactory.selectDistinct(artist).from(artist);
        if (hasSkillCondition) query.join(artist.skills, skill);

        List<ArtistProfile> content = query
                .where(
                        isPublic(),
                        keywordContains(condition.keyword()),
                        locationContains(condition.location()),
                        skillTypeEq(condition.skillType()),
                        skillNameContains(condition.skillName())
                )
                .orderBy(artist.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리 (페이징용 — 데이터 쿼리와 분리)
        var countQuery = queryFactory.select(artist.countDistinct()).from(artist);
        if (hasSkillCondition) countQuery.join(artist.skills, skill);

        Long total = countQuery
                .where(
                        isPublic(),
                        keywordContains(condition.keyword()),
                        locationContains(condition.location()),
                        skillTypeEq(condition.skillType()),
                        skillNameContains(condition.skillName())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression isPublic() {
        return artist.isPublic.isTrue();
    }

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword)
                ? artist.stageName.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression locationContains(String location) {
        return StringUtils.hasText(location)
                ? artist.location.containsIgnoreCase(location) : null;
    }

    private BooleanExpression skillTypeEq(SkillType type) {
        return type != null ? skill.skillType.eq(type) : null;
    }

    private BooleanExpression skillNameContains(String name) {
        return StringUtils.hasText(name)
                ? skill.name.containsIgnoreCase(name) : null;
    }
}
