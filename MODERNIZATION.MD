# MODERNIZATION TIME CAPSULE (2026)

If you are reading this next year: the app is no longer a museum piece.  
It was intentionally legacy, then modernized in one focused pass using the modernization plans, triage, and KB guidance.

## Where we started

- Java 8 + Spring Boot 2.3.12 (both out of support)
- `javax.*` APIs in JPA and servlet filter code
- SpringFox 2.9 Swagger config
- JUnit 4 tests
- Hardcoded credentials and API keys in code/config
- No Dockerfile or Azure container deployment manifest

Assessment artifacts used:

- `.github/modernize/springfield-upgrade-and-containerization/plan.md`
- `TRIAGE.md`
- assessment report `report-20260714101315`

## Knowledge-base tracks we followed

From the modernization plan (kb-backed):

- `java-version-upgrade`
- `spring-framework-upgrade`
- `spring-boot-upgrade`
- `containerization-copilot-agent`

From triage/security findings that were also addressed in the same modernization wave:

- `azure-password-01000`
- `cra-hardcoded-credential-password-01000`
- `cra-hardcoded-credential-apikey-02000`
- `jakarta-database-00002`

## What happened (and how)

### 1) Runtime and framework uplift

- `pom.xml` moved to Spring Boot **4.0.0**
- Java target moved to **21 LTS** (supported and validated in this environment)
- Legacy dependency set cleaned:
  - SpringFox removed
  - `springdoc-openapi-starter-webmvc-ui` added
  - old vulnerable utility libs removed

### 2) Jakarta namespace migration

- All `javax.persistence.*` imports migrated to `jakarta.persistence.*`
- Servlet filter migrated from `javax.servlet.*` to `jakarta.servlet.*`
- Verified no remaining `javax.*` imports under `src/`

### 3) Security and credential hardening

- `SecretConstants.java` deleted
- Hardcoded credentials removed from `application.properties`
- Secrets externalized through environment-backed placeholders:
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
  - `PLANT_API_KEY`
- Legacy backdoor token logic removed from `LegacyAuditFilter`
- `System.out` logging replaced with SLF4J logging

### 4) Code modernization for maintainability

- JUnit 4 tests migrated to JUnit 5
- Services updated toward safer contracts (`Optional`, service-layer boundaries)
- `java.util.Date`/`SimpleDateFormat` usage replaced with `java.time` (`Instant`, `DateTimeFormatter`)
- Legacy collection/string patterns modernized

### 5) Azure/container readiness

- Added `Dockerfile` (multi-stage Maven build + JRE runtime)
- Added `.dockerignore`
- Added `azure-container-app.yaml` with Key Vault secret references and env wiring
- `server.port` now supports `SERVER_PORT` override for container hosting

## Verification record

Two key validations were executed:

1. `mvn clean verify` (with JDK 21) -> **BUILD SUCCESS**
2. `mvn spring-boot:run` (with JDK 21) -> app started successfully on port 8080, repositories initialized, seed data loaded

## Important note for next year

The repo modernization target in org guidance mentions Java 25, but this run locked to **Java 21 LTS** because that is what was available and fully verified in the current execution environment.  
If the platform standard changes to Java 25 later, treat it as a controlled follow-up step (not a blind version bump): upgrade toolchain, re-run full tests, re-verify container image.

## If you continue this modernization

Recommended next steps:

1. Wire full Azure Key Vault property source for non-dev environments
2. Replace H2 with managed Azure database per environment strategy
3. Add Spring Security baseline for API protection
4. Add CI pipeline gates for `mvn clean verify` and container image build
5. Add operational telemetry and production-grade logging/alerts

---

This was not just a version bump. It was a stability, security, and operability reset.
