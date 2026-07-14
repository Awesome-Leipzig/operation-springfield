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

## Update: the rest of the journey (Phases 3–6 + Side Quests)

Everything above was Phase 2. Here's what happened after, since this file was first
written mid-modernization:

### Phase 3 — Meltdown Prevention
- Ran a real CVE scan of the resolved dependency tree and found (and fixed) a
  genuine Moderate CVE: **CVE-2026-55956** in the transitive Tomcat dependency,
  patched by bumping `spring-boot-starter-parent` to 4.0.7 and overriding
  `<tomcat.version>` to 11.0.24. Full writeup: [CVE-SCAN.md](CVE-SCAN.md).
- Added `.github/workflows/ci.yml` — build+test on every push/PR, plus a GitHub
  `dependency-review-action` job.

### Phase 4 — Cloud Evacuation (fully deployed, not just IaC)
- Hand-authored azd-compatible Bicep (`azure.yaml`, `infra/`) covering Container
  Apps, Azure Container Registry, Key Vault, Postgres Flexible Server (Entra-only
  auth), Log Analytics, and Application Insights.
- **Actually deployed** to `rg-swo-gh-hackathon-team2` (Germany West Central) via
  `azd provision` + `azd deploy` — live at
  https://ca-f26yymfqkwvk4.icyfield-7de6aa92.germanywestcentral.azurecontainerapps.io/
- Hit and fixed two real deployment blockers: an RBAC permission gap
  (`Microsoft.Authorization/roleAssignments/write` needs Owner/User Access
  Administrator, not just Contributor) and a missing Postgres JDBC driver
  (`org.postgresql:postgresql` + `com.azure:azure-identity-extensions` had to be
  added — H2 alone isn't enough once `SPRING_DATASOURCE_URL` points at a real
  Postgres instance).
- **Zero passwords, confirmed**: the Container App's env vars have no
  `SPRING_DATASOURCE_PASSWORD` anywhere — auth is via the user-assigned managed
  identity (registered as the Postgres AAD administrator) + the
  `AzurePostgresqlAuthenticationPlugin` JDBC auth plugin.

### Phase 5 — Safety Inspection
- Expanded the test suite from 4 to 32 tests: service unit tests, `@WebMvcTest`
  controller slices, a filter test, and a **Testcontainers** integration test
  (`ReactorRepositoryPostgresIT`) that runs the JPA layer against a real
  `postgres:16-alpine` container via `maven-failsafe-plugin`.
- Writing those tests found two more real bugs: a Jackson constructor-parameter
  name mismatch (`donuts` vs `donutsConsumedDuringIncident`) that silently broke
  JSON round-trip deserialization under Boot 4's constructor-based binding, and a
  `@WebMvcTest` slice that needed `@EnableConfigurationProperties` since
  `@ConfigurationPropertiesScan` isn't loaded in web-layer test slices.
- `scripts/smoke-test.sh` and `scripts/load-test.sh` — both validated against the
  **live** endpoint (4/4 smoke checks, 100/100 load requests, p95 ~104ms — see
  [LOAD-TEST-RESULTS.md](LOAD-TEST-RESULTS.md)).
- Application Insights Java agent wired in as a zero-code javaagent in the
  `Dockerfile` — confirmed live telemetry flowing via an Application Insights API
  query.

### Side Quests
- **Testcontainers** (above) ✅
- **New feature in <30 min**: `POST /api/reactors/{id}/inspect` — stamps
  `lastInspection = now()`, 404 if unknown, 5 new tests.
- **Azure cost estimate + optimization**: pulled real pricing from the Azure Retail
  Prices API for the actual deployed SKUs (~$67–69/month), then applied
  scale-to-zero on the Container App (`minReplicas: 0` + an HTTP concurrency scale
  rule) — the single largest cost line item (~$39/month, over half the bill) —
  bringing the estimate down to ~$28–30/month. Full breakdown:
  [COST-ESTIMATE.md](COST-ESTIMATE.md).

## Important note for next year

The repo modernization target in org guidance mentions Java 25, but this run locked to **Java 21 LTS** because that is what was available and fully verified in the current execution environment.  
If the platform standard changes to Java 25 later, treat it as a controlled follow-up step (not a blind version bump): upgrade toolchain, re-run full tests, re-verify container image.

This is also a **shared/collaborative repo** — multiple people and Copilot agents
push directly to the same branches concurrently. Expect the occasional oscillation
(one contributor adds a feature, another reverts it, a test drifts out of sync with
the reverted code) — always re-run `mvn clean verify` after pulling before trusting
local state, and treat CI as the source of truth over any one person's local build.

## If you continue this modernization

Recommended next steps — updated now that most of the original list is done:

1. ~~Wire full Azure Key Vault property source for non-dev environments~~ — **done**:
   `plant.security.api-key` is Key Vault-backed via the Container App's
   managed-identity secret reference.
2. ~~Replace H2 with managed Azure database per environment strategy~~ — **done**:
   Postgres Flexible Server, Entra-only auth, live in production.
3. **Add a Spring Security baseline for API protection** — still genuinely open.
   `LegacyAuditFilter` remains audit-only (an API-key enforcement feature was added
   and then deliberately reverted mid-hackathon — see git history around
   `368ec89`/`6dc1dc0`); TRIAGE.md finding S7 ("no Spring Security on the classpath")
   is still unresolved. Worth a team discussion on the intended auth model before
   re-attempting.
4. ~~Add CI pipeline gates for `mvn clean verify` and container image build~~ —
   **done**: `.github/workflows/ci.yml` runs build+test+dependency-review on every
   push/PR; container image build is exercised via `azd deploy`.
5. ~~Add operational telemetry and production-grade logging/alerts~~ — **telemetry
   done** (Application Insights, confirmed flowing); alerting rules are still open
   if you want proactive paging rather than just dashboards.
6. **Stop the Postgres server between hackathon sessions** if cost matters further —
   unlike Container Apps, Postgres Flexible Server doesn't scale-to-zero
   automatically; `az postgres flexible-server stop` between sessions would trim
   the remaining ~$14.53/month compute line item (see COST-ESTIMATE.md).

---

This was not just a version bump. It was a stability, security, and operability reset — and then an actual live deployment to prove it.
