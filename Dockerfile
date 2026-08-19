# ── Stage 1: Build ────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY catalog-service/pom.xml catalog-service/pom.xml
COPY qa-workstation/pom.xml qa-workstation/pom.xml
COPY catalog-service catalog-service
COPY qa-workstation qa-workstation

RUN mvn -q -pl catalog-service -am package -DskipTests

# ── Stage 2: Runtime ─────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/catalog-service/target/catalog-service-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=75.0 -jar app.jar"]
