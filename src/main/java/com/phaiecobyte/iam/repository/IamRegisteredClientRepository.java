
package com.phaiecobyte.iam.repository;

import com.phaiecobyte.iam.model.OauthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class IamRegisteredClientRepository implements RegisteredClientRepository {

    private final OauthClientRepository oauthClientRepository;
    private final OauthClientRedirectUriRepository redirectUriRepository;

    @Override
    public void save(RegisteredClient registeredClient) {
        // Client registration is managed by OauthClientService.
        // Dynamic OAuth client registration is not supported.
        throw new UnsupportedOperationException(
                "OAuth client registration must use IAM client management"
        );
    }

    @Override
    public RegisteredClient findById(String id) {
        if (!StringUtils.hasText(id)) {
            return null;
        }

        try {
            Long clientId = Long.valueOf(id);

            return oauthClientRepository.findById(clientId)
                    .map(this::toRegisteredClient)
                    .orElse(null);

        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {

        if (!StringUtils.hasText(clientId)) {
            return null;
        }

        return oauthClientRepository.findByClientId(clientId)
                .filter(this::isEligibleForAuthentication)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    private boolean isEligibleForAuthentication(OauthClient client) {

        if (!"A".equals(client.getStatus())) {
            return false;
        }

        if ("PUBLIC".equals(client.getClientType())) {
            return client.getClientSecret() == null;
        }

        if (!"CONFIDENTIAL".equals(client.getClientType())) {
            return false;
        }

        if (!StringUtils.hasText(client.getClientSecret())) {
            return false;
        }

        LocalDateTime expiresAt = client.getClientSecretExpiresAt();

        return expiresAt == null || expiresAt.isAfter(LocalDateTime.now());
    }

    private RegisteredClient toRegisteredClient(OauthClient client) {

        RegisteredClient.Builder builder = RegisteredClient
                .withId(client.getId().toString())
                .clientId(client.getClientId())
                .clientName(client.getClientName());

        if (client.getClientIdIssuedAt() != null) {
            builder.clientIdIssuedAt(
                    client.getClientIdIssuedAt()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            );
        }

        if (client.getClientSecret() != null) {
            builder.clientSecret(client.getClientSecret());
        }

        if (client.getClientSecretExpiresAt() != null) {
            builder.clientSecretExpiresAt(
                    client.getClientSecretExpiresAt()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            );
        }

        // Client authentication methods
        parseValues(client.getClientAuthMethods())
                .forEach(method -> builder.clientAuthenticationMethod(
                        switch (method) {
                            case "client_secret_basic" ->
                                    ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
                            case "none" ->
                                    ClientAuthenticationMethod.NONE;
                            default -> throw new IllegalStateException(
                                    "Unsupported client authentication method: " + method
                            );
                        }
                ));

        // Authorization grant types
        parseValues(client.getGrantTypes())
                .forEach(grant -> builder.authorizationGrantType(
                        switch (grant) {
                            case "authorization_code" ->
                                    AuthorizationGrantType.AUTHORIZATION_CODE;
                            case "client_credentials" ->
                                    AuthorizationGrantType.CLIENT_CREDENTIALS;
                            case "refresh_token" ->
                                    AuthorizationGrantType.REFRESH_TOKEN;
                            default -> throw new IllegalStateException(
                                    "Unsupported grant type: " + grant
                            );
                        }
                ));

        // Scopes
        parseValues(client.getScopes())
                .forEach(builder::scope);

        // Redirect URIs
        redirectUriRepository
                .findAllByClient_ClientId(client.getClientId())
                .forEach(uri -> builder.redirectUri(uri.getRedirectUri()));

        // Client settings
        Map<String, Object> clientSettings =
                client.getClientSettings() != null
                        ? client.getClientSettings()
                        : Map.of();

        builder.clientSettings(
                ClientSettings.withSettings(clientSettings)
                        .requireProofKey(
                                "PUBLIC".equals(client.getClientType())
                        )
                        .build()
        );

        // Token settings
        Map<String, Object> tokenSettings =
                client.getTokenSettings() != null
                        ? client.getTokenSettings()
                        : Map.of();

        builder.tokenSettings(
                TokenSettings.withSettings(tokenSettings).build()
        );

        return builder.build();
    }

    private Set<String> parseValues(String values) {

        if (!StringUtils.hasText(values)) {
            return Set.of();
        }

        return Arrays.stream(values.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(LinkedHashSet::new,
                        LinkedHashSet::add,
                        LinkedHashSet::addAll);
    }
}
