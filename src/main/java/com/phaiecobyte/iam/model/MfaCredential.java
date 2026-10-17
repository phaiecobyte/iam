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
@Table(name = "mfa_credentials")
public class MfaCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ColumnDefault("gen_random_uuid()")
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "secret_encrypted", length = Integer.MAX_VALUE)
    private String secretEncrypted;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "credential_data")
    private Map<String, Object> credentialData;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;


}