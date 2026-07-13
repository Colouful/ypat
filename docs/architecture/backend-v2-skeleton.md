# backend-v2 Skeleton

**Status**: PR-04 landed.

## What landed

A brand-new Maven module at `backend-v2/` sitting beside the
legacy `backend/` and `backend-base/`. It is the smallest possible
Spring Boot 2.7.18 + Java 17 application that will boot:

```
backend-v2/
├── pom.xml                     # spring-boot-starter-parent 2.7.18, Java 17
├── README.md
└── src/
    ├── main/
    │   ├── java/com/ypat/YpatApplication.java
    │   └── resources/application.yml
    └── test/                   # empty, JUnit 5 lands with first real test
```

## Why a brand-new module instead of refactoring in place

Spring Boot 1.5.9 → 3.5.x is a 13-version jump. Refactoring the
existing `backend/` in place would require editing ~31 entities,
~59 controllers, the OAuth2 configurer, and the Eureka client at
the same time, with no way to ship a working slice in between.

The new module lets us:

1. Land an empty Spring Boot 2.7.18 app on Java 17 in PR-04.
2. Verify the legacy module still compiles under Java 17 in PR-05.
3. Build out v2 one module at a time, with each module behind a
   Nginx prefix that we can switch traffic on (PR-11 onward).
4. Flip the parent to 3.5.x + Java 21 in PR-12 with one focused
   `javax.* → jakarta.*` migration PR.

## Versions locked

| Component | Version | Why |
|---|---|---|
| Spring Boot | 2.7.18 | Latest 2.7.x; still receives security fixes |
| Java | 17 | Last LTS on `javax.*`; matches Boot 2.7.x support matrix |
| Maven | 3.9+ | Implicit via CI agent |
| spring-boot-starter-web | (BOM-managed) | Nothing else added in PR-04 |

Deliberately not added (deferred to the PR that needs them):

- springdoc-openapi — only useful once a Controller exists (PR-11+).
- Spring Modulith — only stable on Spring Boot 3.x (PR-12).
- ArchUnit — has its own setup story; PR-06 brings it in.
- Flyway / Hibernate / MySQL driver — first real DB connection lands
  with PR-07 (Flyway baseline) or PR-08 (first migrated query).
- Resilience4j — PR-09 onward, only for the third-party calls.
- spring-cloud-starter-config — kept on the legacy module for now;
  v2 starts with `application.yml` + env vars.

## Local port

| App | Port |
|---|---|
| system-wap | 8081 (unchanged) |
| system-restapi | 9081 (unchanged) |
| **backend-v2** | **8082** (this PR) |

Nginx cut-over in PR-11 starts routing selected `/api/work/**`
traffic to `8082` at 5% canary.

## CI

A new workflow `backend-v2-build.yml` runs `mvn -B -ntp -DskipTests
package` on every push to a PR branch. Test scope is intentionally
off in PR-04 — there are no tests yet and the empty starter jar
should compile clean before we add any.

## Verification

Local:

```bash
cd backend-v2
mvn -B -ntp -DskipTests package
# → BUILD SUCCESS, target/backend-v2-1.0.0-SNAPSHOT.jar produced
java -jar target/backend-v2-1.0.0-SNAPSHOT.jar &
sleep 5
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8082/actuator/health
# → 404 expected (no actuator yet); what matters is the JVM started.
```

CI will run the same sequence on every PR. If `BUILD SUCCESS` does
not hold for an empty starter project, something is wrong with the
local Java / Maven toolchain and we want to know early.