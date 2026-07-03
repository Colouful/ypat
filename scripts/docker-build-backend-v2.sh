#!/usr/bin/env bash
# scripts/docker-build-backend-v2.sh
#
# Builds the backend-v2 Spring Boot jar locally (host JDK) and
# then builds the runtime Docker image, copying the jar into
# the build context so Docker doesn't have to reach Maven
# Central during the image build (which is flaky from inside
# Docker Desktop on this network).

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "==> Building jar on host (host JDK 17, see backend-v2/pom.xml)"
( cd backend-v2 && mvn -B -ntp -DskipTests package )

JAR_SRC=backend-v2/target/backend-v2-1.0.0-SNAPSHOT.jar
if [ ! -f "$JAR_SRC" ]; then
  echo "ERROR: $JAR_SRC not produced" >&2
  exit 1
fi

# Drop the jar at the build-context root so the runtime
# Dockerfile can COPY it without ARGs.
cp "$JAR_SRC" backend-v2/app.jar

echo "==> Building runtime image (no Maven inside Docker)"
docker build -t ypat-backend-v2:dev -f backend-v2/Dockerfile.runtime backend-v2

echo
echo "Image built: ypat-backend-v2:dev"
echo "Run with: scripts/docker-run-backend-v2.sh"