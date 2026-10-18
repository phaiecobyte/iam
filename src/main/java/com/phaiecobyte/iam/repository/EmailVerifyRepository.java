package com.phaiecobyte.iam.repository;

import com.phaiecobyte.iam.model.EmailVerification;
import com.phaiecobyte.iam.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerifyRepository extends JpaRepository<EmailVerification,Long> {
    Optional<EmailVerification> findByTokenAndVerifiedAtIsNull(String token);
    List<EmailVerification> findAllByUserAndVerifiedAtIsNull(User user);
}
