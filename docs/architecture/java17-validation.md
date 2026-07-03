# Java 17 Compatibility Validation

**Status**: PR-05 implemented. Legacy backend compiles clean on JDK 17.

## Why this matters

PR-04 introduced `backend-v2/` on Spring Boot 2.7.18 + Java 17.
PR-05 proves that the **legacy** `backend/` tree (Spring Boot
1.5.9, originally written for Java 8) still builds under Java 17.

That matters because the cut-over plan keeps both trees compiling
in parallel. If the legacy tree ever silently loses Java 17
compatibility, we want to find out on a PR — not on the day we
flip the Nginx prefix.

## What was verified (locally, before this PR opened)

```
$ java --version
openjdk 17.0.18 2026-01-20 LTS
OpenJDK Runtime Environment Microsoft-13106359 (build 17.0.18+8-LTS)
OpenJDK 64-Bit Server VM Microsoft-13106359 (build 17.0.18+8-LTS, mixed mode, sharing)

$ mvn --version
Apache Maven 3.6.3 (cecedd343002696d0abb50b32b541b8a6ba2883f)
Java version: 17.0.18, vendor: Microsoft

$ cd backend && mvn -B -ntp -Pdev -DskipTests package
... (Reactor Summary)
[INFO] system-object ..................................... SUCCESS
[INFO] system-domain ..................................... SUCCESS
[INFO] system-restapi .................................... SUCCESS
[INFO] system-web ........................................ SUCCESS
[INFO] system-sso ........................................ SUCCESS
[INFO] system-security ................................... SUCCESS
[INFO] system-wap ........................................ SUCCESS
[INFO] BUILD SUCCESS — 4.4 s
```

All seven legacy modules compile clean on JDK 17. The only
warnings are pre-existing:

- `CopyUtil.java` uses a deprecated `sun.*` API.
- `UserService.java` triggers an unchecked-operations warning.
- `MapUtils.java` similar.
- `com.alibaba:druid:1.1.2` POM warning — Druid jar declares
  transitive deps in a way newer Maven warns about. Doesn't
  block compilation.

None of these are introduced by JDK 17; they all exist on JDK 8
too.

## What this PR adds

- `.github/workflows/backend-java17.yml`
  Runs `mvn -B -ntp -Pdev -DskipTests package` on every PR that
  touches `backend/` and confirms each module produces its jar.

## What this PR does NOT do

- Does not switch the legacy backend's source/target bytecode
  level. The legacy pom stays at `1.8` source/target. JDK 17
  can compile a `1.8` target fine; that's what we just proved.
- Does not bump Spring Boot. The legacy tree stays on 1.5.9
  until PR-22 retires it.
- Does not run the unit tests under JDK 17. Tests still run on
  JDK 8 in `ci.yml` → `backend-test` job. PR-05 is about
  *compiling*, not about runtime behavior.

## Caveats found during validation

These don't block the PR but are worth tracking:

1. **Druid 1.1.2 BOM warning.** Cosmetic. Will go away when
   PR-12 swaps Druid for HikariCP (or bumps Druid to a version
   with a clean POM).
2. **sun.* deprecation in CopyUtil.** Internal copy helper.
   Either migrate to `java.nio.file.Files.copy` in a follow-up
   or accept the warning.
3. **`@EnableGlobalMethodSecurity` is deprecated** as of Spring
   Security 5.6. The current code still works on Spring Boot
   1.5.9's Spring Security 5.5.x but will need to migrate to
   `@EnableMethodSecurity` during the auth PR (PR-14).
4. **`OAuthConfigurer extends AuthorizationServerConfigurerAdapter`**
   — also deprecated in Spring Security 5.6. Migration in PR-14.

## Runtime note (not in scope of PR-05)

Compilation is necessary but not sufficient. The legacy backend
has not been *started* under JDK 17 in this PR. We expect
runtime surprises (reflection on JDK internals, security
manager changes) once we do — those will be discovered and
addressed in the migration PRs (PR-04 v2 starts clean; PR-12
flips the parent). Until then, the legacy backend continues to
ship on JDK 8 in production.