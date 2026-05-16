package com.example.melodink.domain.community.repository;

import com.example.melodink.domain.community.dto.request.PostSearchRequest;
import com.example.melodink.domain.community.entity.Post;
import com.example.melodink.domain.community.entity.QPost;
import com.example.melodink.domain.user.entity.QUser;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QPost post = QPost.post;
    private static final QUser user = QUser.user;

    /**
     * 동적 검색 조건:
     *  - keyword  : 내용 LIKE 검색
     *  - category : 카테고리 필터
     *
     * user fetch join으로 N+1 방지
     * 최신순 정렬
     */
    @Override
    public Page<Post> searchPosts(PostSearchRequest condition, PageRequest pageable) {

        List<Post> content = queryFactory
                .selectFrom(post)
                .join(post.user, user).fetchJoin()
                .where(
                        keywordContains(condition.keyword()),
                        categoryEq(condition.category())
                )
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        keywordContains(condition.keyword()),
                        categoryEq(condition.category())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword)
                ? post.content.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression categoryEq(Post.PostCategory category) {
        return category != null ? post.category.eq(category) : null;
    }
}
