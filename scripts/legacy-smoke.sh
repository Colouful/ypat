#!/usr/bin/env bash
# scripts/legacy-smoke.sh
#
# Walks the legacy /api tree and verifies every endpoint returns
# either 2xx/3xx (still serving) or 403 with body
# {"error":"LEGACY_FROZEN"} (frozen). Anything else fails.
#
# Status (PR-22 prep):
#   - The endpoint list is intentionally hand-written. Auto-
#     discovery via OpenAPI would require the baseline OpenAPI
#     from PR-00 + a generated test runner, which is overkill
#     for a 30-day decommission task.
#   - Update this list when modules move. PR-11 starts the
#     content module; delete /api/content/* from this file when
#     v2 owns the prefix.

set -uo pipefail

TARGET="${1:-http://localhost:9081}"
FAIL=0

# Endpoints to check. method + path + (optional) auth header.
ENDPOINTS=(
  "GET /api/auth/captcha"
  "GET /api/ypat/get?id=1"
  "GET /api/ypat/tc/list"
  "GET /api/ypat/zx/list"
  "GET /api/work/list?city=shanghai"
  "GET /api/work/get?id=1"
  "GET /api/banner/list"
  "GET /api/article/list"
  "GET /api/article/get?id=1"
  "GET /api/dict/work-tag"
  "GET /api/area/list"
  "GET /api/tmplid/list"
  "GET /api/param/list"
  "GET /api/product/list"
)

for entry in "${ENDPOINTS[@]}"; do
  read -r method path <<<"$entry"
  url="${TARGET}${path}"
  body="$(curl -sS -o /tmp/legacy-smoke.body -w '%{http_code}' -X "$method" "$url" || echo 'curl-error')"
  case "$body" in
    2*|3*)
      : # still serving — operator must verify v2 already owns
        # this prefix before running the freeze
      ;;
    403)
      if grep -q '"LEGACY_FROZEN"' /tmp/legacy-smoke.body 2>/dev/null; then
        echo "OK (frozen)  $method $path -> $body"
      else
        echo "FAIL          $method $path -> $body (403 without LEGACY_FROZEN body)"
        FAIL=$((FAIL+1))
      fi
      ;;
    *)
      echo "FAIL          $method $path -> $body"
      FAIL=$((FAIL+1))
      ;;
  esac
done

if [[ "$FAIL" -gt 0 ]]; then
  echo
  echo "$FAIL endpoint(s) failed the smoke test."
  exit 1
fi

echo
echo "All endpoints OK."