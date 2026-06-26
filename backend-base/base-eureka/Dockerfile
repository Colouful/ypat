FROM eclipse-temurin:8-jre
WORKDIR /app
COPY target/base-eureka-1.0-SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
