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
        if(!Set.of(CLIENT_TYPE_CONFIDENTIAL,CLIENT_TYPE_PUBLIC).contains(req.getClientType())){
            log.error("FAILED......!");
            throw new BusinessException(ErrorCode.CLIENT_0006,"Invalid client type. Valid[PUBLIC,CONFIDENTIAL]");
        }
        log.info("PASS");

        log.info("Checking application");
        Application app = applicationRepository.findByCode(req.getAppCode())
                .orElseThrow(()-> new BusinessException(ErrorCode.CLIENT_0007));

        log.info("Checking app status active or not......!");
        if(!STATUS_ACTIVE.equals(app.getStatus())){
            log.error("FAILED");
            throw new BusinessException(ErrorCode.CLIENT_0006,"App is not active......!");
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
        log.info("========= Process Patch Oauth Client Status ===========");

        log.info("Validate status");
        if(!status.equals(STATUS_ACTIVE) || !status.equals(STATUS_INACTIVE) || !status.equals(STATUS_SUSPEND)){
            throw new BusinessException(ErrorCode.CLIENT_0006,"Invalid oauth client status!,valid status[A,I,S]");
        }
        log.info("PASS...!");

        log.info("Check client exist or not...!");
        OauthClient client = oauthClientRepository.findByClientId(clientId)
                .orElseThrow(()->new BusinessException(ErrorCode.CLIENT_0007));
        log.info("PASS...!");


        client.setStatus(status);
        oauthClientRepository.save(client);

        Map<String,Object> response = new LinkedHashMap<>();
        response.put("clientId",clientId);
        response.put("clientName",client.getClientName());
        response.put("status",client.getStatus());

        log.info("===== Patch Oauth Client Status Successfully...!=======");
        return response;
    }

    @Transactional
    public Map<String,Object> rotateClientSecret(String clientId){
        log.info("============ Process Rotate Client Secret ===========");

        OauthClient client = oauthClientRepository.findByClientId(clientId)
                .orElseThrow(()->new BusinessException(ErrorCode.CLIENT_0007));


        if(!CLIENT_TYPE_CONFIDENTIAL.equals(client.getClientType())){
            throw new BusinessException(ErrorCode.CLIENT_0006,"Only confidential client can have client secrets");
        }

        log.info("Generating secret");
        byte[] secureBytes = new byte[32];
        SECURE_RANDOM.nextBytes(secureBytes);
        String rawSecret = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(secureBytes);

        log.info("Hashing secret");
        String hashSecret = passwordEncoder.encode(rawSecret);

        log.info("Calculate expiration");
        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = client.getSecretValiDays() != null
                        ? issuedAt.plusDays(client.getSecretValiDays())
                        : null;

        client.setClientSecret(hashSecret);
        client.setClientSecretExpiresAt(expiresAt);

        oauthClientRepository.save(client);


        Map<String,Object> response = new LinkedHashMap<>();
        response.put("clientId",clientId);
        response.put("clientName",client.getClientName());
        response.put("clientSecret",rawSecret);
        response.put("secretValiDay",client.getSecretValiDays());
        response.put("clientSecretExpiresAt",client.getClientSecretExpiresAt());

        return response;
    }

    @Transactional
    public Map<String,Object> revokeClientSecret(String clientId){
        log.info("============ Process Revoke Client Secret ===========");

        OauthClient client = oauthClientRepository.findByClientId(clientId)
                .orElseThrow(()->new BusinessException(ErrorCode.CLIENT_0007,"Client:"+clientId+" is not found"));

        if(!CLIENT_TYPE_CONFIDENTIAL.equals(client.getClientType())){
            throw new BusinessException(ErrorCode.CLIENT_0006,"Only client confidential can have client secret");
        }

        if(client.getClientSecret() == null){
            throw new BusinessException(ErrorCode.CLIENT_0006,"Client secret is already revoked");
        }

        client.setClientSecret(null);
        client.setClientSecretExpiresAt(null);

        oauthClientRepository.save(client);

        Map<String,Object> response = new LinkedHashMap<>();
        response.put("clientId", client.getClientId());
        response.put("clientName", client.getClientName());
        response.put("secretRevoked", true);
        response.put("secretValiDays", client.getSecretValiDays());

        return response;
    }

    @Transactional
    public Map<String,Object> configureClientSecretExpiration(String clientId,Integer validityDays){
        log.info("========= Process Config Client Secret Expiration =========");

        OauthClient client = oauthClientRepository.findByClientId(clientId)
                .orElseThrow(()->new BusinessException(ErrorCode.CLIENT_0007,"Client:"+clientId+" is not found!"));

        if(!CLIENT_TYPE_CONFIDENTIAL.equals(client.getClientType())){
            throw new BusinessException(ErrorCode.CLIENT_0006,"Only confidential clients support secret expiration");
        }

        if(validityDays != null && validityDays <= 0){
            throw new BusinessException(ErrorCode.CLIENT_0006,"Secret validity days must be greater then 0");
        }

        client.setSecretValiDays(validityDays);
        oauthClientRepository.save(client);

        Map<String,Object> response = new LinkedHashMap<>();
        response.put("clientId", client.getClientId());
        response.put("clientName", client.getClientName());
        response.put("secretValiDays", client.getSecretValiDays());
        response.put("clientSecretExpiresAt", client.getClientSecretExpiresAt());

        return response;
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
            throw new BusinessException(ErrorCode.CLIENT_0006,"Unsupported grant type. Supported[authorization_code,refresh_token,client_credentials]");
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
