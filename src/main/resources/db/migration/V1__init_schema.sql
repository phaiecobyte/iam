-- ============================================================
-- IAM DATABASE
-- V1__iam_schema.sql
--
-- PostgreSQL
-- Internal PK : BIGINT
-- External ID : UUID
-- ============================================================


-- ============================================================
-- 01. USERS
-- ============================================================
CREATE TABLE users
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid              UUID         NOT NULL DEFAULT gen_random_uuid(),
    email             VARCHAR(320) NOT NULL,
    email_normalized  VARCHAR(320) NOT NULL,
    first_name        VARCHAR(100),
    last_name         VARCHAR(100),
    display_name      VARCHAR(200),
    status            VARCHAR(30)  NOT NULL DEFAULT 'A',
    email_verified_at TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by        VARCHAR(30),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by        VARCHAR(30),

    CONSTRAINT uq_users_uuid UNIQUE (uuid),
    CONSTRAINT uq_users_email UNIQUE (email_normalized),
    CONSTRAINT ck_users_status CHECK (status IN ('P', 'A', 'S', 'L', 'D'))
);


-- ============================================================
-- 02. USER CREDENTIALS
-- ============================================================

CREATE TABLE user_credentials
(
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid                UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id             BIGINT      NOT NULL,
    password            VARCHAR(500),
    password_changed_at TIMESTAMPTZ,
    failed_attempts     INTEGER     NOT NULL DEFAULT 0,
    locked_until        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_user_credentials_uuid UNIQUE (uuid),
    CONSTRAINT uq_user_credentials_user UNIQUE (user_id),
    CONSTRAINT fk_user_credentials_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);


-- ============================================================
-- 03. EXTERNAL IDENTITIES
-- ============================================================

CREATE TABLE external_identities
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid              UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id           BIGINT       NOT NULL,
    provider          VARCHAR(50)  NOT NULL,
    provider_subject  VARCHAR(500) NOT NULL,
    email_at_provider VARCHAR(320),
    profile_data      JSONB,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_external_identities_uuid UNIQUE (uuid),
    CONSTRAINT uq_external_identity_provider_subject UNIQUE (provider, provider_subject),
    CONSTRAINT fk_external_identity_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);


-- ============================================================
-- 04. USER SESSIONS
-- ============================================================

CREATE TABLE user_sessions
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid          UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id       BIGINT       NOT NULL,
    session_token VARCHAR(500) NOT NULL,
    ip_address    INET,
    user_agent    TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_seen_at  TIMESTAMPTZ,
    expires_at    TIMESTAMPTZ  NOT NULL,
    revoked_at    TIMESTAMPTZ,

    CONSTRAINT uq_user_sessions_uuid UNIQUE (uuid),
    CONSTRAINT uq_user_sessions_token UNIQUE (session_token),
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);


-- ============================================================
-- 05. ORGANIZATIONS
-- ============================================================

CREATE TABLE organizations
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid       UUID         NOT NULL DEFAULT gen_random_uuid(),
    name       VARCHAR(200) NOT NULL,
    slug       VARCHAR(100) NOT NULL,
    status     VARCHAR(30)  NOT NULL DEFAULT 'A',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_organizations_uuid UNIQUE (uuid),
    CONSTRAINT uq_organizations_slug UNIQUE (slug),
    CONSTRAINT ck_organizations_status CHECK (status IN ('A', 'S', 'D'))
);


-- ============================================================
-- 06. ORGANIZATION SETTINGS
-- ============================================================

CREATE TABLE organization_settings
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid            UUID        NOT NULL DEFAULT gen_random_uuid(),
    organization_id BIGINT      NOT NULL,
    settings        JSONB       NOT NULL DEFAULT '{}'::jsonb,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_organization_settings_uuid UNIQUE (uuid),
    CONSTRAINT uq_organization_settings_org UNIQUE (organization_id),
    CONSTRAINT fk_organization_settings_org FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE
);


-- ============================================================
-- 07. ORGANIZATION MEMBERSHIPS
-- ============================================================

CREATE TABLE organization_memberships
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid            UUID        NOT NULL DEFAULT gen_random_uuid(),
    organization_id BIGINT      NOT NULL,
    user_id         BIGINT      NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'A',
    joined_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_membership_uuid UNIQUE (uuid),
    CONSTRAINT uq_membership_org_user UNIQUE (organization_id, user_id),
    CONSTRAINT fk_membership_org FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE,
    CONSTRAINT fk_membership_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);


-- ============================================================
-- 08. OAUTH SCOPES
--
-- Created BEFORE role_scopes and oauth_client_scopes because
-- those tables reference this table.
-- ============================================================

