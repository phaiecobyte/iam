package com.phaiecobyte.iam.service;

import com.phaiecobyte.iam.dto.req.LoginReq;
import com.phaiecobyte.iam.exception.BusinessException;
import com.phaiecobyte.iam.exception.ErrorCode;
import com.phaiecobyte.iam.model.User;
import com.phaiecobyte.iam.model.UserCredential;
import com.phaiecobyte.iam.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.phaiecobyte.iam.constant.LocalConstant.STATUS_ACTIVE;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private static final int MAX_FAILED_ATTEMPT = 5;
    private static final int LOCK_MINUTE = 15;

    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;


    @Transactional
    public Map<String,Object> login(LoginReq req){
        log.info("===================== Process Login ======================");

        log.info("Checking credential......!");
        UserCredential credential = userCredentialRepository.findByUserEmail(req.getEmail())
                .orElseThrow(()-> new BusinessException(ErrorCode.CLIENT_0003));
        log.info("PASS......!");


        log.info("Checking is Account locked......!");
        if(credential.getLockedUntil() != null && credential.getLockedUntil().isAfter(LocalDateTime.now())){
            log.error("Account is locked......!");
            throw new BusinessException(ErrorCode.CLIENT_0004);
        }
        log.info("PASS......!");

        if(credential.getLockedUntil() != null){
            credential.setLockedUntil(null);
            credential.setFailedAttempts(0);
        }

        log.info("Checking password match or not......!");
        if(!passwordEncoder.matches(req.getPassword(),credential.getPassword())){
            log.error("password is not matched......!");

            int attempts = credential.getFailedAttempts() == 0 ? 1 : credential.getFailedAttempts() + 1;
            credential.setFailedAttempts(attempts);

            if(attempts >= MAX_FAILED_ATTEMPT){
                log.error("Failed attempt reach limit....!");
                credential.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTE));
                throw new BusinessException(ErrorCode.CLIENT_0004);
            }
            userCredentialRepository.save(credential);
            throw new BusinessException(ErrorCode.CLIENT_0003);
        }
        log.info("PASS......!");

        User user = credential.getUser();

        log.info("Checking is email verified......!");
        if(user.getEmailVerifiedAt() == null || !STATUS_ACTIVE.equals(user.getStatus())){
            log.error("Email is not verified yet......!");
            throw new BusinessException(ErrorCode.CLIENT_0005);
        }
        log.info("PASS......!");

        credential.setFailedAttempts(0);
        credential.setLockedUntil(null);
        userCredentialRepository.save(credential);

        Instant issuedAt = Instant.now();
        Instant expiredAt = Instant.now().plusMillis(900000); // 15 min

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("iam")
                .subject(user.getUuid().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiredAt)
                .claim("email",user.getEmail())
                .claim("token_type","access") //Distinguishes access tokens from refresh tokens.
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();

        String accessToken = jwtEncoder
                .encode(JwtEncoderParameters.from(header,claims))
                .getTokenValue();

        Map<String,Object> response = new LinkedHashMap<>();

        response.put("tokenType","Bearer");
        response.put("accessToken",accessToken);
        response.put("expireInSecond", 900000 / 1000);

        return response;
    }

}
