# Legacy Service Decommission Plan (PR-22)

**Status**: PR-22 prep. This PR does NOT delete legacy code —
it lands the runbook, the read-only freeze script, and the
checklist that the actual decommission (PR-22 final) executes
against.

## Why this PR exists first

The original PR-22 in the plan was "delete backend/ + backend-base/
and verify nothing breaks." That single-shot deletion is too
risky:

- If even one shared util (`com.ypat.util.FastDFSClient`,
  `com.ypat.util.JwtTokenUtil`, `com.ypat.config.SecurityConfiguration`)
  is still referenced from a Spring component scan, the whole
  Spring context fails to start.
- The legacy Eureka / Zuul / Feign clients are spread across
  four modules; missing one import means silent runtime failure,
  not compile error.

PR-22 splits that into two halves:

1. **This PR (decommission prep)** — the runbook + the
   read-only freeze mechanism + the smoke-test script. Lands
   while the legacy code is still on disk and serving some
   traffic.
2. **PR-22 final** (later, NOT in this commit) — flips the
   switch: deletes backend/ and backend-base/ and verifies
   the rest of the repo still builds.

## Pre-conditions for PR-22 final

ALL of these must be true before PR-22 final opens:

| # | Gate | Evidence |
|---|---|---|
| 1 | All Nginx prefixes route 100% to v2 | Nginx access log shows zero hits on legacy upstreams for 7 consecutive days |
| 2 | Every module cut over | work / user / auth / appointment / member / wallet / payment / identity / notification / content — see cutover table below |
| 3 | Legacy has been in read-only mode for ≥ 30 days | PR-22 prep script ran without 5xx for that long |
| 4 | CodeQL clean on backend-v2 | No HIGH or CRITICAL findings |
| 5 | E2E coverage of business flows from frontend | Existing E2E suite passes against v2 endpoints only |
| 6 | Compliance sign-off | Risk / Legal / Finance three-party sign-off recorded |

## Module cut-over status (target for PR-22 final)

| Module | Migration PR | Owner | Cut-over date | Status |
|---|---|---|---|---|
| content (read) | PR-11 | backend | TBD | pending |
| work (read) | PR-11 | backend | TBD | pending |
| work (write) | PR-15+ | backend | TBD | pending |
| user + identity | PR-15 | backend | TBD | pending |
| auth | PR-14 | backend | TBD | pending |
| appointment | PR-16 | backend | TBD | pending |
| member (folded into user) | PR-17 | backend | TBD | pending |
| wallet | PR-19 | backend | TBD | pending |
| payment | PR-20 | backend | TBD | pending |
| identity (实名) | PR-21 | backend | TBD | pending |
| notification | PR-18 | backend | TBD | pending |
| storage (write) | PR-10 | backend | TBD | pending |
| audit | (none) | backend | TBD | pending |

## What this PR ships

### Runbook (this file)

The cut-over sequence, the rollback sequence, the smoke-test
script, the read-only-freeze mechanism.

### scripts/legacy-freeze.sh

A shell script that flips the legacy backend into read-only mode
without redeploying:

```bash
#!/usr/bin/env bash
# scripts/legacy-freeze.sh
#
# Switches every writable controller method in system-wap /
# system-restapi / system-web into a 403 'service migrated'
# response, without bringing the JVM down. Achieved by toggling
# a feature flag read from EnvironmentConfigurationValidator.
#
# Run on each legacy JVM:
#   YPAT_LEGACY_FROZEN=true ./scripts/legacy-freeze.sh
# Or to unfreeze:
#   YPAT_LEGACY_FROZEN=false ./scripts/legacy-freeze.sh
```

(Full implementation lands with PR-22 final. This PR lands the
script skeleton so the runbook can reference it.)

### scripts/legacy-smoke.sh

```bash
#!/usr/bin/env bash
# scripts/legacy-smoke.sh
#
# Walks the legacy /api tree and verifies every endpoint
# returns either:
#   - 2xx/3xx (still serving legacy traffic — recheck Nginx
#     upstream weight)
#   - 403 with body {"error":"LEGACY_FROZEN"} (expected, the
#     legacy-freeze flag is on)
# Anything else is a smoke-test failure.
```

### docs/architecture/legacy-decommission-checklist.md

A printable checklist the operator ticks off during the
PR-22 final PR review:

- [ ] backend-v2 has been on 100% traffic for 7+ days
- [ ] legacy-freeze.sh ran without 5xx for 30+ days
- [ ] CodeQL backend-v2 clean (no HIGH/CRITICAL)
- [ ] frontend E2E green against v2 endpoints only
- [ ] three-party compliance sign-off
- [ ] Eureka/Config Server/Hystrix dashboard deprecated
- [ ] runbook reviewed by two operators (not just the author)

## Cut-over sequence (Nginx)

Phased per V1.1 §7.2 — the same `^~` exact-prefix rules. Each
phase uses a 24-hour burn-in:

```
Day 1  : /api/content/*        5%  -> v2, 95% -> legacy
Day 2  : /api/content/*       25%  -> v2
Day 3  : /api/content/*       50%  -> v2
Day 4  : /api/content/*      100%  -> v2
Day 5  : /api/work/list       5%   -> v2 (read-only first)
Day 6  : /api/work/list      25%   -> v2
... (same pattern for each prefix)
Day N  : all prefixes        100%  -> v2
```

The actual nginx.conf lives under `deploy/nginx/`. Each phase
is a single `nginx -s reload` with a new upstream weights file.

## Rollback (5-minute target)

Every Nginx upstream block has a `backup` upstream pointing at
the legacy instance. The legacy instance is kept running
(read-only) for 30 days after 100% v2. If v2 starts emitting
5xx at scale:

```bash
# 1. Snapshot weights
cp deploy/nginx/conf.d/upstreams.conf deploy/nginx/conf.d/upstreams.last-good

# 2. Push the rollback config
sed -i 's/v2_weight = 100/v2_weight = 0/' deploy/nginx/conf.d/upstreams.conf
nginx -t && nginx -s reload

# 3. Verify
curl -fsS -o /dev/null -w "%{http_code}\n" https://api.example.com/api/work/list
# expect: 200, served by legacy upstream
```

Target: full rollback under 30 seconds (Nginx reload only;
no JVM restart).

## What PR-22 prep deliberately does NOT do

- Delete backend/ or backend-base/. That is the actual PR-22
  final, gated on every pre-condition above.
- Modify any application code. PR-22 prep is runbook + script
  skeleton only.
- Force the legacy JVMs to read the new freeze flag yet. The
  EnvironmentConfigurationValidator hook for
  `YPAT_LEGACY_FROZEN` lands with PR-22 final.

## Operator pre-flight (when PR-22 final opens)

1. Confirm every row in the cut-over status table above is
   "complete".
2. Run `scripts/legacy-smoke.sh` against staging. All 403s
   should be `LEGACY_FROZEN`, no 5xx.
3. Roll a 1-hour v2-only window in staging (Nginx weight 100%
   on v2). If the smoke-test passes for the full hour, proceed
   to production.
4. Production cut: same sequence as staging, but each phase
   step takes 24 hours instead of an hour. Total 2-3 weeks.
5. After 100% v2, run `legacy-freeze.sh` on every legacy JVM.
6. 30 days later, run `git rm -r backend backend-base` and
   open the deletion PR (PR-22 final).

## References

- V1.1 §7.2 (Nginx 接口级切流)
- V1.1 §7.3 (5 分钟接口级回滚)
- Upgrade plan: PR-22 (this prep), PR-22 final (later)