#!/usr/bin/env bash
# tools/ddl/verify-index.sh
#
# Post-migration smoke test for V2__work_covering_index.
# Confirms EXPLAIN on the work-list query now uses
# idx_city_status_pub and does not filesort.
#
# Run after applying V2__work_covering_index.sql.

set -euo pipefail

if [[ -z "${YPAT_MYSQL_HOST:-}" || -z "${YPAT_MYSQL_PASSWORD:-}" ]]; then
  echo "ERROR: YPAT_MYSQL_HOST and YPAT_MYSQL_PASSWORD must be set." >&2
  exit 1
fi

MYSQL_CMD="mysql -h $YPAT_MYSQL_HOST -u ${YPAT_MYSQL_USERNAME:-root} -p$YPAT_MYSQL_PASSWORD ypat --table"

echo "=== EXPLAIN on the work-list hot-path query ==="
$MYSQL_CMD -e "
EXPLAIN
SELECT id, userid, status, publish_time, city
  FROM t_work
 WHERE city = 'shanghai'
   AND status = 'PUBLISHED'
   AND deleted_flag = 0
 ORDER BY publish_time DESC, id DESC
 LIMIT 20;
"

echo
echo "=== Expected ==="
echo "  - key: idx_city_status_pub"
echo "  - Extra: Backward index scan; no 'Using filesort'"
echo "  - rows: bounded (proportional to matching rows, not total table size)"
echo
echo "If you see 'Using filesort' or key=NULL, the index is not being picked."
echo "Confirm ANALYZE TABLE t_work; has been run recently."