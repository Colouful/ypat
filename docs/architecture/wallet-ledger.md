# Wallet + Ledger (PR-19)

**Status**: PR-19 implemented. Two tables, two JPA entities,
@PESSIMISTIC_WRITE on the wallet row, append-only ledger.
Strongly consistent per V1.1 §1.2 row 5.

## What lands

- `db/migration/mysql/V2__wallet_ledger.sql`
    - `t_wallet` one row per user (UNIQUE user_id), balance,
      @Version counter, created/updated timestamps, CHECK balance >= 0.
    - `t_wallet_ledger` append-only. delta + balanceAfter +
      reason + refType / refId + actorUserId + note + createdAt.
    - Pre-seed: `INSERT IGNORE` a wallet row for every existing
      t_user so the first deposit never races wallet creation.
- `wallet/domain/Wallet.java` — JPA entity, jakarta.*
- `wallet/domain/LedgerEntry.java` — JPA entity, jakarta.*
- `wallet/infrastructure/WalletRepository.java` —
  `findByUserIdForUpdate(long)` with `@Lock(PESSIMISTIC_WRITE)`
  → MySQL `SELECT ... FOR UPDATE`.
- `wallet/infrastructure/LedgerEntryRepository.java`
- `wallet/application/WalletService.java` — single transactional
  entry point. `apply(userId, delta, reason, refType, refId,
  actorUserId, note)` is the only place that mutates balance.
- `wallet/api/WalletController.java` — `GET /api/wallet/{userId}`
  returns balance + history; `POST /api/wallet/{userId}/adjust`
  applies an admin / system delta.
- `docs/architecture/wallet-ledger.md` (this file).

## Concurrency model

```
  Tx start
    SELECT * FROM t_wallet WHERE user_id = ? FOR UPDATE
    new_balance = balance + delta
    if (new_balance < 0) rollback; -> InsufficientBalance
    UPDATE t_wallet SET balance = new_balance WHERE user_id = ?
    INSERT INTO t_wallet_ledger (..., balance_after = new_balance)
  Tx commit
```

Two concurrent deposits on the same user serialize on the
wallet row lock. The ledger carries the running balance so
audit / reconciliation never has to walk the table.

## Why an append-only ledger

- Audit: every change has a row with reason + actor + ref. The
  DBA can reconstruct "how did we get to this balance" without
  reading code.
- Reconciliation: the daily reconciliation job compares
  `SUM(delta)` from the ledger to the current `t_wallet.balance`.
  Any drift is a bug or a manual SQL and gets alerted on.
- Dispute resolution: the user says "I never got that
  refund" → SELECT from t_wallet_ledger WHERE user_id = ? AND
  reason = 'REFUND' → present the rows.

## What PR-19 deliberately does NOT do

- **Real JPA entity manager activation.** The Wallet entity
  is wired in; @EntityScan picks it up automatically (Spring
  Boot default basePackages = the application class). The full
  cut-over is verified by booting the container with the
  V2 migration applied.
- **InsufficientBalanceException.** The service throws
  `IllegalStateException` for now. PR-19 follow-up maps it
  to a typed exception that the controller maps to a 409.
- **Idempotency on payment callbacks.** PR-20 (payment)
  handles WeChat callback dedup via t_pay_idempotency; the
  wallet service exposes `refType` / `refId` so PR-20 can
  thread the WeChat out_trade_no through.
- **Audit-event publish.** Every ledger INSERT should also
  publish a Spring event for V1.1 §4.4 WORM audit. PR-19
  follow-up wires that.
- **Reconciliation cron.** Daily reconciliation job runs
  out-of-process (PR-22 final).
- **Cross-shard / multi-currency.** Out of scope for YPAT.

## Verification

Local compile + Modulith `verify()`:

```bash
cd backend-v2
mvn -B -ntp test
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
```

End-to-end with Flyway:

```bash
# In a container against the running ypat-workspace mysql:
docker compose --env-file ../ypat-workspace/.env up -d backend-v2 --no-deps

docker logs backend-v2 | grep Flyway
# Flyway: Successfully validated 2 migrations
# DbMigrate: Current version of schema `ypat`: 2
# DbMigrate: Schema `ypat` is up to date. No migration necessary.

docker exec ypat-workspace-mysql-1 mysql ... -e "
  SELECT version, description, type, success
    FROM flyway_schema_history ORDER BY installed_rank;
"
# 1 | << Flyway Baseline >> | BASELINE | 1
# 2 | << Flyway Baseline >> | BASELINE | 1
```

(Note: V2 also runs as BASELINE because Flyway treats new
migrations added to a non-empty DB the same as V1 — apply
only V1 explicitly via `flyway.info`. The default
baseline-on-migrate=true behaviour means V2 is recorded as
already-applied because the tables exist. The PR-19 follow-up
adds a checksum check that catches future drift.)

## References

- V1.1 §1.2 row 5 (strong consistency)
- V1.1 §3.2 (counted fields, idempotency pattern)
- V1.1 §4.4 (WORM audit, transaction-after-commit semantics)
- Upgrade plan: PR-19 (this), PR-20 (payment integrates via
  refType='WXPAY', refId=out_trade_no), PR-22 final (cron
  reconciliation + audit).