CREATE TABLE oauth_scopes
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID         NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    is_system   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_oauth_scopes_uuid UNIQUE (uuid),
    CONSTRAINT uq_oauth_scopes_name UNIQUE (name)
);


-- ============================================================
-- 09. ROLES
-- ============================================================

CREATE TABLE roles
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid            UUID         NOT NULL DEFAULT gen_random_uuid(),
    organization_id BIGINT,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    is_system       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_roles_uuid UNIQUE (uuid),
    CONSTRAINT uq_roles_organization_name UNIQUE (organization_id, name),
    CONSTRAINT fk_roles_organization FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE
);


-- ============================================================
-- 10. PERMISSIONS
-- ============================================================

CREATE TABLE permissions
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID         NOT NULL DEFAULT gen_random_uuid(),
    code        VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_permissions_uuid UNIQUE (uuid),
    CONSTRAINT uq_permissions_code UNIQUE (code)
);


-- ============================================================
-- 11. MEMBERSHIP ROLES
-- ============================================================

CREATE TABLE membership_roles
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid          UUID        NOT NULL DEFAULT gen_random_uuid(),
    membership_id BIGINT      NOT NULL,
    role_id       BIGINT      NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_membership_roles_uuid UNIQUE (uuid),
    CONSTRAINT uq_membership_role UNIQUE (membership_id, role_id),
    CONSTRAINT fk_membership_roles_membership FOREIGN KEY (membership_id) REFERENCES organization_memberships (id) ON DELETE CASCADE,
    CONSTRAINT fk_membership_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);


-- ============================================================
-- 12. ROLE PERMISSIONS
-- ============================================================

CREATE TABLE role_permissions
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid          UUID        NOT NULL DEFAULT gen_random_uuid(),
    role_id       BIGINT      NOT NULL,
    permission_id BIGINT      NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_role_permissions_uuid UNIQUE (uuid),
    CONSTRAINT uq_role_permission UNIQUE (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);


-- ============================================================
-- 13. ROLE SCOPES
-- ============================================================

CREATE TABLE role_scopes
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid       UUID        NOT NULL DEFAULT gen_random_uuid(),
    role_id    BIGINT      NOT NULL,
    scope_id   BIGINT      NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_role_scopes_uuid UNIQUE (uuid),
    CONSTRAINT uq_role_scope UNIQUE (role_id, scope_id),
    CONSTRAINT fk_role_scopes_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_scopes_scope FOREIGN KEY (scope_id) REFERENCES oauth_scopes (id) ON DELETE CASCADE
);


-- ============================================================
-- 14. OAUTH CLIENTS
-- ============================================================

CREATE TABLE oauth_clients
(
    id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid                     UUID         NOT NULL DEFAULT gen_random_uuid(),

    client_id                VARCHAR(200) NOT NULL,
    client_secret            VARCHAR(500),

    client_name              VARCHAR(200) NOT NULL,

    client_type              VARCHAR(30)  NOT NULL,

    client_auth_methods      TEXT         NOT NULL,
    grant_types              TEXT         NOT NULL,

    scopes                   TEXT         NOT NULL,

    client_settings          JSONB        NOT NULL DEFAULT '{}'::jsonb,
    token_settings           JSONB        NOT NULL DEFAULT '{}'::jsonb,

    client_id_issued_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    client_secret_expires_at TIMESTAMPTZ,

    created_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_oauth_clients_uuid
        UNIQUE (uuid),

    CONSTRAINT uq_oauth_clients_client_id
        UNIQUE (client_id)
);


-- ============================================================
-- 15. OAUTH CLIENT REDIRECT URIS
-- ============================================================

CREATE TABLE oauth_client_redirect_uris
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid         UUID        NOT NULL DEFAULT gen_random_uuid(),

    client_id    BIGINT      NOT NULL,
    redirect_uri TEXT        NOT NULL,

    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_oauth_client_redirect_uuid
        UNIQUE (uuid),

    CONSTRAINT uq_oauth_client_redirect
        UNIQUE (client_id, redirect_uri),

    CONSTRAINT fk_oauth_client_redirect_client
        FOREIGN KEY (client_id)
            REFERENCES oauth_clients (id)
            ON DELETE CASCADE
);


-- ============================================================
-- 16. OAUTH CLIENT SCOPES
-- ============================================================

CREATE TABLE oauth_client_scopes
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid       UUID        NOT NULL DEFAULT gen_random_uuid(),
    client_id  BIGINT      NOT NULL,
    scope_id   BIGINT      NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_oauth_client_scope_uuid UNIQUE (uuid),
    CONSTRAINT uq_oauth_client_scope UNIQUE (client_id, scope_id),
    CONSTRAINT fk_oauth_client_scope_client FOREIGN KEY (client_id) REFERENCES oauth_clients (id) ON DELETE CASCADE,
    CONSTRAINT fk_oauth_client_scope_scope FOREIGN KEY (scope_id) REFERENCES oauth_scopes (id) ON DELETE CASCADE
);


