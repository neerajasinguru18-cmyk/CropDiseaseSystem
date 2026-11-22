FROM eclipse-temurin:25-jre

WORKDIR /app/backend/backend

COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/data /app/uploads

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
