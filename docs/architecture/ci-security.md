# CI Security Gates

**Status**: PR-03 implemented.

Two new workflows sit alongside `ci.yml`, `openapi-lint.yml`,
`ddl-auto-guard.yml`.

## CodeQL (`.github/workflows/codeql.yml`)

GitHub-native static analysis. Runs against:

- **Java** (all of `backend/`, `backend-base/`)
- **JavaScript / TypeScript** (all of `frontend/`, `frontend-admin/`)

Triggers:

- Every PR touching the protected branches.
- Every push to `main`, `main2`, `refactor/backend-arch-upgrade`.
- Nightly at 03:17 UTC (off-peak).
- Manual dispatch.

Fail policy: CodeQL surfaces findings inline in the PR review UI
as alerts. We do **not** turn any specific query into a hard fail
in CI yet — that would burn PR throughput on noise. The rule is:

> Findings are reviewed by the upgrade lead before merge. Any
> `security-severity: HIGH` or higher must be fixed before the PR
> that introduced them is allowed to land.

When the upgrade is done and the noise settles, PR-22 (cleanup)
will turn on `failure-threshold: high` for the CodeQL job.

## Gitleaks (`.github/workflows/gitleaks.yml`)

Secret scanner. Blocks any PR that introduces a credential,
API key, or private key PEM block.

Behavior:

- Runs in default detect mode against the PR diff.
- Uploads SARIF to the Security tab.
- Posts a redacted summary comment on the PR.

Why we don't scan full git history in this workflow:

> YPAT's source tree contains `SecretExternalizationSourceTest`,
> a JUnit test that asserts known historical secrets have been
> purged. Scanning history would surface those legacy strings
> (which exist as fragments in test assertions, never as live
> values) as leaks. That would be noise, not signal. PR-time
> scanning catches real regressions.

## Workflow matrix after PR-03

| File | Purpose | Fail policy |
|---|---|---|
| `ci.yml` | Build + test (frontend + backend) | Build fail / test fail |
| `openapi-lint.yml` | OpenAPI contract lint (Spectral) | Spectral error |
| `ddl-auto-guard.yml` | Static guard on ddl-auto value | `update` / `create` / `create-drop` |
| `codeql.yml` | Static security analysis | Manual review threshold |
| `gitleaks.yml` | Secret detection | Any finding |

## Future hardening (tracked elsewhere)

| Concern | Tracked in |
|---|---|
| `@PreAuthorize("hasRole('ADMIN')")` on every admin endpoint | PR-14 |
| WeChat / payment callback IP allowlist | PR-14 |
| Login + SMS rate limiting by IP + phone | PR-14 |
| LegacyToken nbf / RS256 cutover | PR-14 |
| Trivy image scan / OWASP Dep-Check | PR-10 (storage infra) |
| CycloneDX SBOM + cosign signing | PR-10 |