package com.phaiecobyte.iam.controller;

import com.phaiecobyte.iam.dto.req.AppReq;
import com.phaiecobyte.iam.service.OauthClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/oauth-clients")
@RequiredArgsConstructor
public class OauthClientController {
    private final OauthClientService oauthClientService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('system.oauth_clients.manage')")
    public ResponseEntity<Object> register(@RequestBody AppReq req){
        return ResponseEntity.ok(oauthClientService.register(req));
    }
}
