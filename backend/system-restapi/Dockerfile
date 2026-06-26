FROM eclipse-temurin:8-jre
WORKDIR /app
RUN mkdir -p /tmp/ypat-logs
COPY target/system-restapi-1.0-SNAPSHOT.jar app.jar
EXPOSE 9081
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
