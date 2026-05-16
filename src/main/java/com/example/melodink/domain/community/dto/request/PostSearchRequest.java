package com.example.melodink.domain.community.dto.request;

import com.example.melodink.domain.community.entity.Post.PostCategory;

public record PostSearchRequest(
        String keyword,           // 내용 키워드 검색
        PostCategory category,    // 카테고리 필터
        int page,
        int size
) {
    public PostSearchRequest {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
    }
}