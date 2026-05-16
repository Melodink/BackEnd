package com.example.melodink.domain.artist.dto.request;

import com.example.melodink.domain.artist.entity.ArtistSkill;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 아티스트 프로필 등록 요청
 *
 * record 전환 시 주의사항:
 * - record는 생성 후 필드 변경 불가 (불변) → Jackson 역직렬화 시 @JsonProperty 불필요
 * - 기본값(isPublic=true)은 record에서 직접 선언 불가 → @Schema 또는 문서로 명시
 * - 유효성 어노테이션은 컴포넌트에 직접 선언
 */
public record ArtistProfileCreateRequest(

        @NotBlank(message = "활동명을 입력해주세요.")
        @Size(max = 50)
        String stageName,

        @Size(max = 1000)
        String bio,

        @Size(max = 100)
        String location,

        String websiteUrl,

        /**
         * 프로필 이미지 CDN URL
         * POST /api/v1/artists/me/profile-image 업로드 후 반환된 imageUrl 값
         */
        String profileImageUrl,

        /**
         * 공개 여부 (미입력 시 클라이언트에서 true 전송 권장)
         */
        boolean isPublic,

        List<SkillRequest> skills
) {}