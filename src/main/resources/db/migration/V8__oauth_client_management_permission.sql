-- Add the permission for managing OAuth clients.
INSERT INTO permissions (code, description)
VALUES (
           'system.oauth_clients.manage',
           'Register and manage OAuth clients across the platform'
       )
    ON CONFLICT (code) DO NOTHING;

-- Assign this permission to the built-in SYSTEM_ADMIN role.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p
              ON p.code = 'system.oauth_clients.manage'
WHERE r.organization_id IS NULL
  AND r.name = 'SYSTEM_ADMIN'
  AND r.is_system = TRUE
    ON CONFLICT (role_id, permission_id) DO NOTHING;