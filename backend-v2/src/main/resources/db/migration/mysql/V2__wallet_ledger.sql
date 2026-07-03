-- V2__wallet_ledger.sql
-- PR-19: in-app wallet ledger.
--
-- Strongly consistent (V1.1 §1.2 row 5). Two tables:
--
--   t_wallet       one row per user, holds the current balance.
--                  UNIQUE(user_id) makes a re-create race impossible.
--
--   t_wallet_ledger append-only ledger. Every balance change is a
--                  row here, with a balance_after column that
--                  makes audits and reconciliations trivial.
--
-- The CURRENT balance is `t_wallet.balance`. The ledger is the
-- SOURCE OF TRUTH for how we got there.
--
-- Concurrency model (V1.1 §3.2 and PR-09 pattern):
--   - All updates run inside a single Flyway-managed transaction.
--   - t_wallet row is locked with SELECT ... FOR UPDATE before
--     the INSERT into t_wallet_ledger.
--   - The UPDATE that follows recomputes balance from the ledger,
--     NOT a `balance + delta` increment, so concurrent writers
--     cannot drift the counter.
--
-- On-line DDL: V2 only adds new tables, never touches existing
-- rows, so ALGORITHM=INPLACE / LOCK=NONE apply.

CREATE TABLE t_wallet (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    balance       BIGINT       NOT NULL DEFAULT 0,
    version       BIGINT       NOT NULL DEFAULT 0,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wallet_user (user_id),
    CONSTRAINT chk_wallet_balance_nonneg CHECK (balance >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE t_wallet_ledger (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL,
    delta          BIGINT       NOT NULL,
    balance_after  BIGINT       NOT NULL,
    reason         VARCHAR(64)  NOT NULL,
    ref_type       VARCHAR(32)  NULL,
    ref_id         VARCHAR(64)  NULL,
    actor_user_id  BIGINT       NULL,
    note           VARCHAR(255) NULL,
    created_at     DATETIME     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_ledger_user_time (user_id, created_at),
    KEY idx_ledger_ref (ref_type, ref_id),
    CONSTRAINT chk_ledger_balance_nonneg CHECK (balance_after >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Pre-create wallets for existing users so the next read doesn't
-- race the first deposit. Idempotent via INSERT IGNORE so
-- reruns are safe.
INSERT IGNORE INTO t_wallet (user_id, balance, version, created_at, updated_at)
SELECT id, 0, 0, NOW(), NOW() FROM t_user;