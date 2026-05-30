package com.example.melodink.domain.user.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.example.melodink.domain.user.entity.ProviderType;
import com.example.melodink.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(ProviderType provider, String providerId);

    Optional<User> findByVerifiedEmail(String verifiedEmail);

    boolean existsByEmailIgnoreCase(String login);

    boolean existsByVerifiedEmailIgnoreCase(String verify);

    Optional<User> findByEmailOrVerifiedEmail(String any, String any1);

   Optional<User> findByEmailAndProvider(String email, ProviderType provider);

    boolean existsByNicknameIgnoreCase(String nick);

    List<Long> findIdsToHardDelete(LocalDateTime now, Pageable of);

    List<UserEmailProjection> findEmailsByIdIn(List<Long> ids);

    void deleteByIdIn(List<Long> ids);
}
