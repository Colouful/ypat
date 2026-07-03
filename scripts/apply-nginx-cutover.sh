#!/usr/bin/env bash
# scripts/apply-nginx-cutover.sh
#
# PR-22 final: this script is the operator runbook for the
# 5% / 25% / 50% / 100% backend-v2 cut-over.
#
# Usage:
#   BACKEND_V2_PERCENT=5   ./scripts/apply-nginx-cutover.sh apply
#   BACKEND_V2_PERCENT=25  ./scripts/apply-nginx-cutover.sh apply
#   ./scripts/apply-nginx-cutover.sh rollback
#   ./scripts/apply-nginx-cutover.sh status
#
# What it does:
#   apply N    -> patches deploy/nginx/v2/default.conf so all three
#                 split_clients blocks are at N% v2 / (100-N)% legacy,
#                 then writes that to the running ypat_ypat-net
#                 nginx container.
#   rollback   -> sets the percentages back to 0 (100% legacy).
#   status     -> prints the current v2 weight from the running config.
#
# Requires:
#   - ypat-workspace's ypat_ypat-net nginx container is running
#   - the current user can `docker exec` into that container

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
V2_CONF="$ROOT/deploy/nginx/v2/default.conf"
RUNNING_NGINX_CONF="/etc/nginx/conf.d/default.conf"
NGINX_CONTAINER="ypat-workspace-nginx-1"

usage() {
  cat <<EOF
Usage:
  BACKEND_V2_PERCENT=N  $0 apply    # N in {5, 25, 50, 100}
  $0 rollback                       # 0% v2 (100% legacy)
  $0 status                         # show current v2 weight
EOF
  exit 64
}

patch_percent() {
  local pct="$1"
  python3 - "$V2_CONF" "$pct" <<'PY'
import re, sys
path, pct = sys.argv[1], sys.argv[2]
src = open(path).read()
def repl(m):
    rest = m.group(0).split("\n", 1)[1]
    return f"{m.group(0).splitlines()[0].split()[0]} {pct}%;{rest}" if rest else f"{m.group(0).splitlines()[0].split()[0]} {pct}%;"
# Replace the "5%   ypat_v2_backend;" line in every split_clients
# block with the new percentage.
new = re.sub(r"(\d+%)(\s+ypat_v2_backend;)", f"{pct}%\\2", src)
# The repl above uses f-string; just rewrite with literal.
PY
python3 - "$V2_CONF" "$pct" <<'PY2'
import re, sys
path, pct = sys.argv[1], sys.argv[2]
src = open(path).read()
new = re.sub(r"(\s*)(\d+)%(\s+ypat_v2_backend;)",
             lambda m: f"{m.group(1)}{pct}%  ypat_v2_backend;", src)
open(path, "w").write(new)
PY2
}

cmd="${1:-}"
case "$cmd" in
  apply)
    pct="${BACKEND_V2_PERCENT:-5}"
    if [[ ! "$pct" =~ ^(5|25|50|100)$ ]]; then
      echo "ERROR: BACKEND_V2_PERCENT must be one of 5, 25, 50, 100 (got '$pct')" >&2
      exit 1
    fi
    echo "Patching v2 config to ${pct}%..."
    patch_percent "$pct"
    echo "Copying to running nginx container..."
    docker cp "$V2_CONF" "${NGINX_CONTAINER}:${RUNNING_NGINX_CONF}"
    echo "Testing config..."
    docker exec "$NGINX_CONTAINER" nginx -t
    echo "Reloading..."
    docker exec "$NGINX_CONTAINER" nginx -s reload
    echo "OK. v2 weight is now ${pct}%."
    ;;
  rollback)
    echo "Rolling back to 0% (100% legacy)..."
    patch_percent "0"
    docker cp "$V2_CONF" "${NGINX_CONTAINER}:${RUNNING_NGINX_CONF}"
    docker exec "$NGINX_CONTAINER" nginx -t
    docker exec "$NGINX_CONTAINER" nginx -s reload
    echo "OK. v2 weight is 0% (all traffic on legacy wap)."
    ;;
  status)
    docker exec "$NGINX_CONTAINER" sh -c "cat $RUNNING_NGINX_CONF" \
      | grep -E "^\s*[0-9]+%\s+ypat_v2_backend" \
      | head -1
    ;;
  *) usage ;;
esac