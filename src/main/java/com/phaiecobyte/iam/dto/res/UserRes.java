package com.phaiecobyte.iam.dto.res;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
public class UserRes {
    private UUID uuid;
    private String email;
    private String firstName;
    private String lastName;
    private String displayName;
    private String status;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
