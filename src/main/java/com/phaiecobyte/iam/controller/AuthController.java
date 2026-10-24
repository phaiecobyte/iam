package com.phaiecobyte.iam.controller;

import com.phaiecobyte.iam.dto.ApiResponse;
import com.phaiecobyte.iam.dto.req.LoginReq;
import com.phaiecobyte.iam.dto.req.UserReq;
import com.phaiecobyte.iam.dto.res.UserRes;
import com.phaiecobyte.iam.service.AuthService;
import com.phaiecobyte.iam.service.EmailVerifyService;
import com.phaiecobyte.iam.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    private final EmailVerifyService emailVerifyService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRes>> register(@RequestBody @Valid UserReq req){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully...!",userService.create(req)));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        emailVerifyService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@RequestParam String email){
        emailVerifyService.resendVerificationEmail(email);
        String message = "If the account requires verification, a verification email will be sent.";
        return ResponseEntity.ok(ApiResponse.success(message,null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginReq req) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(req)));
    }

}
