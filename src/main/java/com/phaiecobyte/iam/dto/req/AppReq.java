package com.phaiecobyte.iam.dto.req;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class AppReq {
    private String appCode;
    private String clientName;
    private String clientType;
    private List<String> grantTypes;
    private List<String> scopes;
    private List<String> redirectUri;
}
