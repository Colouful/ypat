# Spring Modulith + JDK 21 (PR-12)

**Status**: PR-12 implemented. backend-v2 jumps from Spring Boot
2.7.18 / Java 17 to Spring Boot 3.5.15 / Java 21. Spring Modulith
1.4.x is on the classpath with `verify()` as a CI gate.

## What PR-12 lands

| Change | Before (PR-04) | After (PR-12) |
|---|---|---|
| `spring-boot-starter-parent` | 2.7.18 | 3.5.15 |
| `java.version` / `maven.compiler.release` | 17 | 21 |
| Spring Modulith on classpath | no | yes (1.4.x BOM) |
| `ModulithStructureTest` | absent | present, runs `verify()` |
| CI JDK | 17 | 21 |
| `archunit-junit5` | 0.23.1 (last JDK 17 line) | 1.4.1 (tracks Modulith) |

The 2.7.18 jump-board did its job (PR-05 proved the legacy
backend compiles on JDK 17). PR-12 does the second jump, which
was always going to happen — Modulith 1.x does not work on
Spring Boot 2.x.

## What PR-12 deliberately does NOT do

- **Migrate javax.\* → jakarta.\*** in legacy code. PR-12 only
  moves backend-v2 to 3.5.x. The legacy `backend/` tree stays
  on Spring Boot 1.5.9 + javax.\*. Migrations there happen as
  part of the per-module migration PRs (PR-13+) and the final
  retirement (PR-22).
- **Rewrite YpatApplication.** The class is still an empty
  `@SpringBootApplication`. Modulith discovers the modules via
  package-info (PR-06) — no application code change needed.
- **Promote Modulith to a hard fail at PR-time** beyond what
  `verify()` already does. Today the test fails the build if
  boundary rules break. We don't add additional
  custom rules until at least four real business modules land
  (PR-11+).
- **Re-tune all the new CI workflows.** They each got their
  JDK bumped to 21 in PR-12; that's it. Future PRs adjust
  thresholds as the codebase grows.

## How the boundary check works

PR-06 created eleven `package-info.java` files, one per module.
Modulith 1.4 reads those and treats each root package as a
module. `verify()` then enforces:

- No cycles in the module dependency graph.
- Events published in module A can only be consumed by module B
  if B explicitly opts in (`@ApplicationModuleListener`).
- Cross-module access to `*.internal.*` is forbidden.
- Aggregate roots follow Modulith's default-public convention.

The test runs as part of the normal `mvn test` cycle and on
the dedicated `backend-v2-modulith.yml` CI job. The CI job also
uploads the auto-generated component diagram as a build
artifact so reviewers can see the module graph evolve.

## Why now (and not earlier)

- Modulith 1.x needs Spring Boot 3.x → JDK 21 jump is required
  to use it.
- PR-04 had to pick 2.7.18 because that was the path that let
  the legacy `backend/` tree keep compiling on JDK 17 in
  parallel (PR-05).
- The earlier PRs (PR-04 / PR-05 / PR-06 / PR-07a / PR-08 /
  PR-09 / PR-10) deliberately avoided Modulith so they could
  ship small, focused changes.
- PR-11+ is when real business modules start arriving; PR-12
  gives Modulith the rule surface to bite before that wave
  begins.

## Verification

Local:

```bash
cd backend-v2
mvn -B -ntp -Dtest=ModulithStructureTest test
# Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
ls target/spring-modulith-docs/components.html   # visual artifact
```

CI:

- `backend-v2-modulith.yml` runs the same command and uploads
  the diagram to the workflow run's Artifacts section.

## Risks

- **Modulith rule surface is strict.** If a future PR introduces
  a module-internal type that's leaked (e.g. an `internal.*`
  type returned from a public controller), `verify()` will fail
  the build. That's the point, but expect at least one
  re-shuffle per module during PR-13 through PR-21.
- **Spring Boot 3.5 + Java 21 baseline is permanent.** Going
  back to 2.7.18 / JDK 17 is not a supported migration. If a
  blocker forces us off Java 21, the next-best is Java 17 LTS,
  but every dep we'd add would have to support 17 explicitly.
- **Java 21 module-path quirks.** Spring Boot 3.5 supports
  Java 17 and 21; the `maven.compiler.release=21` setting is
  what flips the bytecode level. There is no module-info.java
  anywhere; we're using the classpath, not the module path.
  Don't introduce module-info without a dedicated PR.

## References

- Spring Boot 3.5 release notes
- Spring Modulith 1.4 reference documentation
- Upgrade plan: PR-04 (jump-board), PR-12 (this), PR-13 onwards
  (business modules under Modulith discipline)