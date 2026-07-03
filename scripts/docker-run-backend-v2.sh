#!/usr/bin/env bash
# scripts/docker-run-backend-v2.sh
#
# Runs backend-v2 against the running ypat-workspace stack.
# Connects to the ypat_ypat-net Docker network so backend-v2
# can talk to mysql / redis by hostname.

set -euo pipefail

NETWORK=ypat_ypat-net
if ! docker network inspect "$NETWORK" >/dev/null 2>&1; then
  echo "ERROR: network $NETWORK does not exist. Start ypat-workspace first:" >&2
  echo "  cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace && docker compose up -d" >&2
  exit 1
fi

docker run --rm \
  --name ypat-backend-v2-dev \
  --network "$NETWORK" \
  -p 127.0.0.1:8092:8082 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e YPAT_COS_REGION=ap-guangzhou \
  -e YPAT_COS_BUCKET=ypat-dev-placeholder \
  -e YPAT_COS_SECRET_ID=dev-placeholder-id \
  -e YPAT_COS_SECRET_KEY=dev-placeholder-key \
  ypat-backend-v2:dev