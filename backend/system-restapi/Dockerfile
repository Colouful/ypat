# Multi-stage build — 见 backend/system-wap/Dockerfile 里的说明。
# Context 必须是 backend/。

FROM maven:3.8-eclipse-temurin-8 AS build
WORKDIR /src

COPY pom.xml ./
COPY system-object/pom.xml system-object/
COPY system-domain/pom.xml system-domain/
COPY system-security/pom.xml system-security/
COPY system-sso/pom.xml system-sso/
COPY system-restapi/pom.xml system-restapi/
COPY system-web/pom.xml system-web/
COPY system-wap/pom.xml system-wap/
RUN mvn -pl system-restapi -am dependency:go-offline -B -DskipTests || true

COPY . .
RUN mvn -pl system-restapi -am package -DskipTests -B

FROM eclipse-temurin:8-jre
WORKDIR /app
RUN mkdir -p /tmp/ypat-logs
COPY --from=build /src/system-restapi/target/system-restapi-1.0-SNAPSHOT.jar app.jar
EXPOSE 9081
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
