# Multi-stage build — 见 backend/system-wap/Dockerfile 里的说明。
# Context 必须是 backend-base/ 才能访问父 pom。

FROM maven:3.8-eclipse-temurin-8 AS build
WORKDIR /src

COPY pom.xml ./
COPY base-config/pom.xml base-config/
COPY base-eureka/pom.xml base-eureka/
COPY base-hystrix/pom.xml base-hystrix/
COPY base-turbine/pom.xml base-turbine/
COPY base-zipkin/pom.xml base-zipkin/
RUN mvn -pl base-eureka -am dependency:go-offline -B -DskipTests || true

COPY . .
RUN mvn -pl base-eureka -am package -DskipTests -B

FROM eclipse-temurin:8-jre
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /src/base-eureka/target/base-eureka-1.0-SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
