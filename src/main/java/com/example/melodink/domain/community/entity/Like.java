package com.example.melodink.domain.community.entity;

import com.example.melodink.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Like는 관계 데이터로 외부 단독 노출 없음
 * bigserial PK만 사용
 *
 * target_id 타입 변경:
 * - 기존 UUID → Long (bigserial PK로 통일했으므로)
 * - POST, COMMENT, PORTFOLIO_WORK 모두 Long PK를 가짐
 */
@Entity
@Table(
        name = "likes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "target_id", "target_type"})
        },
        indexes = {
                @Index(name = "idx_likes_user_id", columnList = "user_id"),
                @Index(name = "idx_likes_target", columnList = "target_id, target_type")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // bigserial PK로 통일했으므로 Long으로 참조
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Like(User user, Long targetId, TargetType targetType) {
        this.user = user;
        this.targetId = targetId;
        this.targetType = targetType;
    }

    public enum TargetType {
        POST, COMMENT, PORTFOLIO_WORK
    }
}