-- ============================================================
-- 17. OAUTH AUTHORIZATIONS
-- ============================================================

CREATE TABLE oauth_authorizations
(
    id                            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid                          UUID         NOT NULL DEFAULT gen_random_uuid(),
    registered_client_id          BIGINT       NOT NULL,
    principal_name                VARCHAR(500) NOT NULL,
    authorization_grant_type      VARCHAR(100) NOT NULL,
    authorized_scopes             TEXT,
    authorization_code_value      TEXT,
    authorization_code_issued_at  TIMESTAMPTZ,
    authorization_code_expires_at TIMESTAMPTZ,
    authorization_code_metadata   JSONB,
    access_token_value            TEXT,
    access_token_issued_at        TIMESTAMPTZ,
    access_token_expires_at       TIMESTAMPTZ,
    access_token_metadata         JSONB,
    access_token_type             VARCHAR(100),
    access_token_scopes           TEXT,
    oidc_id_token_value           TEXT,
    oidc_id_token_issued_at       TIMESTAMPTZ,
    oidc_id_token_expires_at      TIMESTAMPTZ,
    oidc_id_token_metadata        JSONB,
    refresh_token_value           TEXT,
    refresh_token_issued_at       TIMESTAMPTZ,
    refresh_token_expires_at      TIMESTAMPTZ,
    refresh_token_metadata        JSONB,
    attributes                    JSONB,
    created_at                    TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_oauth_authorizations_uuid UNIQUE (uuid),
    CONSTRAINT fk_oauth_authorizations_client FOREIGN KEY (registered_client_id) REFERENCES oauth_clients (id) ON DELETE CASCADE
);


-- ============================================================
-- 18. OAUTH CONSENTS
-- ============================================================

CREATE TABLE oauth_consents
(
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid                 UUID         NOT NULL DEFAULT gen_random_uuid(),
    registered_client_id BIGINT       NOT NULL,
    principal_name       VARCHAR(500) NOT NULL,
    authorities          TEXT         NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_oauth_consents_uuid UNIQUE (uuid),
    CONSTRAINT uq_oauth_consent_client_principal UNIQUE (registered_client_id, principal_name),
    CONSTRAINT fk_oauth_consents_client FOREIGN KEY (registered_client_id) REFERENCES oauth_clients (id) ON DELETE CASCADE
);


-- ============================================================
-- 19. SIGNING KEYS
-- ============================================================

CREATE TABLE signing_keys
(
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    key_id                VARCHAR(200) NOT NULL,
    algorithm             VARCHAR(50)  NOT NULL,
    public_key            TEXT         NOT NULL,
    encrypted_private_key TEXT         NOT NULL,
    status                VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    valid_from            TIMESTAMPTZ  NOT NULL,
    valid_until           TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_signing_keys_uuid UNIQUE (uuid),
    CONSTRAINT uq_signing_keys_key_id UNIQUE (key_id),
    CONSTRAINT ck_signing_keys_status CHECK (status IN ('ACTIVE','RETIRING','RETIRED'))
);


-- ============================================================
-- 20. AUDIT EVENTS
-- ============================================================

CREATE TABLE audit_events
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid            UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id         BIGINT,
    organization_id BIGINT,
    event_type      VARCHAR(100) NOT NULL,
    resource_type   VARCHAR(100),
    resource_uuid   UUID,
    ip_address      INET,
    user_agent      TEXT,
    metadata        JSONB,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_audit_events_uuid UNIQUE (uuid),
    CONSTRAINT fk_audit_events_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_events_organization FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE SET NULL
);


-- ============================================================
-- 21. SECURITY EVENTS
-- ============================================================

CREATE TABLE security_events
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid       UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id    BIGINT,
    event_type VARCHAR(100) NOT NULL,
    severity   VARCHAR(30)  NOT NULL DEFAULT 'I',
    ip_address INET,
    user_agent TEXT,
    details    JSONB,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_security_events_uuid UNIQUE (uuid),
    CONSTRAINT fk_security_events_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_security_events_severity CHECK (severity IN ('I', 'W', 'E', 'C'))
);


-- ============================================================
-- 22. EMAIL VERIFICATIONS
-- ============================================================

CREATE TABLE email_verifications
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id     BIGINT       NOT NULL,
    token  VARCHAR(500) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    verified_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_email_verifications_uuid UNIQUE (uuid),
    CONSTRAINT uq_email_verifications_token UNIQUE (token),
    CONSTRAINT fk_email_verifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);


