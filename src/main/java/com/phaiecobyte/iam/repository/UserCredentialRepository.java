package com.phaiecobyte.iam.repository;

import com.phaiecobyte.iam.model.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential,Long> {
}
