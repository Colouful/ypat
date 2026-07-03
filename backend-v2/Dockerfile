# backend-v2 Dockerfile.
#
# Two-phase build: Maven build is done on the host (see
# scripts/docker-build-backend-v2.sh). The Docker stage here
# only does the runtime image.
#
# Why split: the docker build VM intermittently loses mid-stream
# downloads from repo.maven.apache.org. Building on the host
# (which has cached deps from previous `mvn package` runs)
# avoids the network race. The runtime image is independent
# of where the jar was produced.

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# The Spring Boot fat jar is produced on the host by
# scripts/docker-build-backend-v2.sh, which `docker cp`s the
# jar in or `docker run --mount`s the target/ directory.
COPY backend-v2-1.0.0-SNAPSHOT.jar /app/app.jar

# Default profile is `docker` so the container picks up
# application-docker.yml overrides (MySQL / Redis hostnames,
# COS env overrides).
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]