package com.example.melodink.domain.artist.service;

import com.example.melodink.domain.artist.dto.request.*;
import com.example.melodink.domain.artist.dto.response.*;
import com.example.melodink.domain.artist.entity.ArtistProfile;
import com.example.melodink.domain.artist.entity.ArtistSkill;
import com.example.melodink.domain.artist.entity.PortfolioWork;
import com.example.melodink.domain.artist.repository.ArtistProfileRepository;
import com.example.melodink.domain.artist.repository.PortfolioWorkRepository;
import com.example.melodink.domain.user.entity.Role;
import com.example.melodink.domain.user.entity.User;
import com.example.melodink.domain.user.repository.UserRepository;
import com.example.melodink.global.s3.S3FileType;
import com.example.melodink.global.s3.S3Properties;
import com.example.melodink.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtistService {

    private final ArtistProfileRepository artistProfileRepository;
    private final PortfolioWorkRepository portfolioWorkRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final S3Properties s3Properties;

    // ══════════════════════════════════════════════════════════
    // 아티스트 프로필
    // ══════════════════════════════════════════════════════════

    /**
     * 아티스트 프로필 등록
     * - USER → ARTIST 역할 승격
     * - profileImageUrl은 사전 업로드된 CDN URL 그대로 저장
     */
    @Transactional
    public ArtistProfileResponse registerProfile(Long userId, ArtistProfileCreateRequest request) {
        User user = findUser(userId);

        if (artistProfileRepository.existsByUserId(userId)) {
            throw new ArtistException("이미 아티스트 프로필이 존재합니다.");
        }

        ArtistProfile profile = ArtistProfile.builder()
                .user(user)
                .stageName(request.stageName())
                .bio(request.bio())
                .location(request.location())
                .websiteUrl(request.websiteUrl())
                .profileImageUrl(request.profileImageUrl())
                .isPublic(request.isPublic())
                .build();

        if (request.skills() != null) {
            List<ArtistSkill> skills = buildSkills(profile, request.skills());
            profile.replaceSkills(skills);
        }

        artistProfileRepository.save(profile);
        user.changeRole(Role.ARTIST);

        return ArtistProfileResponse.from(profile);
    }

    /** 공개 프로필 상세 조회 */
    public ArtistProfileResponse getPublicProfile(UUID publicId) {
        ArtistProfile profile = artistProfileRepository
                .findPublicProfileWithDetails(publicId)
                .orElseThrow(() -> new ArtistException("아티스트 프로필을 찾을 수 없습니다."));
        return ArtistProfileResponse.from(profile);
    }

    /** 본인 프로필 조회 (비공개 포함) */
    public ArtistProfileResponse getMyProfile(Long userId) {
        ArtistProfile profile = artistProfileRepository
                .findMyProfileWithDetails(userId)
                .orElseThrow(() -> new ArtistException("아티스트 프로필을 찾을 수 없습니다."));
        return ArtistProfileResponse.from(profile);
    }

    /**
     * 프로필 수정
     * - profileImageUrl이 변경되면 기존 S3 이미지 삭제
     */
    @Transactional
    public ArtistProfileResponse updateProfile(Long userId, ArtistProfileUpdateRequest request) {
        ArtistProfile profile = findProfileByUserId(userId);

        // 프로필 이미지 변경 시 기존 S3 파일 삭제
        if (StringUtils.hasText(request.profileImageUrl())
                && !request.profileImageUrl().equals(profile.getProfileImageUrl())) {
            deleteS3FileIfExists(profile.getProfileImageUrl());
        }

        profile.update(
                request.stageName(),
                request.bio(),
                request.location(),
                request.websiteUrl(),
                // null이면 기존 URL 유지
                StringUtils.hasText(request.profileImageUrl())
                        ? request.profileImageUrl()
                        : profile.getProfileImageUrl()
        );

        if (request.skills() != null) {
            profile.replaceSkills(buildSkills(profile, request.skills()));
        }

        return ArtistProfileResponse.from(profile);
    }

    /** 공개/비공개 토글 */
    @Transactional
    public void changeVisibility(Long userId, boolean isPublic) {
        findProfileByUserId(userId).changeVisibility(isPublic);
    }

    /** 아티스트 검색 (QueryDSL 동적 쿼리) */
    public Page<ArtistSummaryResponse> searchArtists(ArtistSearchRequest condition) {
        PageRequest pageable = PageRequest.of(condition.page(), condition.size());
        return artistProfileRepository
                .searchArtists(condition, pageable)
                .map(ArtistSummaryResponse::from);
    }

    // ══════════════════════════════════════════════════════════
    // 포트폴리오 작품
    // ══════════════════════════════════════════════════════════

    /**
     * 작품 등록
     * - mediaObjectKey → CDN URL 변환 후 저장
     * - thumbnailObjectKey → CDN URL 변환 후 저장
     */
    @Transactional
    public PortfolioWorkResponse addWork(Long userId, PortfolioWorkRequest request) {
        ArtistProfile profile = findProfileByUserId(userId);

        String mediaUrl = buildCdnUrl(request.mediaObjectKey());
        String thumbnailUrl = StringUtils.hasText(request.thumbnailObjectKey())
                ? buildCdnUrl(request.thumbnailObjectKey())
                : null;

        PortfolioWork work = PortfolioWork.builder()
                .artistProfile(profile)
                .title(request.title())
                .description(request.description())
                .workType(request.workType())
                .mediaUrl(mediaUrl)
                .thumbnailUrl(thumbnailUrl)
                .isFeatured(request.isFeatured())
                .build();

        portfolioWorkRepository.save(work);
        return PortfolioWorkResponse.from(work);
    }

    /**
     * 작품 상세 조회 + 조회수 증가
     */
    @Transactional
    public PortfolioWorkResponse getWork(UUID publicId) {
        PortfolioWork work = portfolioWorkRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ArtistException("작품을 찾을 수 없습니다."));
        portfolioWorkRepository.incrementViewCount(work.getId());
        return PortfolioWorkResponse.from(work);
    }

    /** 아티스트 전체 작품 목록 */
    public List<PortfolioWorkSummaryResponse> getWorks(UUID artistPublicId) {
        ArtistProfile profile = artistProfileRepository.findByPublicId(artistPublicId)
                .orElseThrow(() -> new ArtistException("아티스트 프로필을 찾을 수 없습니다."));
        return portfolioWorkRepository
                .findByArtistProfileIdOrderByCreatedAtDesc(profile.getId())
                .stream().map(PortfolioWorkSummaryResponse::from).toList();
    }

    /**
     * 작품 수정
     * - 미디어/썸네일 URL이 변경되면 기존 S3 파일 삭제
     */
    @Transactional
    public PortfolioWorkResponse updateWork(Long userId, UUID workPublicId,
                                            PortfolioWorkRequest request) {
        PortfolioWork work = findWorkByOwner(userId, workPublicId);

        String newMediaUrl = buildCdnUrl(request.mediaObjectKey());
        String newThumbnailUrl = StringUtils.hasText(request.thumbnailObjectKey())
                ? buildCdnUrl(request.thumbnailObjectKey())
                : null;

        // 변경된 파일만 기존 S3 삭제
        if (!newMediaUrl.equals(work.getMediaUrl())) {
            deleteS3FileIfExists(work.getMediaUrl());
        }
        if (newThumbnailUrl != null && !newThumbnailUrl.equals(work.getThumbnailUrl())) {
            deleteS3FileIfExists(work.getThumbnailUrl());
        }

        work.update(request.title(), request.description(), newMediaUrl, newThumbnailUrl);
        return PortfolioWorkResponse.from(work);
    }

    /** 대표작 토글 */
    @Transactional
    public void toggleFeatured(Long userId, UUID workPublicId) {
        findWorkByOwner(userId, workPublicId).toggleFeatured();
    }

    /**
     * 작품 삭제
     * - DB 삭제 + S3 미디어·썸네일 파일 삭제
     */
    @Transactional
    public void deleteWork(Long userId, UUID workPublicId) {
        PortfolioWork work = findWorkByOwner(userId, workPublicId);

        // S3 파일 먼저 삭제 (트랜잭션 커밋 전)
        deleteS3FileIfExists(work.getMediaUrl());
        deleteS3FileIfExists(work.getThumbnailUrl());

        portfolioWorkRepository.delete(work);
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ArtistException("유저를 찾을 수 없습니다."));
    }

    private ArtistProfile findProfileByUserId(Long userId) {
        return artistProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ArtistException("아티스트 프로필을 찾을 수 없습니다."));
    }

    private PortfolioWork findWorkByOwner(Long userId, UUID workPublicId) {
        return portfolioWorkRepository
                .findByPublicIdAndUserId(workPublicId, userId)
                .orElseThrow(() -> new ArtistException("작품을 찾을 수 없거나 수정 권한이 없습니다."));
    }

    private List<ArtistSkill> buildSkills(ArtistProfile profile, List<SkillRequest> requests) {
        return requests.stream()
                .map(s -> ArtistSkill.builder()
                        .artistProfile(profile)
                        .skillType(s.skillType())
                        .name(s.name())
                        .build())
                .toList();
    }

    /**
     * objectKey → CDN URL 변환
     * 예: works/audio/abc123.mp3 → https://cdn.melodink.com/works/audio/abc123.mp3
     */
    private String buildCdnUrl(String objectKey) {
        return s3Properties.getCdnBaseUrl() + "/" + objectKey;
    }

    /**
     * S3 파일 삭제 (null·빈값 안전 처리)
     * 삭제 실패는 경고 로그만 남기고 서비스 흐름 유지
     */
    private void deleteS3FileIfExists(String cdnUrl) {
        if (StringUtils.hasText(cdnUrl)) {
            try {
                s3Service.delete(cdnUrl);
            } catch (Exception e) {
                log.warn("[S3] 파일 삭제 실패 (무시): url={}, error={}", cdnUrl, e.getMessage());
            }
        }
    }

    public static class ArtistException extends RuntimeException {
        public ArtistException(String message) { super(message); }
    }
}