# backend-v2 Module Structure

**Status**: PR-06 implemented. 11 top-level modules + ArchUnit
discipline.

## What landed

backend-v2/ now has the 11 module roots we agreed on in the
upgrade plan:

| Module | Owner | Strong consistency? | First real PR |
|---|---|---|---|
| `auth` | v2 | yes (stateless token) | PR-14 |
| `user` | v2 | eventual | PR-15 |
| `work` | v2 | eventual | PR-11 (read), PR-13 (write) |
| `content` | v2 | eventual | PR-11 |
| `member` | v2 | eventual | merged into user in PR-17 |
| `wallet` | v2 | **strong** | PR-19 |
| `payment` | v2 | **strong + external** | PR-20 |
| `identity` | v2 | **strong + KMS** | PR-21 |
| `notification` | v2 | eventual, async | PR-18 |
| `storage` | v2 | n/a (infra) | PR-10 |
| `audit` | v2 | n/a (cross-cutting) | first user is PR-14 |

Each root has a `package-info.java` claiming ownership, the
migration target date placeholder, and the public API surface
(`api` / `application` / `domain`). The exact rules for each
module are written in its own package-info and enforced by
`ModulePackageStructureTest`.

## Why package-info.java per module

Three reasons:

1. **Javadoc anchor.** A future contributor reading
   `com.ypat.wallet` immediately knows who owns it, what's
   public, and when it's expected to migrate.
2. **Lint handle.** ArchUnit can grep for these as the source of
   truth on what counts as a "module". When Modulith lands in
   PR-12 these package-info entries will be replaced by
   `@org.springframework.modulith.ApplicationModule` annotations
   on the same package — the migration is purely mechanical.
3. **IDE affordance.** IntelliJ shows the package-info text
   next to the package in the project tree. It's the one piece
   of docs that nobody has to remember to open.

## ArchUnit rules in PR-06

`ModulePackageStructureTest` (test scope) enforces:

| Rule | Catches |
|---|---|
| Every top-level module has a `package-info` | Module added without claiming it |
| `*.internal` packages can't depend on other modules | Accidental coupling across modules |
| `com.ypat.*` (non-legacy) can't depend on legacy packages | A drift toward the old `com.ypat.service.*` |
| `*Controller` classes live in `..api..` | Controllers in the wrong layer |

These rules run as a normal JUnit test (`./mvnw test`). They do
**not** spin up Spring, do not touch the DB, and complete in
under a second. When a new module ships, the test list grows.

## What PR-06 does NOT do

- Does NOT enable Spring Modulith. That's PR-12 on Boot 3.x.
  Modulith's `ApplicationModules.of(...).verify()` does a deeper
  cross-check (events, repositories) but it does not work on
  Spring Boot 2.7.x.
- Does NOT enforce component-scan restrictions. Adding
  `@ComponentScan` filters too early creates noise and limits
  refactoring freedom. Defer until PR-12 brings Modulith.
- Does NOT add any business code. The 11 packages are empty
  placeholders. Real classes arrive with the migration PRs.
- Does NOT cover `@Repository`/`@Service` layer rules. Those
  live behind Modulith's verify() in PR-12.

## CI

ArchUnit runs as part of the standard `mvn test` for backend-v2.
A new CI workflow (`backend-v2-archunit.yml`) makes the same
run visible on every PR.

## How to extend

When a new module appears:

1. `mkdir -p backend-v2/src/main/java/com/ypat/<name>`
2. Add `<name>/package-info.java` following the template
   (`@NonNullApi package com.ypat.<name>;` plus the Javadoc).
3. Add `<name>` to `ModulePackageStructureTest.TOP_LEVEL_MODULES`.
4. Add a row to the table above.

When a new rule appears:

1. Add it to `ModulePackageStructureTest`.
2. Run `mvn test` to confirm it passes against current code.
3. If it intentionally fails current code, mark it
   `@Ignore`d with a TODO pointing to the PR that will fix it.