package com.example.melodink.domain.artist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 아티스트 프로필 수정 요청
 * profileImageUrl이 null이면 서비스에서 기존 URL 유지
 */
public record ArtistProfileUpdateRequest(

        @NotBlank(message = "활동명을 입력해주세요.")
        @Size(max = 50)
        String stageName,

        @Size(max = 1000)
        String bio,

        @Size(max = 100)
        String location,

        String websiteUrl,

        /**
         * 새 프로필 이미지 CDN URL
         * null 전송 시 서비스 레이어에서 기존 URL 유지
         */
        String profileImageUrl,

        List<SkillRequest> skills
) {}