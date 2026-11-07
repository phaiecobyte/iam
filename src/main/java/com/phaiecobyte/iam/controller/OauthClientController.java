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

    @PatchMapping("/patch-status/{clientId}")
    @PreAuthorize("hasAnyAuthority('system.oauth_clients.manage')")
    public ResponseEntity<Object> patchOauthClientStatus(@PathVariable(value = "clientId") String clientId, @RequestParam String status){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Update oauth client successfully...!",oauthClientService.patchClientStatus(clientId,status)));
    }
}
