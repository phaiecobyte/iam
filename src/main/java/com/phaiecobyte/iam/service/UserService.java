package com.phaiecobyte.iam.service;

import com.phaiecobyte.iam.constant.LocalConstant;
import com.phaiecobyte.iam.dto.req.UserReq;
import com.phaiecobyte.iam.dto.res.UserRes;
import com.phaiecobyte.iam.exception.BusinessException;
import com.phaiecobyte.iam.exception.ErrorCode;
import com.phaiecobyte.iam.mapper.UserMapper;
import com.phaiecobyte.iam.model.User;
import com.phaiecobyte.iam.model.UserCredential;
import com.phaiecobyte.iam.repository.UserCredentialRepository;
import com.phaiecobyte.iam.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailVerifyService emailVerifyService;
    private final PasswordEncoder passwordEncoder;
    private final UserCredentialRepository userCredentialRepository;


    @Transactional
    public UserRes create(UserReq req){

        log.info("================== Process Create User ================");

        String email = req.getEmail().toLowerCase(Locale.ROOT).strip();

        log.info("Checking email exist or not......!");
        if (userRepository.existsByEmail(email)) {
            log.error("Email already exist......!");
            throw new BusinessException(ErrorCode.CLIENT_0001,"Email already exist!");
        }
        log.info("PASS......!");

        User user = userMapper.toEntity(req);
        user.setUuid(UUID.randomUUID());
        user.setEmail(email);
        user.setStatus(LocalConstant.STATUS_PENDING);
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy("SYS");

        User savedUser = userRepository.save(user);
        log.info("Saved user successfully......!");


        UserCredential credential = new UserCredential();
        credential.setUser(savedUser);
        credential.setFailedAttempts(0);
        credential.setPassword(passwordEncoder.encode(req.getPassword()));

        userCredentialRepository.save(credential);
        log.info("Save credential successfully......!");

        emailVerifyService.createVerificationToken(savedUser);
        log.info("Create verification token successfully...!");

        return userMapper.toRes(savedUser);
    }

}
