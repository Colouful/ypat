# Member Merge into User (PR-17)

**Status**: PR-17 implemented as the **architectural decision**
+ a stub sub-domain UseCase. Real table merge is a later PR
once V1.0 §2.3.6 schema drift is fixed (PR-01b follow-up) and
a Flyway migration can fold `t_user_member` / `t_member_plan`
into `t_user`.

## What this PR does

V1.1 §2.2 says member and user should be one module. The
backend-v2 PR-06 (PR-06) shipped a `com.ypat.member` package
alongside `com.ypat.user`. PR-17 acknowledges the merge in
code:

1. `com.ypat.member` package-info now declares itself a
   **sub-domain** of user — the canonical entry is
   `com.ypat.user.application.*`, and member is reached through
   user.
2. `MembershipTier` UseCase lands at `com.ypat.member.application.*`
   so legacy callers can resolve it through Modulith's
   `@NamedInterface`. Future migration: move this class to
   `com.ypat.user.application.MembershipTier` once the schema
   merge is done.

## What this PR does NOT do

- **Database merge.** The legacy schema has separate tables
  (`t_user`, `t_user_member`, `t_member_plan`,
  `t_member_order`). Folding them requires a Flyway migration
  that backfills `t_user` columns and drops the others —
  that's a multi-PR effort after PR-19 (wallet) lands, because
  the tier change history is auditable and must roll up to the
  wallet ledger.
- **CurrentUser integration.** `GetCurrentUserUseCase` (PR-15)
  already returns `memberTier` and `memberExpiry` as fields on
  `CurrentUser`. Wiring them to the real `MembershipTier`
  service (instead of the placeholder values from PR-15's stub)
  is part of the PR-17 follow-up that lands when the schema
  merge does.

## Why the empty module is kept

Two reasons:

1. **Modulith boundary discipline.** PR-06 created 11 module
   roots, each with a package-info. Removing the `com.ypat.member`
   root before the schema merge creates an orphan sub-package
   that the legacy code still references (it imports
   `com.ypat.service.MemberService`, etc.). Until the database
   columns are merged, we need the package as a sink for code
   we don't want to spread across the user module.
2. **Verification surface.** The stub `MembershipTierImpl` is
   part of this PR so Modulith's `verify()` (PR-12) sees a real
   class behind every `com.ypat.member` package — important for
   catching accidental empty-module regressions later.

## Verification

Local compile + Modulith `verify()`:

```bash
cd backend-v2
mvn -B -ntp test
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

The docker smoke test (PR-23) is unchanged — backend-v2 boots,
applies the Flyway baseline, and serves `/api/work/list`.

## Migration map

| Phase | PR | What |
|---|---|---|
| Now | PR-17 (this) | Architectural decision + stub MembershipTier UseCase |
| Next | PR-17 follow-up | MembershipTier returns real values; CurrentUser pulls tier through it |
| Later | schema merge PR | Flyway migration folds t_user_member / t_member_plan into t_user |
| Later | package delete PR | com.ypat.member package removed; com.ypat.user.application.MembershipTier is the only home |

## References

- V1.1 §2.2 (member + user → user)
- Upgrade plan: PR-17 (this), PR-17 follow-up, schema merge PR