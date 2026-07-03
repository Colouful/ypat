# Payment (PR-20)

**Status**: PR-20 implemented. WeChat callback idempotent-credit
flow against the wallet from PR-19. Three-side sign-off (Risk /
Legal / Finance, V1.1 §9.1) is operator-side, not enforced in
code.

## What lands

- `db/migration/mysql/V3__pay_idempotency.sql`
    - `t_pay_idempotency` keyed by `out_trade_no`. status enum
      (PENDING / SUCCESS / FAILED), ledger_id link to
      `t_wallet_ledger`.
- `payment/domain/PayIdempotency.java`
- `payment/infrastructure/PayIdempotencyRepository.java`
- `payment/application/PaymentService.java` — `@Transactional`
  handleCallback(). Inserts the idempotency row first; a
  duplicate-key collision (concurrent callbacks) is treated as
  a successful duplicate and returns false. On success it calls
  `WalletService.apply(...)` from PR-19 with reason='DEPOSIT',
  refType='WXPAY', refId=out_trade_no.
- `payment/api/WeChatPayNotificationController.java` — `POST
  /wxpay/notify`, returns literal "SUCCESS" (text/plain, the
  WeChat protocol expectation).
- `docs/architecture/payment-wechat.md` (this file).

## Idempotency model

```
  WeChat callback POST /wxpay/notify
    INSERT INTO t_pay_idempotency (out_trade_no, status='PENDING')
      -- race lost: another callback already inserted
      -- duplicate-key collision -> PaymentService returns false
      -- controller still responds "SUCCESS"
    UPDATE  t_pay_idempotency SET status='SUCCESS', amount=?, ...
    walletService.apply(userId, amount, 'DEPOSIT', 'WXPAY', out_trade_no)
      -- runs PR-19's strongly-consistent flow:
      --   SELECT t_wallet FOR UPDATE
      --   balance += amount (DB CHECK rejects negative)
      --   INSERT INTO t_wallet_ledger
    UPDATE  t_pay_idempotency SET ledger_id = <ledger id>
```

All inside one transaction. If the credit fails (e.g. wallet
row missing), the transaction rolls back and t_pay_idempotency
stays in PENDING — the next callback retries from scratch.

## What PR-20 deliberately does NOT do

- **Real signature verification.** V1.1 §4.4 step 1 is HMAC-SHA256
  over the raw XML using the WeChat pay key. PR-20 ships a
  placeholder parser; the HMAC check is PR-20 follow-up.
- **IP allowlist.** Operator-side Nginx ACL on `/wxpay/notify`
  (V1.1 §4.4 "回调 IP 仅放微信官方 IP 段"). Out of code scope.
- **Refund / cancel callback.** PR-20 follow-up wires the
  `refund_id` / `refund_status` flow against WalletService.apply
  with negative delta.
- **Three-side sign-off workflow.** V1.1 §9.1 row 4 says the
  production rollout of PR-20 needs 风控 / 法务 / 财务 sign-off
  recorded in the deploy ticket. That is operator workflow,
  not code.
- **Real WeChat Pay SDK integration.** Tencent's SDK is the
  PR-20 follow-up. PR-20 ships only the contract + idempotency
  layer; the merchant-id / pay-key configuration + HMAC
  verification come later.

## Verification

Local compile + Modulith `verify()`:

```bash
cd backend-v2
mvn -B -ntp test
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
```

End-to-end via docker compose (mirrors PR-19's flow):

```bash
docker compose --env-file ../ypat-workspace/.env \\
    up -d backend-v2 --no-deps

# Flyway runs V3:
docker logs backend-v2 | grep Flyway
# DbValidate  : Successfully validated 4 migrations
# DbMigrate   : Migrating schema ypat to version "3 - pay idempotency"

mysql ypat -e \"SHOW TABLES LIKE 't_pay%'\"
# t_pay_idempotency

# WeChat-shaped XML (signature verification stubbed):
curl -X POST -H 'Content-Type: application/xml' \\
    -d '<xml><out_trade_no>WXTEST001</out_trade_no>\\
        <transaction_id>12345</transaction_id>\\
        <total_fee>5000</total_fee>\\
        <user_id>1</user_id></xml>' \\
    localhost:8092/wxpay/notify
# SUCCESS (text/plain)
mysql ypat -e 'SELECT out_trade_no, status, ledger_id FROM t_pay_idempotency'
# WXTEST001 | SUCCESS | <id>
mysql ypat -e 'SELECT balance FROM t_wallet WHERE user_id=1'
# 5000  # +50.00 yuan
```

## References

- V1.1 §1.2 row 5 (strong consistency across wallet + payment)
- V1.1 §4.4 (WeChat callback flow)
- V1.1 §9.1 (verification: 10k TPS, 0 idempotent loss)
- Upgrade plan: PR-19 (wallet), PR-20 (this), PR-22 final