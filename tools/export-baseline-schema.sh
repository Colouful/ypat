#!/usr/bin/env bash
# tools/export-baseline-schema.sh
#
# Exports the current production MySQL schema as Flyway's V1 baseline.
# PR-01a introduces this script; PR-07 will run it for real once we
# have agreed on the cutover date.
#
# Why this script exists:
#   We just switched ddl-auto from `update` to `none`. That stops
#   Hibernate from mutating schema, but it also means we lose the
#   "auto-create new columns" safety net. The fix is Flyway — and
#   Flyway needs a baseline of what the schema looks like TODAY
#   before any migration kicks in.
#
# Output:
#   db/migration/mysql/V1__baseline_schema.sql
#
# Usage:
#   ./tools/export-baseline-schema.sh
#
# Required env (from .env or shell):
#   YPAT_MYSQL_HOST       default: 127.0.0.1
#   YPAT_MYSQL_PORT       default: 3306
#   YPAT_MYSQL_DB         default: ypat
#   YPAT_MYSQL_USERNAME   default: root
#   YPAT_MYSQL_PASSWORD   required

set -euo pipefail

HOST="${YPAT_MYSQL_HOST:-127.0.0.1}"
PORT="${YPAT_MYSQL_PORT:-3306}"
DB="${YPAT_MYSQL_DB:-ypat}"
USER="${YPAT_MYSQL_USERNAME:-root}"

if [[ -z "${YPAT_MYSQL_PASSWORD:-}" ]]; then
  echo "ERROR: YPAT_MYSQL_PASSWORD is not set. Aborting." >&2
  exit 1
fi

OUT_DIR="db/migration/mysql"
OUT_FILE="${OUT_DIR}/V1__baseline_schema.sql"

mkdir -p "${OUT_DIR}"

echo "Exporting baseline schema from ${USER}@${HOST}:${PORT}/${DB}"
echo "→ ${OUT_FILE}"

# --no-data: structure only, no rows.
# --skip-comments: keep the file small and diff-friendly.
# --skip-set-charset / --compact: don't reset per-session charset.
# Strip AUTO_INCREMENT=N from CREATE TABLE so reruns produce a
# byte-identical baseline (avoids spurious diffs when re-exported).
mysqldump \
  --host="${HOST}" \
  --port="${PORT}" \
  --user="${USER}" \
  --password="${YPAT_MYSQL_PASSWORD}" \
  --no-data \
  --skip-comments \
  --skip-set-charset \
  --compact \
  --skip-add-drop-table \
  "${DB}" \
  | sed -E 's/AUTO_INCREMENT=[0-9]+ //g' \
  > "${OUT_FILE}"

# Sanity check: must produce a non-empty file with CREATE TABLE.
if [[ ! -s "${OUT_FILE}" ]]; then
  echo "ERROR: ${OUT_FILE} is empty. mysqldump likely failed." >&2
  exit 1
fi
if ! grep -q "CREATE TABLE" "${OUT_FILE}"; then
  echo "ERROR: ${OUT_FILE} contains no CREATE TABLE. Check credentials." >&2
  exit 1
fi

LINE_COUNT=$(wc -l < "${OUT_FILE}")
echo "OK: ${LINE_COUNT} lines written to ${OUT_FILE}"
echo "Next: review the diff and commit alongside PR-07 (Flyway baseline)."