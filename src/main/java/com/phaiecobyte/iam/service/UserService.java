package com.phaiecobyte.iam.service;

import com.phaiecobyte.iam.constant.LocalConstant;
import com.phaiecobyte.iam.dto.req.UserReq;
import com.phaiecobyte.iam.dto.res.UserRes;
import com.phaiecobyte.iam.exception.BusinessException;
import com.phaiecobyte.iam.exception.ErrorCode;
import com.phaiecobyte.iam.mapper.UserMapper;
import com.phaiecobyte.iam.model.User;
import com.phaiecobyte.iam.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


    @Transactional
    public UserRes create(UserReq req){

        log.info("================== Process Create User ================");

        String email = req.getEmail().trim();
        String normalizedEmail = email.toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailNormalized(normalizedEmail)) {
            throw new BusinessException(ErrorCode.CLIENT_0001,"Email already exist!");
        }

        User user = userMapper.toEntity(req);
        user.setUuid(UUID.randomUUID());
        user.setEmail(email);
        user.setEmailNormalized(normalizedEmail);
        user.setStatus(LocalConstant.STATUS_PENDING);
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy("SYS");

        User savedUser = userRepository.save(user);
        emailVerifyService.createVerificationToken(savedUser);

        return userMapper.toRes(savedUser);
    }

}
