package com.example.melodink.global.log.repository;

import com.example.melodink.global.log.entity.AccountEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountEventLogRepository extends JpaRepository<AccountEventLog, Long> { }