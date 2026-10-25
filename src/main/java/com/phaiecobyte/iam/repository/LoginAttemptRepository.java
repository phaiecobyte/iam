package com.phaiecobyte.iam.repository;

import com.phaiecobyte.iam.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt,Long> {
}
