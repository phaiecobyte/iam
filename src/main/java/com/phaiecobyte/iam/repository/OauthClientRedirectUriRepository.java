package com.phaiecobyte.iam.repository;

import com.phaiecobyte.iam.model.OauthClientRedirectUris;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OauthClientRedirectUriRepository extends JpaRepository<OauthClientRedirectUris,Long> {
}
