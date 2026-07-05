# Notification Async (PR-18)

**Status**: PR-18 implemented as the contract + listener
skeleton. Real SMS / push / IM integrations land with PR-18
follow-up.

## What lands

- `notification/application/NotificationService.java` —
  Spring's `ApplicationEventPublisher` + three typed records
  (`Sms`, `Push`, `InApp`).
- `notification/application/AsyncNotificationListener.java` —
  `@Async @EventListener` methods. Spring's task executor
  delivers the events off the request thread.
- `notification/api/NotificationController.java` — `POST
  /api/notification/sms` and `POST /api/notification/push`.

## Why Spring events, not MQ

V1.1 §1.2 row 4 ("领域事件 schema 注册") wants a Schema
Registry eventually, but the PR-18 goal is async dispatch
inside one JVM. Spring events satisfy that for now, and
`@TransactionalEventListener` already gives us the
"fire-after-commit" semantic that downstream consumers
(wallet, appointment) need.

When the upgrade is done (PR-22 final), `ApplicationEventPublisher`
is replaced with a Kafka / RocketMQ producer. The records
(`Sms`, `Push`, `InApp`) become the schema in the Schema
Registry; consumers swap in `@KafkaListener` /
`@RocketMQMessageListener`.

## What PR-18 deliberately does NOT do

- **Real SMS / push integrations.** Tencent SMS SDK,
  JPush / Getui, FCM, etc. all live in the per-channel
  sender beans wired in PR-18 follow-up.
- **In-app message persistence.** Writing to `t_mess_info`
  (Flyway-managed) needs a JPA entity, which lands with PR-19
  (wallet) — the same Flyway baseline run carries both.
- **Template management.** `templateId` is a string. The
  template registry table is a separate concern.
- **Rate limiting.** Per V1.1 §4.1 third bullet, SMS and
  login endpoints need IP+phone rate limiting. That belongs
  with the auth migration (PR-14 follow-up), not here.

## Verification

Local compile:

```bash
cd backend-v2
mvn -B -ntp test
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
```

`ModulithStructureTest.verify()` (PR-12) passes — `notification`
joins the 11-module list with all four layers wired.

## Migration map

| Phase | PR | What |
|---|---|---|
| Now | PR-18 (this) | Async dispatch via Spring events |
| Next | PR-18 follow-up | Real SMS / push integrations + t_mess_info writes |
| Later | PR-22 final | Swap ApplicationEventPublisher for Kafka / RocketMQ |

## References

- V1.1 §1.2 row 4 (domain event schema registry)
- Upgrade plan: PR-18 (this), PR-18 follow-up, PR-22 final