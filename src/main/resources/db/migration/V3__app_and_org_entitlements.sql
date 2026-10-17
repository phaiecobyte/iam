-- ============================================================
-- V3: APPLICATION CATALOG AND ORGANIZATION ENTITLEMENTS
-- ============================================================

-- Applications are the products an organization can buy (for example, POS
-- and HR). OAuth clients are registrations used by those products to sign in.
CREATE TABLE applications
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID         NOT NULL DEFAULT gen_random_uuid(),
    code        VARCHAR(100) NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    status      VARCHAR(1)   NOT NULL DEFAULT 'A',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_applications_uuid UNIQUE (uuid),
    CONSTRAINT uq_applications_code UNIQUE (code),
    CONSTRAINT ck_applications_status CHECK (status IN ('A', 'S', 'D'))
);

INSERT INTO applications (code, name, description)
VALUES
    ('POS', 'Point of Sale', 'Point of sale application'),
    ('HR', 'Human Resources', 'Human resources management application')
ON CONFLICT (code) DO NOTHING;


-- An application can have multiple OAuth clients (for example, web and
-- mobile registrations, or separate production and development clients).
ALTER TABLE oauth_clients ADD COLUMN application_id BIGINT;
ALTER TABLE oauth_clients ADD CONSTRAINT fk_oauth_clients_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE RESTRICT;
CREATE INDEX idx_oauth_clients_application ON oauth_clients (application_id);


-- One entitlement per organization and product. Status and dates let the IAM
-- deny access to suspended or expired products without deleting the record.
CREATE TABLE organization_applications
(
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid                UUID        NOT NULL DEFAULT gen_random_uuid(),
    organization_id     BIGINT      NOT NULL,
    application_id      BIGINT      NOT NULL,
    status              VARCHAR(1)  NOT NULL DEFAULT 'A',
    starts_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    ends_at             TIMESTAMPTZ,
    assigned_by_user_id BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_organization_applications_uuid UNIQUE (uuid),
    CONSTRAINT uq_organization_application UNIQUE (organization_id, application_id),
    CONSTRAINT fk_organization_applications_organization FOREIGN KEY (organization_id)
        REFERENCES organizations (id) ON DELETE CASCADE,
    CONSTRAINT fk_organization_applications_application FOREIGN KEY (application_id)
        REFERENCES applications (id) ON DELETE RESTRICT,
    CONSTRAINT fk_organization_applications_assigned_by FOREIGN KEY (assigned_by_user_id)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_organization_applications_status CHECK (status IN ('A', 'S', 'D')),
    CONSTRAINT ck_organization_applications_dates CHECK (ends_at IS NULL OR ends_at > starts_at)
);

CREATE INDEX idx_organization_applications_application
    ON organization_applications (application_id);
