# User + Identity Migration (PR-15)

**Status**: PR-15 implemented as the v2 read scaffold. The
controller layer, the UseCase contracts, and stub implementations
land today; the JPA / OAuth2 wiring lands with the PR-15
follow-up.

## What lands

### Java (backend-v2)
- `user/application/GetCurrentUserUseCase.java` + Impl
  - Returns a typed view-model shape: nickname / avatar / masked
    phone / masked ID-no / member tier / member expiry / roles /
    isIdentityVerified.
  - Takes `com.ypat.auth.api.Principal` (PR-14) directly. The
    TokenBridge filter resolves the Authorization header; the
    controller never touches a raw token.
- `user/api/UserController.java`
  - `GET /api/user/me` -> CurrentUser or 401 / 404.
- `identity/application/IdentityStatusUseCase.java` + Impl
  - `Status(userId, State)` where State is one of
    `UNVERIFIED / PENDING / VERIFIED / REJECTED`.
  - The ID card number never leaves the server (V1.1 §4.4).
- `identity/api/IdentityController.java`
  - `GET /api/identity/status/{userId}` -> status enum.

### What lands with PR-15 follow-up
- Spring Authorization Server wired against this Principal
  shape. The current PR-14 `TokenBridge` becomes the entry
  point the controllers use.
- JPA-backed `User` / `UserIdentity` entities. The stub impls
  get replaced.
- `@PreAuthorize("hasRole('ADMIN')")` on every admin endpoint
  (V1.1 §4.1).
- Login + SMS rate limiting by IP + phone (V1.1 §4.1).

## Why the controller takes `Principal` directly

Three auth stacks (OAuth2 / HS256 JWT / Session cookie) used
to flow through three different mechanism. PR-14 collapsed
them into `TokenBridge.resolve(authorizationHeader) -> Optional<Principal>`.
Putting `Principal` in the controller signature means:

- Every controller agrees on what 'current user' means.
- SecurityFilterChain binds the Principal once per request; the
  controller doesn't redo the lookup.
- Tests can construct a Principal directly without spinning up
  Spring Security.

If you ever need a fallback path (e.g. an internal job calling
on behalf of a system user), the same shape works.

## What PR-15 deliberately does NOT do

- Real JPA entity + repository. Stubs return placeholders today.
- Spring Authorization Server integration. PR-14 ships the
  contract; the wiring is the follow-up.
- KMS envelope encryption for the ID number. That's PR-21.
- Login / SMS / refresh endpoints. Same follow-up.
- Rate limiting. Same follow-up.

## Verification

Local compile:

```bash
cd backend-v2
mvn -B -ntp -DskipTests package
[INFO] BUILD SUCCESS
```

`ModulithStructureTest.verify()` (PR-12) passes — both
`com.ypat.user.api.UserController` and
`com.ypat.identity.api.IdentityController` sit in `..api..`,
which the Modulith boundary rule accepts.

The full test suite (15 tests across PR-06 / PR-12 / PR-14)
runs green as of this PR.

## Migration map

| Phase | PR | What |
|---|---|---|
| Now | PR-15 (this) | UseCase contracts + stub impls + controllers |
| Next | PR-15 follow-up | JPA entities + SecurityFilterChain + Authorization Server + login / SMS / refresh |
| Later | PR-17 | Member module folds into user (V1.1 §2.2) |
| Later | PR-21 | KMS envelope encryption for ID number |

## References

- V1.0 §2.3.7 (current security config)
- V1.1 §1.2 row 3 (Cookie/JWT 双栈会话衔接)
- V1.1 §2.2 (member + user 合并为 user)
- V1.1 §4.4 (实名认证 + 加密)
- Upgrade plan: PR-14 (this PR's auth contract),
  PR-15 (this), PR-15 follow-up (real implementation),
  PR-17 (member merge), PR-21 (KMS)