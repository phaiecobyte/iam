package com.phaiecobyte.iam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ColumnDefault("gen_random_uuid()")
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @ColumnDefault("'A'")
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 30)
    private String createdBy;

    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 30)
    private String updatedBy;


}