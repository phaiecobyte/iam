package com.phaiecobyte.iam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "oauth_clients")
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
    private OffsetDateTime clientIdIssuedAt;

    @Column(name = "client_secret_expires_at")
    private OffsetDateTime clientSecretExpiresAt;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "application_id")
    private Application application;


}