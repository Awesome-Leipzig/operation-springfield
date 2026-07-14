# Configuration & Externalized Settings Inventory

The application relies on a single `application.properties` file with no environment-specific profiles or external secret stores; all sensitive values — including database credentials, an API key, and a backdoor token — are hardcoded in both the properties file and a Java utility class, representing critical security risks.

## Configuration Sources

| Source | Type | Path/Location | Notes |
|---|---|---|---|
| `application.properties` | Spring Boot properties | `src/main/resources/application.properties` | Sole config file; no profile variants; contains hardcoded secrets |
| `SecretConstants.java` | Hardcoded Java constants | `src/main/java/com/springfield/plant/util/SecretConstants.java` | Duplicates secrets from properties file in source code — **CRITICAL security risk** |
| H2 in-memory DB | Embedded database | Runtime (JVM process) | Configured entirely via `application.properties`; data is ephemeral |
| No Spring Cloud Config | — | — | No external config server or bootstrap context detected |
| No secret store | — | — | No Vault, Azure Key Vault, AWS Secrets Manager, or environment variable injection |

## Build Profiles

| Profile | Activation | Purpose | Key Dependencies/Plugins |
|---|---|---|---|
| *(default only)* | Always active | Single monolithic build configuration | `spring-boot-maven-plugin` (repackage/run); no additional profiles defined |

No Maven `<profiles>` blocks exist in `pom.xml`. There is no build-time differentiation between environments.

## Runtime Profiles

| Profile | Activation Method | Config Files | Key Overrides |
|---|---|---|---|
| *(default only)* | Implicit (no `spring.profiles.active` set) | `application.properties` | None — single profile used for all environments including "production" |

No profile-specific properties files (e.g., `application-dev.properties`, `application-prod.properties`) exist. No `@Profile` annotations are present in the codebase. There is no mechanism to switch configuration between environments.

## Properties Inventory

| Property Key | Default Value | Profiles | Source | Sensitivity |
|---|---|---|---|---|
| `spring.application.name` | `sector-7g-safety-ledger` | default | `application.properties` | None |
| `server.port` | `8080` | default | `application.properties` | None |
| `spring.datasource.url` | `jdbc:h2:mem:snpp;DB_CLOSE_DELAY=-1` | default | `application.properties` | Low (in-memory, ephemeral) |
| `spring.datasource.username` | [MASKED] | default | `application.properties` | **HIGH — hardcoded credential in source control** |
| `spring.datasource.password` | [MASKED] | default | `application.properties` | **CRITICAL — hardcoded password in source control** |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | default | `application.properties` | Medium — destructive on restart; inappropriate for any persistent store |
| `spring.jpa.show-sql` | `true` | default | `application.properties` | Low (verbosity risk in production logs) |
| `spring.h2.console.enabled` | `true` | default | `application.properties` | **HIGH — H2 web console exposed; must be disabled in production** |
| `plant.api.key` | [MASKED] | default | `application.properties` | **CRITICAL — hardcoded API key in source control** |
| `plant.audit.backdoor` | [MASKED] | default | `application.properties` | **CRITICAL — hardcoded backdoor token used by `LegacyAuditFilter`** |

## Startup Parameters & Resource Requirements

| Service | JVM/Runtime Options | Memory | Instance Count |
|---|---|---|---|
| `sector-7g-safety-ledger` | None specified (Spring Boot defaults apply) | No heap limits configured | 1 (single JVM process, no clustering) |

No `JAVA_OPTS`, `-Xmx`/`-Xms`, GC tuning, or any JVM flags are defined anywhere in the project. No Dockerfile, `docker-compose.yml`, Kubernetes manifests, or container resource limits exist.

## Startup Dependency Chain

1. **JVM starts** — `PlantApplication.main()` invokes `SpringApplication.run()`
2. **Spring context initializes** — auto-configuration runs; H2 in-memory database is created in-process (no external dependency; always available)
3. **Hibernate DDL executes** — `spring.jpa.hibernate.ddl-auto=create-drop` drops and recreates all schema tables on every start (data is non-persistent)
4. **`LegacyAuditFilter` registered** — Servlet filter chain configured; reads `SecretConstants` values at first request
5. **`DataLoader.run()` executes** — `CommandLineRunner` seeds all reactor, employee, and incident data via repository calls; prints boot confirmation to stdout
6. **Application ready** — HTTP server (embedded Tomcat on port 8080) begins accepting requests

No external services (message brokers, caches, remote config servers) are required. There are no health-check or wait mechanisms — the application starts unconditionally.

## Secrets & Sensitive Configuration

