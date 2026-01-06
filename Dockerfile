FROM eclipse-temurin:25-jdk
LABEL authors="felipe.ooliveira"

WORKDIR /app

COPY target/flow-guard-service.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
