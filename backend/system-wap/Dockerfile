# Multi-stage build — 自包含镜像构建，不再依赖构建方预先跑过 mvn package。
# 见 docs/deploy/LESSONS.md 2026-07-03 事故：本地打的 jar 忘了传上去，
# 服务器上 docker build 拿到旧 jar 导致新代码"看起来部署了但没生效"。
#
# 构建 context 是 backend/（父 pom 所在处）。docker-compose.yml 里要写：
#   build:
#     context: ./backend
#     dockerfile: system-wap/Dockerfile

# ---------- Build stage ----------
FROM maven:3.8-eclipse-temurin-8 AS build
WORKDIR /src

# 先只 COPY 所有模块的 pom.xml，让依赖下载步骤能被 Docker 层缓存
COPY pom.xml ./
COPY system-object/pom.xml system-object/
COPY system-domain/pom.xml system-domain/
COPY system-security/pom.xml system-security/
COPY system-sso/pom.xml system-sso/
COPY system-restapi/pom.xml system-restapi/
COPY system-web/pom.xml system-web/
COPY system-wap/pom.xml system-wap/
RUN mvn -pl system-wap -am dependency:go-offline -B -DskipTests || true

# 源码
COPY . .

RUN mvn -pl system-wap -am package -DskipTests -B

# ---------- Runtime stage ----------
FROM eclipse-temurin:8-jre
WORKDIR /app
RUN mkdir -p /tmp/ypat-logs
COPY --from=build /src/system-wap/target/system-wap-1.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
