ALTER TABLE oauth_clients ADD COLUMN secret_vali_days INTEGER;

ALTER TABLE oauth_clients
    ADD CONSTRAINT chk_oauth_client_secret_validity
        CHECK (
            secret_vali_days IS NULL
                OR secret_vali_days > 0
            );