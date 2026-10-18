
-- V4__remove_email_normalized.sql

UPDATE users
SET email = LOWER(BTRIM(email));

ALTER TABLE users
DROP CONSTRAINT uq_users_email;

ALTER TABLE users
DROP COLUMN email_normalized;