| Secret Reference | Type | Storage | Risk Level |
|---|---|---|---|
| `spring.datasource.username` | Database username | Hardcoded in `application.properties` (source control) | **CRITICAL** |
| `spring.datasource.password` | Database password | Hardcoded in `application.properties` (source control) | **CRITICAL** |
| `SecretConstants.DB_USER` | Database username duplicate | Hardcoded Java constant in `SecretConstants.java` (source control) | **CRITICAL** |
| `SecretConstants.DB_PASSWORD` | Database password duplicate | Hardcoded Java constant in `SecretConstants.java` (source control) | **CRITICAL** |
| `plant.api.key` / `SecretConstants.PLANT_API_KEY` | Application API key | Hardcoded in both `application.properties` and `SecretConstants.java` | **CRITICAL** |
| `plant.audit.backdoor` / `SecretConstants.SMITHERS_BACKDOOR_TOKEN` | Backdoor authentication token | Hardcoded in both `application.properties` and `SecretConstants.java`; checked in `LegacyAuditFilter` via `X-Smithers-Token` header | **CRITICAL** |

> **Note:** Actual secret values are not reproduced here. All are stored as plaintext in version-controlled files.

### Secrets Provisioning Workflow

There is no secrets provisioning workflow. All secrets are statically embedded in two locations:

1. **`application.properties`** — Spring Boot reads these at startup; no environment variable substitution (e.g., `${ENV_VAR}`) is used.
2. **`SecretConstants.java`** — A `public static final` utility class; `LegacyAuditFilter` references it directly at runtime. The class duplicates values already present in `application.properties`, creating two independent hardcoded copies of each secret.

No integration with any secret manager (HashiCorp Vault, Azure Key Vault, AWS Secrets Manager, Kubernetes Secrets, or `@ConfigurationProperties` with externalized env vars) exists. **Migration to managed identity or environment-variable-based injection is required before any cloud deployment.**

## Feature Flags

| Flag Name | Default | Controlled By |
|---|---|---|
| `spring.h2.console.enabled` | `true` | `application.properties` — enables the H2 browser console at `/h2-console`; should be `false` in production |
| `spring.jpa.show-sql` | `true` | `application.properties` — logs all SQL statements to stdout; should be `false` in production |
| Smithers backdoor gate | Always enabled (hardcoded) | `LegacyAuditFilter` — checks `X-Smithers-Token` request header against `SecretConstants.SMITHERS_BACKDOOR_TOKEN`; grants no actual privilege but represents dead code with security implications |

No `@ConditionalOnProperty`, `@ConditionalOnExpression`, or feature-flag framework (e.g., Unleash, LaunchDarkly, FF4J) is used anywhere in the codebase.

## Framework & Runtime Versions

| Component | Version | Source |
|---|---|---|
| Java | 1.8 (Java 8) | `pom.xml` `<java.version>` — EOL; target upgrade is Java 21 |
| Maven Compiler Source/Target | 1.8 | `pom.xml` `<maven.compiler.source>` / `<maven.compiler.target>` |
| Spring Boot | 2.3.12.RELEASE | `pom.xml` parent — EOL since August 2021 |
| Spring Web MVC | Managed by Spring Boot 2.3.x (5.2.x) | Transitive via `spring-boot-starter-web` |
| Spring Data JPA | Managed by Spring Boot 2.3.x | Transitive via `spring-boot-starter-data-jpa` |
| Hibernate ORM | Managed by Spring Boot 2.3.x (~5.4.x) | Transitive via `spring-boot-starter-data-jpa` |
| Thymeleaf | Managed by Spring Boot 2.3.x (3.0.x) | Transitive via `spring-boot-starter-thymeleaf` |
| Embedded Tomcat | Managed by Spring Boot 2.3.x (9.x) | Transitive via `spring-boot-starter-web` |
| H2 Database | Managed by Spring Boot 2.3.x (1.4.x) | `pom.xml` runtime scope |
| SpringFox Swagger2 | 2.9.2 | `pom.xml` — abandoned; incompatible with Spring Boot 3; replace with `springdoc-openapi` |
| SpringFox Swagger UI | 2.9.2 | `pom.xml` — abandoned; incompatible with Spring Boot 3 |
| Apache Commons Text | 1.8 | `pom.xml` — vulnerable to CVE-2022-42889 (Text4Shell RCE) |
| Apache Commons Collections | 3.2.1 | `pom.xml` — vulnerable to CVE-2015-6420 (deserialization RCE) |
| Google Guava | 20.0 | `pom.xml` — multiple known CVEs; severely outdated (current: 33.x) |
| JUnit | 4 (exact version managed by Boot parent) | `pom.xml` test scope — legacy; migrate to JUnit 5 |
| spring-boot-maven-plugin | Managed by Spring Boot 2.3.x | `pom.xml` build plugin |
