package com.example.melodink.global.common;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * 외부 노출용 보조키 (UUIDv7)
     * - API URL, 응답 바디에서 id 대신 이 값을 사용
     * - DB 레벨 default: gen_uuid_v7() (PostgreSQL 17+)
     * - 애플리케이션 레벨 fallback: @PrePersist에서 생성
     */
    @Column(name = "public_id", nullable = false, updatable = false, unique = true,
            columnDefinition = "uuid DEFAULT gen_uuid_v7()")
    private UUID publicId;

    @PrePersist
    private void assignPublicId() {
        if (this.publicId == null) {
            this.publicId = Uuidv7Generator.generate();
        }
    }

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
