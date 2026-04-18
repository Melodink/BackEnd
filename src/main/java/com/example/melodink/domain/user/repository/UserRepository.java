package com.example.melodink.domain.user.repository;

import com.example.melodink.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
