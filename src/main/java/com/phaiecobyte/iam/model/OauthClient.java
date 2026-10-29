package com.phaiecobyte.iam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter @Setter
@Entity @Table(name = "oauth_clients")
@EntityListeners(AuditingEntityListener.class)
public class OauthClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ColumnDefault("gen_random_uuid()")
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Column(name = "client_id", nullable = false, length = 200)
    private String clientId;

    @Column(name = "client_secret", length = 500)
    private String clientSecret;

    @Column(name = "client_name", nullable = false, length = 200)
    private String clientName;

    @Column(name = "client_type", nullable = false, length = 30)
    private String clientType;

    @Column(name = "client_auth_methods", nullable = false, length = Integer.MAX_VALUE)
    private String clientAuthMethods;

    @Column(name = "grant_types", nullable = false, length = Integer.MAX_VALUE)
    private String grantTypes;

    @Column(name = "scopes", nullable = false, length = Integer.MAX_VALUE)
    private String scopes;

    private String status = "A";

    @ColumnDefault("'{}'")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "client_settings", nullable = false)
    private Map<String, Object> clientSettings;

    @ColumnDefault("'{}'")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "token_settings", nullable = false)
    private Map<String, Object> tokenSettings;

    @ColumnDefault("now()")
    @Column(name = "client_id_issued_at", nullable = false)
    private LocalDateTime clientIdIssuedAt;

    @Column(name = "client_secret_expires_at")
    private LocalDateTime clientSecretExpiresAt;

    @CreatedDate
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    @LastModifiedDate
    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "application_id")
    private Application application;


}