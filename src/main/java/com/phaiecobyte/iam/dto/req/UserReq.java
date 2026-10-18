package com.phaiecobyte.iam.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserReq {
    @Email @NotBlank
    private String email;
    @NotBlank
    private String password;

    private String firstName;
    private String lastName;
    private String displayName;
    private String status;
    private String createdBy;
    private String updatedBy;
}
