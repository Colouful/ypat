#!/usr/bin/env bash
# scripts/freeze-legacy.sh
#
# PR-22 final: freeze the legacy system-wap / system-restapi
# into read-only mode at the Nginx layer (no Java recompile).
#
# The ypat-workspace docker-compose mounts
# ./docker/nginx/default.conf as :ro inside the nginx container,
# so we edit the host file and run `nginx -s reload` to pick
# up the change. The freeze block is written via a Python
# here-doc because the raw \" escapes survive correctly there
# (the bash -> sh -> awk chain eats backslashes).

set -euo pipefail

NGINX_CONTAINER="ypat-workspace-nginx-1"
HOST_CONF="/Users/lizhenwei/workspace/vueworkspace/ypat-workspace/docker/nginx/default.conf"
MARKER="__YPAT_LEGACY_FROZEN__"

cmd="${1:-}"
case "$cmd" in
  freeze)
    if grep -q "$MARKER" "$HOST_CONF"; then
      echo "freeze block already present; no-op"
    else
      echo "Inserting freeze block into $HOST_CONF"
      cp "$HOST_CONF" "${HOST_CONF}.bak"
      HOST_CONF="$HOST_CONF" MARKER="$MARKER" python3 - <<'PYEOF'
import os, re, pathlib
host = os.environ["HOST_CONF"]
marker = os.environ["MARKER"]
p = pathlib.Path(host)
src = p.read_text()
# Regular triple-quoted string. \" -> " in Python source;
# in the file it becomes \" which is what nginx needs.
# \n in the source becomes a real newline.
freeze = """    # PR-22 final: legacy freeze block (scripts/freeze-legacy.sh)
    location ~ ^/api/(.*)$ {
        if ($request_method !~ "^(GET|HEAD|OPTIONS)$") {
            add_header Content-Type application/json always;
            return 403 "{\\"error\\":\\"LEGACY_FROZEN\\"}";
        }
    }
"""
m = list(re.finditer(r"^\}\s*\Z", src, re.MULTILINE))
if not m:
    raise SystemExit("server closing brace not found")
last = m[-1]
new = src[:last.start()] + freeze + src[last.start():]
p.write_text(new)
print("freeze block inserted")
PYEOF
    fi
    docker exec "$NGINX_CONTAINER" nginx -t
    docker exec "$NGINX_CONTAINER" nginx -s reload
    echo "Legacy is now read-only. POST/PUT/DELETE/PATCH on /api/* returns 403."
    ;;
  unfreeze)
    if ! grep -q "$MARKER" "$HOST_CONF"; then
      echo "freeze block not present; no-op"
    else
      echo "Removing freeze block from $HOST_CONF"
      cp "$HOST_CONF" "${HOST_CONF}.bak"
      sed -i "/$MARKER/,/^    }$/d" "$HOST_CONF"
    fi
    docker exec "$NGINX_CONTAINER" nginx -t
    docker exec "$NGINX_CONTAINER" nginx -s reload
    echo "Legacy is writable again."
    ;;
  status)
    if grep -q "$MARKER" "$HOST_CONF"; then
      echo "FROZEN (legacy read-only)"
    else
      echo "WRITABLE (no freeze block)"
    fi
    ;;
  *)
    echo "Usage: $0 {freeze|unfreeze|status}"
    exit 64
    ;;
esac