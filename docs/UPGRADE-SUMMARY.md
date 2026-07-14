# Phase 2 — Core Upgrade Summary

**Scope**: Java 8 → 21, Spring Boot 2.3.12 → 4.0.0, `javax.*` → `jakarta.*`, plus the
planted legacy-idiom cleanups from [TRIAGE.md](TRIAGE.md).

Single squashed migration commit: `77dc51a` ("Migration version 1.0") — 25 files
changed, 325 insertions(+), 296 deletions(-).

## What changed

| Area | Before | After |
|---|---|---|
| Java | 8 (EOL) | **21 LTS** |
| Spring Boot | 2.3.12.RELEASE (EOL) | **4.0.0** |
| Spring Framework | 5.x (EOL) | **7.x** (via Boot 4 BOM) |
| Persistence/Servlet API | `javax.persistence.*`, implicit servlet API | **`jakarta.persistence.*`, `jakarta.servlet.*`** |
| API docs | SpringFox 2.9.2 (`SwaggerConfig.java`, dead since Boot 3) | **springdoc-openapi-starter-webmvc-ui 2.8.9**, auto-configured, `SwaggerConfig.java` deleted |
| Test framework | JUnit 4 + `SpringRunner` | **JUnit 5** (`org.junit.jupiter.api`), AssertJ, Mockito |
| Secrets | `SecretConstants.java` with hardcoded DB password + API key | **File deleted**; `application.properties` reads `${SPRING_DATASOURCE_PASSWORD}` / `${PLANT_API_KEY}` from env/Key Vault |
| Legacy idioms | `Hashtable`, `StringBuffer`, `new Integer(...)`, static shared `SimpleDateFormat` | Modern collections (`HashMap`/`List.of()`), `String`/`StringBuilder` where needed, boxed literals, `DateUtils` rewritten on `java.time` (`Instant`, `DateTimeFormatter`) |
| Container readiness | No Dockerfile | **Multi-stage `Dockerfile`** (`maven:3.9.11-eclipse-temurin-21` → `eclipse-temurin:21-jre`), `SERVER_PORT`-driven |
| Deploy manifest | None | **`azure-container-app.yaml`** — Container Apps manifest wired for Key Vault secrets + system-assigned managed identity |

## Copilot vs. human

- **Copilot-driven**: the bulk migration commit (namespace rewrite, dependency bumps,
  JUnit 5 conversion, `DateUtils`/service idiom cleanup, Dockerfile/manifest
  scaffolding) was generated in one pass via Copilot agent mode against the
  AppCAT assessment + `.github/modernize/springfield-upgrade-and-containerization/plan.md`.
- **Human-reviewed/approved**: the upgrade plan's two open questions (target Java
  version, target container host) were confirmed and closed out by a human before
  sign-off (see plan.md).
- **Human-verified this pass**: `mvn clean verify` (BUILD SUCCESS, 4/4 tests) and a
  live `mvn spring-boot:run` boot with endpoint checks (`/`, `/api/reactors`,
  `/api/incidents`, `/swagger-ui/index.html` — all HTTP 200) were re-run and confirmed
  as part of the Phase 2 validation loop, since the migration commit predates this
  verification pass.

## Weirdest diff

`LegacyAuditFilter.java` (45 lines changed) went further than a namespace swap: the
old `X-Smithers-Token` dead-code auth-bypass header check (TRIAGE.md finding S6) was
removed entirely and replaced with a `PlantSecurityProperties`-backed audit log line
(`AUDIT {method} {uri} (api key configured: {bool})`) over SLF4J instead of
`System.out`. That's normally scoped as Phase 3 "secrets/backdoor cleanup" work, but
it rode along in the same commit as the `javax` → `jakarta` migration since the filter
file needed a servlet-API rewrite anyway — worth flagging in the retro (Phase 6) as an
example of a Copilot pass quietly fixing an adjacent security smell it wasn't
explicitly asked to touch.
