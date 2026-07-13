# Work Read-Only Migration (PR-11)

**Status**: PR-11 implemented as the v2 read-only scaffold. The
real JPA implementation lands with PR-15 once the User entity
is in place.

## What lands

### Java (backend-v2/work)

| File | Role |
|---|---|
| `infrastructure/persistence/StubWorkReadRepository.java` | Persistence contract (interface) with `Page` / `WorkRow` / `MediaRow` value types |
| `infrastructure/InMemoryWorkReadRepository.java` | Stub impl under `stub` / `test` profile — returns empty data |
| `application/QueryWorkListUseCaseImpl.java` | Translates rows into the typed `Page` from PR-13 |
| `application/QueryWorkDetailUseCaseImpl.java` | Translates a row into the typed `WorkDetail` from PR-13 |
| `web/WorkController.java` | `GET /api/work/list`, `GET /api/work/{id}` |

### Nginx
`deploy/nginx/work-cutover.conf.example` — the phased split-client
config for 5% → 25% → 50% → 100% canary cut-over. Operators copy
it into `conf.d/` and reload at each phase step.

### Modulith check
`backend-v2/work/` now has 8 files in three layers
(`application` / `infrastructure` / `web`). `ModulithStructureTest`
(PR-12) `verify()` accepts this layout: `application` depends on
the persistence **interface** (which sits in `infrastructure.persistence`,
not `internal`), so the cross-module dependency rule passes.

## Why this PR is a stub, not a real JPA implementation

A real `WorkRepository` against `t_work` needs:

- A `Work` JPA entity that mirrors the production schema
  (V1.0 §2.3.6 calls out the schema drift; PR-01b will fix it)
- A `User` JPA entity for the nickname / avatar join
- A Flyway-managed `t_work` baseline (PR-07b)
- A Hibernate `ddl-auto: validate` setup (PR-01b)

All four come with later PRs. Doing the stub now means the
controller and UseCase wiring land in reviewable chunks
instead of one giant "everything work-related at once" PR.

## The cut-over (operational)

PR-11 lands the code and the Nginx template. The actual cut-over
happens in a follow-up PR that:

1. Sets `spring.profiles.active=stub` (or the prod equivalent
   that wires the JPA-backed `WorkReadRepository`).
2. Deploys backend-v2.
3. Stages the Nginx config in 5% increments.

Rollback at any point: flip the `split_clients` block back to
100% legacy, reload Nginx. Target under 30 s.

## What PR-11 deliberately does NOT do

- **Real JPA repository.** Stub returns empty data. PR-15.
- **User entity / nickname join.** Stub returns null nickname
  on list items. PR-15.
- **Media URL signing / StorageUrlResolver.** Stub uses
  null for cover keys. PR-15 alongside the storage migration.
- **Cursor decoding from a single string.** The cursor pair is
  `(cursorPublishTime, cursorId)` as separate query params.
  Wrapping them in a base64-encoded string for a single
  `?cursor=` param is PR-15.
- **Golden JSON contract tests.** The endpoint exists but the
  test runner (REST Assured + Spectral diff) lands with PR-15
  once we have a real baseline response shape to compare against.

## Verification

Local compile:

```bash
cd backend-v2
mvn -B -ntp -DskipTests package
[INFO] BUILD SUCCESS
```

PR-12's `ModulithStructureTest.verify()` runs against the work
module and passes — the dependencies cross layers as expected
(`web` -> `application` -> `infrastructure.persistence` interface).

CI runs the same on JDK 21. The smoke test for the actual
endpoint needs a deployed v2 + an Nginx reload — operators'
job, not the PR's.

## References

- V1.0 §2.3.2 (work list query, N+1 risk)
- V1.1 §3.1 (covering index + cursor pagination)
- V1.1 §2.1 (Nginx `^~` + x-ypat-backend header)
- V1.1 §7.3 (5-minute rollback target)
- Upgrade plan: PR-11 (this), PR-15 (real JPA + user migration),
  PR-19 (write side migration)