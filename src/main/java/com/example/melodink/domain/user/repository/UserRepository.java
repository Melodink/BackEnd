package com.example.melodink.domain.user.repository;

import com.example.melodink.domain.user.entity.ProviderType;
import com.example.melodink.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(ProviderType provider, String providerId);

    Optional<User> findByVerifiedEmail(String verifiedEmail);

    boolean existsByEmailIgnoreCase(String login);

    boolean existsByVerifiedEmailIgnoreCase(String verify);

    Optional<User> findByEmailOrVerifiedEmail(String any, String any1);

   Optional<User> findByEmailAndProvider(String email, ProviderType provider);

    boolean existsByNicknameIgnoreCase(String nick);

    @Query("""
       select u.id from User u
       where u.status = 'DELETED'
         and u.retentionUntil is not null
         and u.retentionUntil < :now
    """)
    List<Long> findIdsToHardDelete(@Param("now") LocalDateTime now, Pageable pageable);

    List<UserPublicIdProjection> findPublicIdsByIdIn(List<Long> ids);

    void deleteByIdIn(List<Long> ids);

    User findByPublicId(UUID publicId);
}
