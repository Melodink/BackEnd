package com.example.melodink.domain.user.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.example.melodink.domain.user.entity.ProviderType;
import com.example.melodink.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(ProviderType provider, String providerId);

    Optional<User> findByVerifiedEmail(String verifiedEmail);

    boolean existsByEmailIgnoreCase(String login);

    boolean existsByVerifiedEmailIgnoreCase(String verify);

    Optional<User> findByEmailOrVerifiedEmail(String any, String any1);
}
