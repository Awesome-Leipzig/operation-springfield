FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src src

RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/target/sector-7g-safety-ledger-1.0.0.jar app.jar

# Phase 5 telemetry: Application Insights Java agent, zero code changes. The agent
# auto-instruments the JVM and reads APPLICATIONINSIGHTS_CONNECTION_STRING (set by
# infra/resources.bicep on the Container App) at startup; if that env var is unset
# (e.g. plain `docker run` locally) the agent runs as a harmless no-op.
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.7.9/applicationinsights-agent-3.7.9.jar /app/applicationinsights-agent.jar
ENV JAVA_TOOL_OPTIONS="-javaagent:/app/applicationinsights-agent.jar"

ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
