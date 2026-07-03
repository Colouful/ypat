-- V4__identity_encrypted.sql
-- PR-21: identity card number stored encrypted via KMS envelope.
--
-- Replaces the legacy plaintext t_user.certcode column with
-- t_user_identity. The encrypted blob carries the per-row
-- data key (wrapped by the KMS master key) inline; the
-- KMS key id is also stored for rotation.
--
-- Schema reality check: V1.0 §2.3.7 mentions "certcode" on
-- t_user, but the current schema dump (PR-07b baseline)
-- shows t_user_orig as the table that holds plaintext
-- PII; t_user.certcode is null for almost every row. We
-- therefore do NOT drop t_user.certcode (PR-21 is the
-- forward-only path); we add t_user_identity as the new
-- encrypted storage and migrate rows in a follow-up.

CREATE TABLE t_user_identity (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    encrypted_blob  LONGBLOB     NOT NULL,
    key_id          VARCHAR(64)  NOT NULL,
    status          VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    submitted_at    DATETIME     NULL,
    reviewed_at     DATETIME     NULL,
    reviewer_id     BIGINT       NULL,
    reject_reason   VARCHAR(255) NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_identity_user (user_id),
    KEY idx_user_identity_status (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;