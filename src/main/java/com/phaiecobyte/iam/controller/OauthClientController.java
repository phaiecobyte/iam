package com.phaiecobyte.iam.controller;

import com.phaiecobyte.iam.dto.ApiResponse;
import com.phaiecobyte.iam.dto.req.AppReq;
import com.phaiecobyte.iam.service.OauthClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/oauth-clients")
@RequiredArgsConstructor
public class OauthClientController {
    private final OauthClientService oauthClientService;

    @PostMapping("/register")
    @PreAuthorize("hasAnyAuthority('system.oauth_clients.manage')")
    public ResponseEntity<Object> register(@RequestBody AppReq req){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Register oauth client successfully...!",oauthClientService.register(req)));
    }

    @PatchMapping("/{clientId}/status")
    @PreAuthorize("hasAnyAuthority('system.oauth_clients.manage')")
    public ResponseEntity<Object> patchOauthClientStatus(@PathVariable(value = "clientId") String clientId, @RequestParam String status){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Update oauth client status successfully...!",oauthClientService.patchClientStatus(clientId,status)));
    }

    @PostMapping("/{clientId}/secret/rotate")
    @PreAuthorize("hasAnyAuthority('system.oauth_clients.manage')")
    public ResponseEntity<Object> rotateClientSecret(@PathVariable(value = "clientId") String clientId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Rotate Client Secret Successfully...!",oauthClientService.rotateClientSecret(clientId)));
    }

    @PatchMapping("/{clientId}/secret/revoke")
    @PreAuthorize("hasAnyAuthority('system.oauth_clients.manage')")
    public ResponseEntity<Object> revokeClientSecret(@PathVariable(value = "clientId") String clientId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Revoke Client Secret Successfully...!",oauthClientService.revokeClientSecret(clientId)));
    }

    @PatchMapping("/{clientId}/secret-policy")
    @PreAuthorize("hasAnyAuthority('system.oauth_clients.manage')")
    public ResponseEntity<Object> configSecretValidity(@PathVariable(value = "clientId") String clientId, @RequestParam Integer validityDays){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Configure client secret expiration successfully...!",oauthClientService.configureClientSecretExpiration(clientId,validityDays)));
    }
}
