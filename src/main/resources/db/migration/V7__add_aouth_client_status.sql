ALTER TABLE oauth_clients ADD COLUMN status VARCHAR(1) NOT NULL DEFAULT 'A';
ALTER TABLE oauth_clients ADD CONSTRAINT ck_oauth_clients_status CHECK (status IN ('A', 'I', 'S'));