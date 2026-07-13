#!/usr/bin/env bash
# tools/check-ddl-auto.sh
#
# Static guard: no backend application*.yml may declare
# `ddl-auto: update` (or `create` / `create-drop`).
#
# PR-01a switched every JPA-owning module from `update` to `none`.
# This script makes that contract enforceable in CI so no future
# PR can silently re-introduce auto-mutating Hibernate behavior.
#
# Allowed values (case-sensitive after trimming):
#   - validate (preferred, see PR-01b)
#   - none     (current state, used by system-restapi + system-wap)
#
# Exit code:
#   0  clean
#   1  forbidden value found, prints offenders
#
# Usage:
#   ./tools/check-ddl-auto.sh

set -euo pipefail

# Directories we care about. system-domain and system-object are
# shared jars; system-sso already uses `none`; only system-restapi
# and system-wap have been touched in PR-01a.
SEARCH_PATHS=(
  "backend/system-restapi/src/main/resources"
  "backend/system-wap/src/main/resources"
)

FORBIDDEN_PATTERN='ddl-auto:[[:space:]]*(update|create|create-drop)[[:space:]]*$'

OFFENDERS=()
for path in "${SEARCH_PATHS[@]}"; do
  if [[ ! -d "${path}" ]]; then
    continue
  fi
  # grep -RHn: recursive, filename, line number.
  # Match only inside YAML keys (lines starting with whitespace then `ddl-auto:`).
  # We don't use ripgrep; portability > cleverness.
  while IFS=: read -r file lineno matched; do
    OFFENDERS+=("${file}:${lineno}: ${matched}")
  done < <(grep -RHnE "${FORBIDDEN_PATTERN}" "${path}" 2>/dev/null || true)
done

if (( ${#OFFENDERS[@]} > 0 )); then
  echo "✗ Forbidden ddl-auto value detected." >&2
  echo "  Hibernate must NOT auto-mutate schema. Allowed: validate | none." >&2
  echo >&2
  printf '  %s\n' "${OFFENDERS[@]}" >&2
  echo >&2
  echo "  See docs/architecture/schema-migration.md for the rationale." >&2
  exit 1
fi

echo "✓ ddl-auto guard clean (no update / create / create-drop found)"