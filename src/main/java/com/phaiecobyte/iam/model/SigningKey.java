package com.phaiecobyte.iam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "signing_keys")
public class SigningKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ColumnDefault("gen_random_uuid()")
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Column(name = "key_id", nullable = false, length = 200)
    private String keyId;

    @Column(name = "algorithm", nullable = false, length = 50)
    private String algorithm;

    @Column(name = "public_key", nullable = false, length = Integer.MAX_VALUE)
    private String publicKey;

    @Column(name = "encrypted_private_key", nullable = false, length = Integer.MAX_VALUE)
    private String encryptedPrivateKey;

    @ColumnDefault("'A'")
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "valid_from", nullable = false)
    private OffsetDateTime validFrom;

    @Column(name = "valid_until")
    private OffsetDateTime validUntil;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;


}