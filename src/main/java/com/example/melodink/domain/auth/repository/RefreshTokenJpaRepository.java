package com.example.melodink.domain.auth.repository;

import com.example.melodink.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {
    // 해시 기반으로 변경
    boolean existsByTokenHash(String tokenHash);
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Transactional
    void deleteByTokenHash(String tokenHash);
    @Transactional
    void deleteAllByEmail(String email);
}