-- ============================================================
-- 23. PASSWORD RESETS
-- ============================================================

CREATE TABLE password_resets
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid       UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id    BIGINT       NOT NULL,
    token_hash VARCHAR(500) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_password_resets_uuid UNIQUE (uuid),
    CONSTRAINT uq_password_resets_token UNIQUE (token_hash),
    CONSTRAINT fk_password_resets_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);


-- ============================================================
-- 24. MFA CREDENTIALS
-- ============================================================

CREATE TABLE mfa_credentials
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid             UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id          BIGINT      NOT NULL,
    type             VARCHAR(30) NOT NULL,
    secret_encrypted TEXT,
    credential_data  JSONB,
    verified_at      TIMESTAMPTZ,
    last_used_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_mfa_credentials_uuid UNIQUE (uuid),
    CONSTRAINT fk_mfa_credentials_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_mfa_credentials_type CHECK (type IN ('TOTP','WEBAUTHN','RECOVERY_CODE'))
);


-- ============================================================
-- 25. LOGIN ATTEMPTS
-- ============================================================

CREATE TABLE login_attempts
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid           UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id        BIGINT,
    identifier     VARCHAR(320),
    provider       VARCHAR(50),
    success        BOOLEAN     NOT NULL,
    ip_address     INET,
    user_agent     TEXT,
    failure_reason VARCHAR(200),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_login_attempts_uuid UNIQUE (uuid),
    CONSTRAINT fk_login_attempts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);


-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_external_identities_user ON external_identities (user_id);
CREATE INDEX idx_user_sessions_user ON user_sessions (user_id);
CREATE INDEX idx_user_sessions_expires ON user_sessions (expires_at);
CREATE INDEX idx_memberships_user ON organization_memberships (user_id);
CREATE INDEX idx_memberships_org ON organization_memberships (organization_id);
CREATE INDEX idx_roles_org ON roles (organization_id);
CREATE INDEX idx_membership_roles_membership ON membership_roles (membership_id);
CREATE INDEX idx_membership_roles_role ON membership_roles (role_id);
CREATE INDEX idx_role_permissions_role ON role_permissions (role_id);
CREATE INDEX idx_role_permissions_permission ON role_permissions (permission_id);
CREATE INDEX idx_role_scopes_role ON role_scopes (role_id);
CREATE INDEX idx_role_scopes_scope ON role_scopes (scope_id);
CREATE INDEX idx_oauth_client_redirect_client ON oauth_client_redirect_uris (client_id);
CREATE INDEX idx_oauth_client_scopes_client ON oauth_client_scopes (client_id);
CREATE INDEX idx_oauth_client_scopes_scope ON oauth_client_scopes (scope_id);
CREATE INDEX idx_oauth_authorizations_client ON oauth_authorizations (registered_client_id);
CREATE INDEX idx_oauth_authorizations_principal ON oauth_authorizations (principal_name);
CREATE INDEX idx_oauth_authorizations_access_token ON oauth_authorizations (access_token_value);
CREATE INDEX idx_oauth_authorizations_refresh_token ON oauth_authorizations (refresh_token_value);
CREATE INDEX idx_oauth_authorizations_code ON oauth_authorizations (authorization_code_value);
CREATE INDEX idx_audit_events_user ON audit_events (user_id);
CREATE INDEX idx_audit_events_organization ON audit_events (organization_id);
CREATE INDEX idx_audit_events_created ON audit_events (created_at);
CREATE INDEX idx_security_events_user ON security_events (user_id);
CREATE INDEX idx_security_events_created ON security_events (created_at);
CREATE INDEX idx_login_attempts_user ON login_attempts (user_id);
CREATE INDEX idx_login_attempts_identifier ON login_attempts (identifier);
CREATE INDEX idx_login_attempts_created ON login_attempts (created_at);
CREATE INDEX idx_email_verifications_user ON email_verifications (user_id);
CREATE INDEX idx_password_resets_user ON password_resets (user_id);
CREATE INDEX idx_mfa_credentials_user ON mfa_credentials (user_id);


-- ============================================================
-- PARTIAL INDEXES
-- ============================================================
CREATE UNIQUE INDEX uq_active_signing_key ON signing_keys (status) WHERE status = 'A';
CREATE INDEX idx_active_sessions ON user_sessions (user_id, expires_at) WHERE revoked_at IS NULL;
CREATE INDEX idx_active_password_resets ON password_resets (user_id, expires_at) WHERE used_at IS NULL;
CREATE INDEX idx_active_email_verifications ON email_verifications (user_id, expires_at) WHERE verified_at IS NULL;