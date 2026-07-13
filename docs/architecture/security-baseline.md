# Security Baseline

**Status**: PR-02 implemented for CORS. Other PRs follow.

## CORS

### What was broken

`WebSecurityConfig.corsConfigurer()` read `YPAT_CORS_ORIGINS` and
**defaulted to `"*"`** when the env var was missing or empty:

```java
String origins = env("YPAT_CORS_ORIGINS", "*");   // ← PR-02 fixed
```

A production deploy that forgot to set the env var would silently
allow every origin, with `allowCredentials` disabled. With JWT in
an `Authorization: Bearer ...` header that's not directly exploitable
for session theft, but it is still a textbook data-leak chain once
cookies / `withCredentials` show up.

### What PR-02 changes

`EnvironmentConfigurationValidator` now:

1. Requires `YPAT_CORS_ORIGINS` in any non-`dev` profile.
2. Rejects the literal value `*`.
3. Rejects a comma-list that contains `*` as one of the tokens
   (e.g. `https://app.example.com,*`).
4. Stays silent in `dev` profile — local HMR hits the API from
   random origins, that's expected.

### What PR-02 does NOT change

- The whitelist (`.antMatchers(...).permitAll()`) is already
  method + path precise. V1.1 §4.1's "P0 white-list hardening"
  requirement was already met by the existing code.
- `@EnableMethodSecurity(prePostEnabled = true)` is already on
  (`WebSecurityConfig.java` line 27). Fine-grained `@PreAuthorize`
  enforcement on top of URL-level rules is a separate PR-14 concern.
- IP whitelisting for WeChat / payment callbacks lives in PR-14
  alongside the auth migration.

### Operator runbook

If a deploy fails with:

```
YPAT EnvironmentConfigurationValidator: [staging] requires
YPAT_CORS_ORIGINS to be set. Aborting startup to prevent
misconfiguration.
```

or:

```
YPAT EnvironmentConfigurationValidator: [production] YPAT_CORS_ORIGINS='*'
is forbidden. Use an explicit comma-separated origin list.
```

Set `YPAT_CORS_ORIGINS` in the deploy environment to the
comma-separated list of allowed origins, e.g.

```bash
export YPAT_CORS_ORIGINS="https://app.example.com,https://admin.example.com"
```

Then restart the service.

## Future hardening (tracked elsewhere)

| Concern | Tracked in |
|---|---|
| `@PreAuthorize("hasRole('ADMIN')")` on every admin endpoint | PR-14 |
| WeChat / payment callback IP allowlist | PR-14 |
| Login + SMS rate limiting by IP + phone | PR-14 |
| CodeQL / gitleaks / Trivy | PR-03 |
| LegacyToken nbf / RS256 cutover | PR-14 |