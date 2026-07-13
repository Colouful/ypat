#!/usr/bin/env bash
# scripts/legacy-freeze.sh
#
# Toggles every legacy JVM into read-only mode without a restart.
# Reads YPAT_LEGACY_FROZEN from the environment and forwards it
# to the running JVM via the Spring Boot actuator env endpoint,
# which EnvironmentConfigurationValidator already understands
# (PR-22 final wires the YPAT_LEGACY_FROZEN key).
#
# Status (PR-22 prep):
#   - The shell contract is final.
#   - The actuator env endpoint path is final.
#   - The YPAT_LEGACY_FROZEN handler in
#     EnvironmentConfigurationValidator lands with PR-22 final,
#     so this script will exit non-zero on a JVM that hasn't
#     yet upgraded. Operators running this script before PR-22
#     final lands should expect that.

set -euo pipefail

usage() {
  cat <<'USAGE'
Usage: YPAT_LEGACY_FROZEN=true|false ./scripts/legacy-freeze.sh <actuator-base>

Where <actuator-base> is something like http://localhost:9081 for
system-restapi. Repeat for every legacy JVM you want to flip.

The script POSTs the new value to the Spring Boot actuator
env endpoint, which Spring applies without a restart.
USAGE
}

if [[ $# -ne 1 ]]; then
  usage
  exit 64
fi
TARGET="$1"

if [[ -z "${YPAT_LEGACY_FROZEN:-}" ]]; then
  echo "ERROR: YPAT_LEGACY_FROZEN must be set (true or false)" >&2
  exit 64
fi

URL="${TARGET}/actuator/env"
PAYLOAD="{\"name\":\"YPAT_LEGACY_FROZEN\",\"value\":${YPAT_LEGACY_FROZEN}}"

echo "Setting YPAT_LEGACY_FROZEN=${YPAT_LEGACY_FROZEN} on ${TARGET}"
curl -fsS \
  -H 'Content-Type: application/json' \
  -X POST \
  --data "${PAYLOAD}" \
  "${URL}"
echo

# Reset the affected scope so the change takes effect on the next
# request, not on JVM restart.
curl -fsS \
  -X POST \
  "${TARGET}/actuator/refresh" \
  || true
echo "Done."