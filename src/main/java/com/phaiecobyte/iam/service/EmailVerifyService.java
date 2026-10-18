package com.phaiecobyte.iam.service;

import com.phaiecobyte.iam.constant.LocalConstant;
import com.phaiecobyte.iam.exception.BusinessException;
import com.phaiecobyte.iam.exception.ErrorCode;
import com.phaiecobyte.iam.model.EmailVerification;
import com.phaiecobyte.iam.model.User;
import com.phaiecobyte.iam.repository.EmailVerifyRepository;
import com.phaiecobyte.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerifyService {
    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    @Value("${app.mail.from}")
    private String fromEmail;

    private final EmailVerifyRepository emailVerifyRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void createVerificationToken(User user) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        LocalDateTime now = LocalDateTime.now();

        EmailVerification verification = new EmailVerification();
        verification.setUuid(UUID.randomUUID());
        verification.setUser(user);
        verification.setToken(hashToken(rawToken));
        verification.setExpiresAt(now.plusMinutes(15));
        verification.setCreatedAt(now);

        emailVerifyRepository.save(verification);

        String verificationUrl = publicBaseUrl
                + "/api/v1/auth/verify-email?token="
                + rawToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Verify your IAM account");
        message.setText(
                "Hello " + (user.getDisplayName() != null
                        ? user.getDisplayName()
                        : user.getEmail())
                        + ",\n\n"
                        + "Please click the link below to verify your email address:\n\n"
                        + verificationUrl
                        + "\n\n"
                        + "This link expires in 15 minutes.\n"
                        + "If you did not register, you can ignore this email."
        );

        mailSender.send(message);
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        log.info("================= process verify email ================");
        if (rawToken == null || rawToken.isBlank()) {
            throw invalidToken();
        }

        String tokenHash = hashToken(rawToken);

        EmailVerification verification = emailVerifyRepository
                .findByTokenAndVerifiedAtIsNull(tokenHash)
                .orElseThrow(this::invalidToken);

        LocalDateTime now = LocalDateTime.now();

        if (!verification.getExpiresAt().isAfter(now)) {
            throw invalidToken();
        }

        User user = verification.getUser();

        verification.setVerifiedAt(now);
        user.setEmailVerifiedAt(now);

        if (LocalConstant.STATUS_PENDING.equals(user.getStatus())) {
            user.setStatus(LocalConstant.STATUS_ACTIVE);
        }


        emailVerifyRepository.save(verification);
        userRepository.save(user);
    }

    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available", exception);
        }
    }

    private BusinessException invalidToken() {
        return new BusinessException(ErrorCode.CLIENT_0002);
    }

    @Transactional
    public void resendVerificationEmail(String email){
        log.info("Process resendVerificationEmail");
        if(email == null || email.isBlank()){
            log.info("Email is null...!");
            return;
        }

        userRepository.findByEmail(email.toLowerCase(Locale.ROOT).strip())
                .filter(user -> LocalConstant.STATUS_PENDING.equals(user.getStatus())
                        && user.getEmailVerifiedAt() == null)
                .ifPresent(user -> {
                    emailVerifyRepository.findAllByUserAndVerifiedAtIsNull(user)
                            .forEach(verify->verify.setExpiresAt(LocalDateTime.now()));
                    emailVerifyRepository.flush(); //ensures the previous token expirations are written before the new token is created.
                    createVerificationToken(user);
                });

    }
}