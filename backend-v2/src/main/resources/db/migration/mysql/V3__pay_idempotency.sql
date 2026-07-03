-- V3__pay_idempotency.sql
-- PR-20: payment idempotency table.
--
-- The WeChat pay callback can fire twice (network retries,
-- operator double-click in WeChat console, etc). The wallet
-- must not be credited twice for the same out_trade_no.
--
-- Storage model: one row per (out_trade_no, status) tuple. A
-- callback INSERTs the row in PENDING, atomically claims it
-- with UPDATE ... WHERE status='PENDING', and writes the
-- terminal status (SUCCESS / FAILED) on the same transaction.
-- Two concurrent callbacks on the same out_trade_no race on
-- this row's primary key; the loser sees a duplicate-key error
-- and is treated as a duplicate delivery.

CREATE TABLE t_pay_idempotency (
    out_trade_no    VARCHAR(64)  NOT NULL,
    transaction_id  VARCHAR(64)  NULL,
    amount          BIGINT       NOT NULL,
    user_id         BIGINT       NULL,
    status          VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    notify_payload  TEXT         NULL,
    ledger_id       BIGINT       NULL,
    error_code      VARCHAR(64)  NULL,
    error_message   VARCHAR(255) NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    PRIMARY KEY (out_trade_no),
    KEY idx_pay_status_time (status, created_at),
    KEY idx_pay_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;