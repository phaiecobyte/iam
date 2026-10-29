package com.phaiecobyte.iam.repository;

import com.phaiecobyte.iam.model.OauthClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OauthClientRepository extends JpaRepository<OauthClient,Long> {
    Optional<OauthClient> findByClientId(String clientId);
    boolean existsByClientId(String clientId);
}
