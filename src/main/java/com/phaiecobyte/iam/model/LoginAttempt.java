package com.phaiecobyte.iam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "login_attempts")
@EntityListeners(AuditingEntityListener.class)
public class LoginAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ColumnDefault("gen_random_uuid()")
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Column(name = "identifier", length = 320)
    private String identifier;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "ip_address")
    private InetAddress ipAddress;

    @Column(name = "user_agent", length = Integer.MAX_VALUE)
    private String userAgent;

    @Column(name = "failure_reason", length = 200)
    private String failureReason;

    @CreatedDate
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


}