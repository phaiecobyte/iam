package com.phaiecobyte.iam.repository;

import com.phaiecobyte.iam.model.UserCredential;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential,Long> {
    @EntityGraph(attributePaths = "user")
    Optional<UserCredential> findByUserEmail(String email);
}
