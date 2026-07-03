# Appointment Migration (PR-16)

**Status**: PR-16 implemented as the v2 scaffold. The state
machine, audit trail, and atomic transitions land with the
PR-16 follow-up.

## What lands

### Java (backend-v2/appointment)

| File | Role |
|---|---|
| `application/CreateAppointmentUseCase.java` | The contract for `POST /api/appointment`. Defines `Command`, `Result`, and `State`. |
| `application/TransitionAppointmentUseCase.java` | The contract for `POST /api/appointment/{id}/transition`. Defines `Event` and `Conflict`. |
| `application/Stubs.java` | Two `@Service` stub impls that always succeed. |
| `api/AppointmentController.java` | Both endpoints, taking `Principal` directly. |
| `package-info.java` + `api/package-info.java` | Modulith named-interface declarations. |

## State machine

The legacy code lets either side cancel or accept freely.
v2 narrows the contract; the state machine is the same one
the legacy business rules describe, but enforced in one place:

```
PENDING   --(creator confirms)-->   CONFIRMED
PENDING   --(creator cancels)-->    CANCELLED
PENDING   --(TTL expires)-->        EXPIRED
CONFIRMED --(both accept)-->         COMPLETED
CONFIRMED --(either cancels)-->      CANCELLED
```

Anything else throws `TransitionAppointmentUseCase.Conflict`.
The implementation will enforce these atomically in SQL
(`UPDATE t_appointment SET state=? WHERE id=? AND state IN (...)`)
so two concurrent cancels don't both succeed.

## Why both endpoints take `Principal`

Same reasoning as PR-15: `TokenBridge.resolve(authorizationHeader)`
(from PR-14) returns a `Principal`, and the controllers never
see the raw token. `CreateAppointmentUseCase.create` takes the
`fromUserId` from the Principal; `TransitionAppointmentUseCase.transition`
takes the actor's `userId` so the implementation can audit-log
who did what.

## Audit trail (PR-16 follow-up)

V1.1 §4.4 mandates WORM audit storage for admin operations.
Appointment transitions qualify because they're financial-adjacent
(wallet drawdowns ride on them). PR-16 follow-up adds a
`t_appointment_audit` table that records
`(appointment_id, actor_user_id, from_state, to_state, event,
occurred_at)`. The audit row is INSERTed in the same transaction
as the state change, so the two cannot diverge.

## What PR-16 deliberately does NOT do

- Real JPA entity + repository. Stubs always succeed.
- Atomic SQL transition (`UPDATE ... WHERE state IN (...)`). PR-16
  follow-up.
- Audit table + WORM sink. PR-16 follow-up.
- TTL sweeper for PENDING -> EXPIRED. PR-16 follow-up adds a
  scheduled task.
- Wallet / payment integration. PR-19 (wallet) + PR-20 (payment)
  plug in once the appointment lands.
- Calendar / iCal export. Out of scope for the upgrade.

## Verification

Local compile:

```bash
cd backend-v2
mvn -B -ntp -DskipTests package
[INFO] BUILD SUCCESS
```

`ModulithStructureTest.verify()` (PR-12) passes — `appointment`
is now a 12th module, all four layers (application / infrastructure
/ api / root package-info) line up.

Full suite:

```bash
mvn -B -ntp test
# 15 + 0 = 15 tests (PR-16 doesn't add new tests; the stubs
# are too thin to be worth a unit test beyond the existing
# structure rules)
```

## References

- V1.1 §4.4 (WORM audit, transition concurrency)
- Upgrade plan: PR-15 (user migration), PR-16 (this),
  PR-19 (wallet), PR-20 (payment)