package com.example.melodink.domain.user.entity;

import com.example.melodink.domain.artist.entity.ArtistProfile;
import com.example.melodink.domain.community.entity.Post;
import com.example.melodink.domain.job.entity.JobApplication;
import com.example.melodink.domain.job.entity.JobPosting;
import com.example.melodink.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "nickname"),
                @UniqueConstraint(columnNames = "public_id")
        },
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_public_id", columnList = "public_id")
        })
public class User extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            columnDefinition = "varchar(255) default 'LOCAL' check (provider in ('LOCAL','GOOGLE','KAKAO','NAVER'))"
    )
    @Builder.Default
    private ProviderType provider = ProviderType.LOCAL;

    void ensureProvider() {
        if (provider == null) provider = ProviderType.LOCAL;
    }

    @Column(name = "provider_id")
    private String providerId;

    private String name;

    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;
    private String password;

    @Column(nullable = true)
    private String pictureUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    private LocalDateTime deletedAt;       // 탈퇴 시각
    private LocalDateTime retentionUntil;  // 보관 만료 시각

    @Column(nullable = false)
    @Builder.Default
    private long tokenVersion = 0L;        // 토큰 버전(즉시 무효화용)

    @Column(unique = true)
    private String verifiedEmail;

    private LocalDateTime emailVerifiedAt; // 이메일 검증 시각

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ArtistProfile artistProfile;


    @Builder.Default        // NPE 방지를 위해 적용
    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "director")
    private List<JobPosting> jobPostings = new ArrayList<>();
//
//    @Builder.Default
//    @OneToMany(mappedBy = "member")
//    private List<Comment> comments = new ArrayList<>();
//
//    @Builder.Default
//    @OneToMany(mappedBy = "member")
//    private List<CommentLike> commentLikes = new ArrayList<>();
//
//    @Builder.Default
//    @OneToMany(mappedBy = "member")
//    private List<CommentReport> commentReports = new ArrayList<>();
//
//    @Builder.Default
//    @OneToMany(mappedBy = "member")
//    private List<PaymentOrder> paymentOrders = new ArrayList<>();
//
//    @Builder.Default
//    @OneToMany(mappedBy = "member")
//    private List<Participant> participant = new ArrayList<>();

    // == 팩토리 ==
    public static User ofLocal(String name, String email, String passwordHash) {
        User m = new User();
        m.provider = ProviderType.LOCAL;
        m.providerId = null;
        m.name = name;
        m.email = email != null ? email.toLowerCase() : null;
        m.password = passwordHash;
        return m;
    }

    public static User ofSocial(ProviderType provider, String providerId,
                                  String name, String email, String pictureUrl) {
        User m = new User();
        m.provider = provider;                 // GOOGLE/KAKAO/...
        m.providerId = providerId;
        m.name = name;
        m.nickname = name;
        m.email = email != null ? email.toLowerCase() : null;
        m.pictureUrl = pictureUrl;
        // createdAt은 JPA Auditing이 자동으로 설정
        return m;
    }

    // == 소셜 동기화 ==
    public User update(String name, String email, String profileUrl) {
        if (name != null) this.name = name;
        if (email != null) this.email = email.toLowerCase();
        if (profileUrl != null) this.pictureUrl = profileUrl;

        return this;
    }

    public void linkSocial(ProviderType provider, String providerId) {
        this.setProvider(provider);
        this.setProviderId(providerId);
    }

    public void changeRole(Role role) {
        this.role = role;
    }
}
