package com.example.melodink.global.common;

import com.example.melodink.domain.user.entity.AccountStatus;
import com.example.melodink.domain.user.entity.Role;
import com.example.melodink.domain.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * 역할 검증 유틸리티
 *
 * 모든 서비스에서 공통으로 사용하는 역할 검증 로직을 중앙화
 * 역할 관련 예외 메시지도 여기서 통일 관리
 */
@Component
public class RoleValidator {

    /**
     * DIRECTOR 역할 검증
     * 채용 공고 등록·수정·삭제·지원자 관리에 사용
     */
    public void validateDirector(User user) {
        if (user.getRole() != Role.DIRECTOR) {
            throw new RoleException("디렉터만 사용할 수 있는 기능입니다.");
        }
    }

    /**
     * ARTIST 역할 검증
     * 포트폴리오 관리·채용 지원에 사용
     */
    public void validateArtist(User user) {
        if (user.getRole() != Role.ARTIST) {
            throw new RoleException("아티스트만 사용할 수 있는 기능입니다.");
        }
    }

    /**
     * 아티스트 프로필 최초 등록 가능 여부 검증
     * USER 역할만 아티스트로 전환 가능
     * (이미 ARTIST·DIRECTOR·ADMIN인 경우 불가)
     */
    public void validateCanRegisterArtist(User user) {
        if (user.getRole() != Role.USER) {
            throw new RoleException("일반 유저만 아티스트로 등록할 수 있습니다.");
        }
    }

    /**
     * 아티스트 프로필 보유 여부 검증
     * 포트폴리오 작품 등록·수정·삭제 시 ARTIST 역할 확인
     */
    public void validateArtistOrHigher(User user) {
        if (user.getRole() == Role.USER) {
            throw new RoleException("아티스트 프로필이 필요합니다.");
        }
    }

    /**
     * 로그인 유저 활성 상태 검증
     * 비활성화된 계정의 쓰기 작업 차단
     */
    public void validateActive(User user) {
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new RoleException("비활성화된 계정입니다.");
        }
    }

    public static class RoleException extends RuntimeException {
        public RoleException(String message) { super(message); }
    }
}