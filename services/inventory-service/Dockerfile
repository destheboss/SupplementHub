FROM eclipse-temurin:17-jre

WORKDIR /app

COPY build/libs/*.jar app.jar

RUN useradd -r -u 10001 appuser

USER appuser

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]