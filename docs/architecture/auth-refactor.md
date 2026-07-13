# Auth Refactor Plan (PR-14)

**Status**: PR-14 implemented as the contract layer. Concrete
classes (Authorization Server wiring, controller filter
integration, login flow) land in a follow-up PR.

## The current state (what we are replacing)

YPAT runs three authentication stacks in parallel today. All
three are still on disk at the start of the upgrade:

1. **Spring Security OAuth (legacy)** — `system-sso` runs
   `OAuthConfigurer extends AuthorizationServerConfigurerAdapter`.
   This is Spring Security 5.5 / Spring Boot 1.5.9 OAuth. Spring
   deprecated `AuthorizationServerConfigurerAdapter` in 5.6 and
   removed it in 6.0. We're on a stack it doesn't even compile
   against anymore.
2. **Self-rolled JWT (HS256)** — `system-wap/util/JwtTokenUtil.java`.
   Hand-parse / hand-sign HS256 tokens. Used for everything
   that isn't OAuth2.
3. **Spring Session** — `system-sso/application.yml` declares
   `server.session.cookie.name: SESSIONID`. The `system-sso`
   login flow issues a session cookie that downstream
   `system-restapi` reads.

V1.1 §1.2 row 3 calls this out: "Cookie/JWT 双栈会话衔接
[is a] 7-day 窗口期用户强制下线、撤销失败 [risk]".

## What v2 ships

| Concern | v2 answer |
|---|---|
| Token issuance | Spring Authorization Server (replaces `AuthorizationServerConfigurerAdapter`) |
| Token signature | RS256 with current `kid`. HS256 keys go to a separate `kid=legacy` |
| Principal abstraction | `com.ypat.auth.api.Principal` (PR-14) — single shape across all paths |
| Entry contract | `com.ypat.auth.api.TokenBridge` (PR-14) — controllers depend only on this |
| Legacy HS256 accept window | Hard cutoff: `nbf < 2026-09-01T00:00:00Z` (see `JwtTokenMigrator.RS256_CUTOFF_DATE`) |
| Revocation | `com.ypat.auth.internal.RedisTokenBlacklist` — Redis `bl:{kind}:{tokenId}` with TTL = exp |
| Session cookie | Dies with the legacy tree in PR-22 final. No equivalent in v2. |

## Hard cutover

The single most important date in the whole upgrade:

```
RS256_CUTOFF_DATE = 2026-09-01T00:00:00Z
```

Anything before: legacy HS256 accepted, RS256 accepted.  
Anything on or after: legacy HS256 rejected with the same
401 as 'never issued'. The frontend is expected to have
migrated to OAuth2 by then; the cutover is the safety net.

## Why PR-14 is just the contract

The contract is small, pure Java, and unit-testable. The
implementation needs:

- A real Authorization Server (Spring Authorization Server
  starter dep, application.yml config, login UI flow).
- A real `SecurityFilterChain` that calls `TokenBridge`.
- A real `AuthenticationManager` for the OAuth2 password
  grant.
- A migration of `User` / `UserDetails` into v2.

Those come with PR-15 (user migration) because the OAuth2
server needs a user table to authenticate against. Splitting
the work keeps each PR reviewable.

## Files in PR-14

| File | Role |
|---|---|
| `auth/api/Principal.java` | The unified principal. Equality by userId. Carries roles, token kind, expiresAt. |
| `auth/api/TokenBridge.java` | The contract every controller goes through. `resolve(authorizationHeader) -> Optional<Principal>` and `revoke(tokenId, ttlSeconds)`. |
| `auth/internal/RedisTokenBlacklist.java` | Redis `SET bl:{kind}:{tokenId} EX <ttl>`. Used by `revoke()` and by `TokenBridge.resolve()` before accepting any token. |
| `auth/internal/JwtTokenMigrator.java` | Translates the legacy HS256 token shape into a Principal. Hard-rejects past `RS256_CUTOFF_DATE`. |
| `auth/JwtTokenMigratorTest.java` | 8 unit tests covering the decision branches. Pure Java, no Spring. |

## What PR-14 deliberately does NOT do

- **Add Spring Authorization Server starter dep.** Comes with
  PR-15 when we have a real `User` to authenticate.
- **Wire `SecurityFilterChain`.** Comes with PR-15 (depends
  on user migration completing first).
- **Migrate login / SMS / OAuth2 password endpoints.** Stays
  on the legacy `system-sso` until PR-15 cuts over.
- **Touch the legacy `JwtTokenUtil`.** It keeps working. PR-14
  just produces the parallel `JwtTokenMigrator` so v2 can
  translate the same shape.
- **Add rate limiting** (V1.1 §4.1 P0 加固 third bullet).
  Belongs in PR-15 alongside the SMS / login controller
  migration, where the request path is right there to read.

## Test coverage

`JwtTokenMigratorTest` covers:

| Case | Expected |
|---|---|
| Valid HS256 token before cutoff | principal with userId, username, ROLE_USER |
| HS256 token with isAdmin=true | ROLE_ADMIN added |
| RS256 token | rejected (not migrator's path) |
| HS256 token past cutoff | rejected |
| Expired token | rejected |
| Missing nbf | rejected |
| Missing / zero userId | rejected |
| Principal equality is by userId, not token | different tokens for same user compare equal |

## What lands with PR-15 (the actual implementation)

PR-15 will add:

- `spring-boot-starter-oauth2-authorization-server` dependency
- `auth/internal/SecurityFilterChainConfig.java` wiring the
  new filter chain into v2
- `auth/internal/OAuth2TokenBridge.java` implementing
  `TokenBridge` for the new RS256 path
- `auth/internal/LogoutService.java` calling `TokenBridge.revoke`
- Login / SMS / refresh endpoints in v2
- `@PreAuthorize("hasRole('ADMIN')")` on every admin endpoint
- Rate limiting on login + SMS (Redis `INCR` per IP+phone)

PR-15 is where the controllers flip from the legacy
`com.ypat.config.WebSecurityConfig` to the v2 chain.

## References

- V1.0 §2.3.7 (current security config)
- V1.1 §1.2 row 3 (Cookie/JWT 双栈会话衔接)
- V1.1 §4.2 (双 Token 兼容窗口)
- V1.1 §4.4 (回调处理流 + 实名认证)
- Upgrade plan: PR-14 (this), PR-15 (implementation)