package com.phaiecobyte.iam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "oauth_authorizations")
public class OauthAuthorization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ColumnDefault("gen_random_uuid()")
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Column(name = "principal_name", nullable = false, length = 500)
    private String principalName;

    @Column(name = "authorization_grant_type", nullable = false, length = 100)
    private String authorizationGrantType;

    @Column(name = "authorized_scopes", length = Integer.MAX_VALUE)
    private String authorizedScopes;

    @Column(name = "authorization_code_value", length = Integer.MAX_VALUE)
    private String authorizationCodeValue;

    @Column(name = "authorization_code_issued_at")
    private OffsetDateTime authorizationCodeIssuedAt;

    @Column(name = "authorization_code_expires_at")
    private OffsetDateTime authorizationCodeExpiresAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "authorization_code_metadata")
    private Map<String, Object> authorizationCodeMetadata;

    @Column(name = "access_token_value", length = Integer.MAX_VALUE)
    private String accessTokenValue;

    @Column(name = "access_token_issued_at")
    private OffsetDateTime accessTokenIssuedAt;

    @Column(name = "access_token_expires_at")
    private OffsetDateTime accessTokenExpiresAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "access_token_metadata")
    private Map<String, Object> accessTokenMetadata;

    @Column(name = "access_token_type", length = 100)
    private String accessTokenType;

    @Column(name = "access_token_scopes", length = Integer.MAX_VALUE)
    private String accessTokenScopes;

    @Column(name = "oidc_id_token_value", length = Integer.MAX_VALUE)
    private String oidcIdTokenValue;

    @Column(name = "oidc_id_token_issued_at")
    private OffsetDateTime oidcIdTokenIssuedAt;

    @Column(name = "oidc_id_token_expires_at")
    private OffsetDateTime oidcIdTokenExpiresAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "oidc_id_token_metadata")
    private Map<String, Object> oidcIdTokenMetadata;

    @Column(name = "refresh_token_value", length = Integer.MAX_VALUE)
    private String refreshTokenValue;

    @Column(name = "refresh_token_issued_at")
    private OffsetDateTime refreshTokenIssuedAt;

    @Column(name = "refresh_token_expires_at")
    private OffsetDateTime refreshTokenExpiresAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "refresh_token_metadata")
    private Map<String, Object> refreshTokenMetadata;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes")
    private Map<String, Object> attributes;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;


}