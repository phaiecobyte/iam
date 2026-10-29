package com.phaiecobyte.iam.service;

import com.phaiecobyte.iam.dto.req.AppReq;
import com.phaiecobyte.iam.exception.BusinessException;
import com.phaiecobyte.iam.exception.ErrorCode;
import com.phaiecobyte.iam.model.Application;
import com.phaiecobyte.iam.model.OauthClient;
import com.phaiecobyte.iam.model.OauthClientRedirectUris;
import com.phaiecobyte.iam.repository.ApplicationRepository;
import com.phaiecobyte.iam.repository.OauthClientRedirectUriRepository;
import com.phaiecobyte.iam.repository.OauthClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.sql.BatchUpdateException;
import java.time.LocalDateTime;
import java.util.*;

import static com.phaiecobyte.iam.constant.LocalConstant.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OauthClientService {
    private final OauthClientRepository oauthClientRepository;
    private final ApplicationRepository applicationRepository;
    private final OauthClientRedirectUriRepository oauthClientRedirectUriRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public Map<String,Object> register(AppReq req){
        log.info("=============== PROCESS REGISTER APP ===============");

        log.info("Checking client type......!");
        if(!Set.of("CONFIDENTIAL", "PUBLIC").contains(req.getClientType())){
            log.error("FAILED......!");
            throw new IllegalArgumentException("Invalid client type");
        }
        log.info("PASS");

        log.info("Checking application");
        Application app = applicationRepository.findByCode(req.getAppCode())
                .orElseThrow(()-> new IllegalArgumentException("App not found...!"));

        log.info("Checking app status active or not......!");
        if(!STATUS_ACTIVE.equals(app.getStatus())){
            log.error("FAILED");
            throw new IllegalArgumentException("App is not active......!");
        }
        log.info("PASS");

        validateConfig(req.getClientType(),req.getGrantTypes(),req.getScopes(),req.getRedirectUri());

        String clientId = "iam_" + UUID.randomUUID().toString().replace("-","");
        String rawSecret = null;
        String storedSecret = null;

        if("CONFIDENTIAL".equals(req.getClientType())){
            byte[] secretBytes = new byte[32];
            SECURE_RANDOM.nextBytes(secretBytes);
            rawSecret = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(secretBytes);

            storedSecret = passwordEncoder.encode(rawSecret);
        }

        OauthClient client = new OauthClient();

        client.setUuid(UUID.randomUUID());
        client.setClientId(clientId);
        client.setClientSecret(storedSecret);
        client.setClientName(req.getClientName());
        client.setClientType(req.getClientType());
        client.setApplication(app);
        client.setClientIdIssuedAt(LocalDateTime.now());

        client.setGrantTypes(String.join(",",req.getGrantTypes()));
        client.setScopes(String.join(",",req.getScopes()));

        client.setClientAuthMethods(
                "PUBLIC".equals(req.getClientType())
                        ? "none"
                        : "client_secret_basic"
        );

        client.setClientSettings(new HashMap<>());
        client.setTokenSettings(new HashMap<>());

        OauthClient saved = oauthClientRepository.save(client);

        for (String uri : req.getRedirectUri()) {
            OauthClientRedirectUris redirect =
                    new OauthClientRedirectUris();

            redirect.setUuid(UUID.randomUUID());
            redirect.setClient(saved);
            redirect.setRedirectUri(uri);
            redirect.setCreatedAt(LocalDateTime.now());

            oauthClientRedirectUriRepository.save(redirect);
        }


        Map<String,Object> response = new LinkedHashMap<>();

        response.put("clientId", saved.getClientId());
        response.put("clientName", saved.getClientName());
        response.put("clientType", saved.getClientType());
        response.put("appCode", app.getCode());

        if(rawSecret != null){
            response.put("clientSecret",rawSecret);
        }

        return response;

    }


    @Transactional
    public Map<String,Object> patchClientStatus(String clientId, String status){

        if(!Set.of(STATUS_ACTIVE,STATUS_INACTIVE,STATUS_SUSPEND).contains(status)){
            throw new BusinessException(ErrorCode.CLIENT_0006,"Invalid status client status");
        }

        OauthClient client = oauthClientRepository.findByClientId(clientId)
                .orElseThrow(()->new BusinessException(ErrorCode.CLIENT_0007));


        return null;
    }

    private void validateConfig(
            String clientType,
            List<String> grantTypes,
            List<String> scopes,
            List<String> redirectUris
    ){
        if (grantTypes.isEmpty()) {
            throw new BusinessException(ErrorCode.CLIENT_0006,"At least one grant type is required");
        }

        if (scopes.isEmpty()) {
            throw new BusinessException(ErrorCode.CLIENT_0006,"At least one scope is required");
        }

        Set<String> allowedGrants = Set.of(
                "authorization_code",
                "refresh_token",
                "client_credentials"
        );

        if (!allowedGrants.containsAll(grantTypes)) {
            throw new BusinessException(ErrorCode.CLIENT_0006,"Unsupported grant type");
        }

        if (grantTypes.contains("refresh_token") && !grantTypes.contains("authorization_code")) {
            throw new BusinessException(ErrorCode.CLIENT_0006,"Refresh token requires authorization_code");
        }

        if ("PUBLIC".equals(clientType) && grantTypes.contains("client_credentials")) {
            throw new BusinessException(ErrorCode.CLIENT_0006,"Public clients cannot use client_credentials");
        }

        if (grantTypes.contains("authorization_code") && redirectUris.isEmpty()) {
            throw new BusinessException(ErrorCode.CLIENT_0006,"Redirect URI is required for authorization_code");
        }

        if (redirectUris.stream().anyMatch(uri -> uri == null || uri.isBlank())) {
            throw new BusinessException(ErrorCode.CLIENT_0006,"Redirect URI cannot be blank");
        }

        if (new HashSet<>(redirectUris).size() != redirectUris.size()) {
            throw new BusinessException(ErrorCode.CLIENT_0006,"Duplicate redirect URI");
        }

        if (scopes.stream().anyMatch(scope -> scope == null || scope.isBlank())) {
            throw new BusinessException(ErrorCode.CLIENT_0006,"Scope cannot be blank");
        }
    }
}
