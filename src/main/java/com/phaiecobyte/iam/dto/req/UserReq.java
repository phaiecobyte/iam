package com.phaiecobyte.iam.dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserReq {
    private String email;
    private String firstName;
    private String lastName;
    private String displayName;
    private String status;
    private String createdBy;
    private String updatedBy;
}
