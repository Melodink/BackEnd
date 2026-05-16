package com.example.melodink.domain.artist.dto.request;

import com.example.melodink.domain.artist.entity.SkillType;

/**
 * 아티스트 검색 조건
 * GET /api/v1/artists?keyword=&skillType=&skillName=&location=&page=0&size=20
 *
 * @ModelAttribute 바인딩 사용
 * record의 컴포넌트명이 쿼리 파라미터명과 일치해야 함
 */
public record ArtistSearchRequest(
        String keyword,
        SkillType skillType,
        String skillName,
        String location,
        int page,
        int size
) {
    /**
     * 기본값 설정용 compact constructor
     * page, size가 null로 들어올 경우 대비
     */
    public ArtistSearchRequest {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100; // 최대 페이지 크기 제한
    }
}