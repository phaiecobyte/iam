-- ============================================================
-- V2: GLOBAL SYSTEM ADMINISTRATORS
-- ============================================================

-- organization_id IS NULL denotes a platform-wide role. Keep built-in
-- global roles unique by name while allowing tenant roles to use the same name.
CREATE UNIQUE INDEX uq_global_system_role_name
    ON roles (name)
    WHERE organization_id IS NULL AND is_system = TRUE;


-- Global roles are assigned directly to users. Organization roles continue
-- to be assigned through membership_roles.
CREATE TABLE user_roles
(
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid                UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id             BIGINT      NOT NULL,
    role_id             BIGINT      NOT NULL,
    assigned_by_user_id BIGINT,
    assigned_at         TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_user_roles_uuid UNIQUE (uuid),
    CONSTRAINT uq_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id)
        REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_assigned_by FOREIGN KEY (assigned_by_user_id)
        REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_user_roles_role ON user_roles (role_id);


-- user_roles is reserved for platform-wide roles. Enforce that a tenant role
-- cannot accidentally be granted globally.
CREATE FUNCTION iam_v2_enforce_global_user_role() RETURNS TRIGGER AS $$
DECLARE
    v_organization_id BIGINT;
BEGIN
    SELECT organization_id
      INTO v_organization_id
      FROM roles
     WHERE id = NEW.role_id;

    IF FOUND AND v_organization_id IS NOT NULL THEN
        RAISE EXCEPTION 'user_roles can only reference global roles'
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_user_roles_global_role
    BEFORE INSERT OR UPDATE OF role_id ON user_roles
    FOR EACH ROW
    EXECUTE FUNCTION iam_v2_enforce_global_user_role();


-- Do not turn a role into an organization role while it is assigned globally.
CREATE FUNCTION iam_v2_prevent_scoping_assigned_global_role() RETURNS TRIGGER AS $$
BEGIN
    IF OLD.organization_id IS NULL
       AND NEW.organization_id IS NOT NULL
       AND EXISTS (SELECT 1 FROM user_roles WHERE role_id = OLD.id) THEN
        RAISE EXCEPTION 'a globally assigned role cannot become organization-scoped'
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_roles_preserve_global_scope
    BEFORE UPDATE OF organization_id ON roles
    FOR EACH ROW
    EXECUTE FUNCTION iam_v2_prevent_scoping_assigned_global_role();


-- Seed the platform administrator role and its coarse-grained capabilities.
INSERT INTO roles (organization_id, name, description, is_system)
SELECT NULL, 'SYSTEM_ADMIN', 'Platform-wide system administrator', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM roles
     WHERE organization_id IS NULL
       AND name = 'SYSTEM_ADMIN'
       AND is_system = TRUE
);

INSERT INTO permissions (code, description)
VALUES
    ('system.users.read', 'View users across the platform'),
    ('system.users.manage', 'Manage users across the platform'),
    ('system.organizations.read', 'View organizations across the platform'),
    ('system.organizations.manage', 'Manage organizations across the platform'),
    ('system.roles.manage', 'Manage platform-wide role assignments'),
    ('system.audit.read', 'View platform audit events')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
  FROM roles r
  JOIN permissions p ON p.code IN (
      'system.users.read',
      'system.users.manage',
      'system.organizations.read',
      'system.organizations.manage',
      'system.roles.manage',
      'system.audit.read'
  )
 WHERE r.organization_id IS NULL
   AND r.name = 'SYSTEM_ADMIN'
   AND r.is_system = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;
