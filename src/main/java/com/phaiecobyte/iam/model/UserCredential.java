package com.phaiecobyte.iam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_credentials")
@EntityListeners(AuditingEntityListener.class)
public class UserCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @UuidGenerator
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "password", length = 500)
    private String password;

    @Column(name = "password_changed_at")
    private OffsetDateTime passwordChangedAt;

    @ColumnDefault("0")
    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @CreatedDate
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


}