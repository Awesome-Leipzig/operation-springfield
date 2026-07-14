# Configuration & Externalized Settings Inventory

The application uses a single `application.properties` file as its configuration source, with all sensitive values and environment-specific overrides resolved from environment variables at runtime. Secrets (datasource credentials, API key) flow through Azure Key Vault via Container Apps secret references in production, with no Spring profiles required beyond the default.

---

## Configuration Sources

| Source File | Type | Purpose | Notes |
|---|---|---|---|
| `src/main/resources/application.properties` | Spring Boot properties | Primary application configuration: server port, datasource, JPA, H2 console, custom security property | All sensitive values use `${ENV_VAR:default}` syntax |
| `pom.xml` | Maven POM | Build definition, dependency management, plugin configuration | No Maven profiles defined |
| `Dockerfile` | Container build | Multi-stage build (Maven + JRE); sets `JAVA_TOOL_OPTIONS`, `SERVER_PORT`, injects App Insights Java agent | Sets env vars baked into the image layer |
| `azure-container-app.yaml` | Azure Container Apps manifest | Deployment configuration: ingress, Key Vault secret references, env var bindings, scale rules | Used for manual `az containerapp` deployments |
| `azure.yaml` | Azure Developer CLI (azd) manifest | Declares service `web` pointing at Dockerfile; drives `azd up` / `azd deploy` workflow | Minimal; delegates to Bicep for infra |
| `infra/main.bicep` | Bicep (subscription scope) | Entry point for `azd provision`; creates resource group module references, emits azd outputs | Delegates resource creation to `resources.bicep` |
| `infra/main.parameters.json` | Bicep parameters | Binds azd environment variables to Bicep parameters at provision time | All values are `${AZD_ENV_VAR}` references |
| `infra/resources.bicep` | Bicep (resource group scope) | Provisions ACR, Key Vault, Postgres Flexible Server, Container Apps environment and app, Log Analytics, App Insights | Sets all Container App env vars and Key Vault secret references |
| `infra/modules/identity.bicep` | Bicep module | Creates user-assigned managed identity used for passwordless Postgres auth and Key Vault access | Referenced by `resources.bicep` |

---

## Build Profiles

No Maven build profiles are defined in `pom.xml`. The Spring Boot Maven Plugin and Maven Failsafe Plugin are the only build customizations present. All environment differentiation is handled at runtime via environment variables, not at build time.

---

## Runtime Profiles

No Spring profile-specific property files (`application-{profile}.properties`) exist in the project. There is a single `application.properties` file covering all environments. Runtime environment differentiation is achieved entirely through environment variable overrides:

- **Local / development**: environment variables are unset; `application.properties` defaults apply (H2 in-memory database, `create-drop` DDL, H2 console enabled, empty API key).
- **Production (Azure Container Apps)**: environment variables (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_JPA_HIBERNATE_DDL_AUTO`, `PLANT_API_KEY`, `SERVER_PORT`, `APPLICATIONINSIGHTS_CONNECTION_STRING`, `AZURE_CLIENT_ID`) are injected by the Container Apps runtime, sourced from Bicep output values and Key Vault secret references.

Spring's `spring.profiles.active` is not set anywhere in the codebase; no `@Profile` annotations are present.

---

## Properties Inventory

### Server / Application

| Property Key | Default Value | Production Override | Description |
|---|---|---|---|
| `spring.application.name` | `sector-7g-safety-ledger` | — | Spring application name used in logs and actuator info |
| `server.port` | `8080` | `SERVER_PORT=8080` (explicit) | HTTP listener port |

### Database / DataSource

| Property Key | Default Value | Production Override | Description |
|---|---|---|---|
| `spring.datasource.url` | `jdbc:h2:mem:snpp;DB_CLOSE_DELAY=-1` | `SPRING_DATASOURCE_URL` (Postgres JDBC URL with Azure auth plugin) | JDBC connection URL |
| `spring.datasource.username` | `sa` | `SPRING_DATASOURCE_USERNAME` (managed identity name, from Key Vault in `azure-container-app.yaml`) | Database user |
| `spring.datasource.password` | *(empty string)* | `SPRING_DATASOURCE_PASSWORD` (from Key Vault; not used for managed-identity path) | Database password — empty for managed-identity auth |

### JPA / Hibernate

| Property Key | Default Value | Production Override | Description |
|---|---|---|---|
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | `SPRING_JPA_HIBERNATE_DDL_AUTO=update` | Schema generation strategy; `create-drop` for ephemeral H2 dev, `update` for persistent Postgres in production |
| `spring.jpa.show-sql` | `true` | — | Logs all generated SQL statements; not overridden for production (verbose in logs) |
| `spring.h2.console.enabled` | `true` | — | Enables H2 web console at `/h2-console`; no production override — console is inert when datasource points to Postgres |

### Custom Application Properties

| Property Key | Default Value | Production Override | Description |
|---|---|---|---|
| `plant.security.api-key` | *(empty string)* | `PLANT_API_KEY` (from Key Vault) | Custom API key bound via `@ConfigurationProperties(prefix = "plant.security")` into `PlantSecurityProperties` record |

---

## Startup Parameters & Resource Requirements

### JVM / Container (Dockerfile)

| Parameter | Value | Source |
|---|---|---|
| Base build image | `maven:3.9.11-eclipse-temurin-21` | `Dockerfile` `FROM` (build stage) |
| Base runtime image | `eclipse-temurin:21-jre` | `Dockerfile` `FROM` (runtime stage) |
| `JAVA_TOOL_OPTIONS` | `-javaagent:/app/applicationinsights-agent.jar` | `Dockerfile` `ENV` |
| `SERVER_PORT` | `8080` | `Dockerfile` `ENV` |
| Exposed port | `8080` | `Dockerfile` `EXPOSE` |
| App Insights agent version | `3.7.9` | `Dockerfile` `ADD` from GitHub releases |

No explicit JVM heap (`-Xmx`/`-Xms`) flags are set. Memory sizing is delegated to the Container Apps runtime.

### Azure Container Apps Scale / Resource Configuration (`resources.bicep`)

| Parameter | Value | Notes |
|---|---|---|
| Compute SKU (Postgres) | `Standard_B1ms` (Burstable) | Postgres Flexible Server |
| Postgres storage | `32 GB` | Flexible Server storage |
| Postgres version | `16` | |
| Log Analytics retention | `30 days` | `PerGB2018` SKU |
| Container App min replicas | `1` | Reverted from 0 — scale-to-zero caused concurrent `CREATE TABLE` race on cold start |
| Container App max replicas | `3` | |
| HTTP scale trigger | `concurrentRequests: 10` | KEDA HTTP scaler |

---

## Startup Dependency Chain

```
Azure Container Apps runtime
  |
  +-- Key Vault (resolves secret: plant-api-key)
  |     identity: user-assigned managed identity
  |
  +-- Container App: sector-7g-safety-ledger
        |
        +-- Env vars injected by runtime (SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME,
        |   AZURE_CLIENT_ID, SPRING_JPA_HIBERNATE_DDL_AUTO, PLANT_API_KEY, ...)
        |
        +-- JVM starts with App Insights Java agent (JAVA_TOOL_OPTIONS)
        |     reads APPLICATIONINSIGHTS_CONNECTION_STRING at agent init
        |
        +-- Spring Boot application context starts
              |
              +-- DataSource connects to Azure Postgres Flexible Server
              |     auth: azure-identity-extensions JDBC plugin authenticates via
              |     AZURE_CLIENT_ID (managed identity) -- no password
              |
              +-- Hibernate DDL: schema `update` runs once (minReplicas=1 ensures
                  only one replica starts cold against an empty schema)
```

There are no explicit `depends_on` / `initContainers` / readiness probe declarations in `azure-container-app.yaml` or `resources.bicep` beyond the implicit Container Apps secret resolution order (Key Vault secrets must be accessible before the container starts).

---

## Secrets & Sensitive Configuration

| Secret | Reference / Placeholder | Where Resolved | Notes |
|---|---|---|---|
| Datasource URL | `${SPRING_DATASOURCE_URL:jdbc:h2:mem:snpp;DB_CLOSE_DELAY=-1}` | Env var (set by Bicep output `postgresJdbcUrl`) | Not a secret per se, but environment-specific |
| Datasource username | `${SPRING_DATASOURCE_USERNAME:sa}` | Env var (set to managed identity name by Bicep) | Production value is the managed identity name, not a password-bearing credential |
| Datasource password | `${SPRING_DATASOURCE_PASSWORD:}` | Env var, sourced from Key Vault secret `datasource-password` via Container Apps secret ref | Empty for managed-identity auth path; Key Vault ref retained for compatibility |
| Plant API key | `${PLANT_API_KEY:}` | Env var, sourced from Key Vault secret `plant-api-key` via Container Apps secret ref | Bound into `PlantSecurityProperties.apiKey()`; placeholder `REPLACE_ME_POST_DEPLOY` in Bicep — must be rotated post-deploy |
| App Insights connection string | `APPLICATIONINSIGHTS_CONNECTION_STRING` | Env var set directly from Bicep `appInsights.properties.ConnectionString` output | Read by the App Insights Java agent at JVM startup, not by Spring |
| Azure Client ID | `AZURE_CLIENT_ID` | Env var set to managed identity `clientId` by Bicep | Read by `azure-identity-extensions` JDBC plugin for passwordless Postgres auth |

No secrets are hardcoded in source code or property files. `SecretConstants.java` does not exist in the codebase.

### Secrets Provisioning Workflow

```
azd provision
  |
  +-- Bicep creates Key Vault (RBAC-enabled, enableRbacAuthorization: true)
  |
  +-- Bicep writes placeholder secret `plant-api-key` = "REPLACE_ME_POST_DEPLOY"
  |     (only when assignRoles=true)
  |
  +-- Bicep grants Key Vault Secrets User role to:
  |     - user-assigned managed identity (for runtime Container App access)
  |     - deploying principal (for local dev access, if principalId provided)
  |
  +-- Bicep creates Container App with secret references:
  |     secrets[].keyVaultUrl = plantApiKeySecret.properties.secretUri
  |     secrets[].identity   = identityResourceId
  |
  +-- Container Apps runtime resolves Key Vault secrets at container startup
  |     using the managed identity -- injects as env vars (PLANT_API_KEY)
  |
  +-- Post-deploy manual step: rotate via
        `az keyvault secret set --vault-name <kv> --name plant-api-key --value <real-value>`

For Postgres, there is NO password in the secrets chain:
  Managed identity -> AZURE_CLIENT_ID -> azure-identity-extensions JDBC plugin
  -> Entra token exchange -> Postgres (passwordAuth: Disabled on the server)
```

When `assignRoles=false` (insufficient Azure RBAC permissions), the fallback path uses ACR admin credentials and a plain environment variable for `PLANT_API_KEY` (no Key Vault reference).

---

## Feature Flags

No `@ConditionalOnProperty`, `@ConditionalOnExpression`, or third-party feature toggle patterns are present in the codebase. The `PlantSecurityProperties.apiKeyConfigured()` helper method provides a runtime boolean check on whether `plant.security.api-key` is non-blank, but this is not wired to any `@ConditionalOn*` bean registration.

---

## Framework Version Inventory

| Framework / Library | Version | Notes |
|---|---|---|
| Java | 21 | `java.version` property in POM; Eclipse Temurin 21 JRE in Dockerfile |
| Spring Boot | 4.0.7 | Parent POM BOM |
| Spring Framework | (managed by Boot BOM) | Boot 4.x targets Spring Framework 7.x |
| Apache Tomcat (embedded) | 11.0.24 | Overrides BOM default 11.0.22 to patch CVE-2026-55956 |
| springdoc-openapi-starter-webmvc-ui | 2.8.9 | OpenAPI/Swagger UI; replaces removed SpringFox |
| H2 Database | (managed by Boot BOM) | Runtime scope; in-memory dev/test database |
| PostgreSQL JDBC driver | (managed by Boot BOM) | Runtime scope; production datastore driver |
| azure-identity-extensions | 1.2.9 | Passwordless Postgres auth via managed identity |
| Application Insights Java agent | 3.7.9 | Downloaded in Dockerfile; auto-instruments JVM |
| Maven | 3.9.11 | Build image (`maven:3.9.11-eclipse-temurin-21`) |
| Maven Failsafe Plugin | (managed by Boot BOM) | Runs `*IT.java` integration tests in `verify` phase |
| Testcontainers (JUnit Jupiter) | (managed by Boot BOM) | Integration test support |
| Testcontainers PostgreSQL | (managed by Boot BOM) | Postgres container for integration tests |
| spring-boot-starter-validation | (managed by Boot BOM) | Bean Validation (Jakarta) at controller boundary |
| spring-boot-configuration-processor | (managed by Boot BOM) | Annotation processor for `@ConfigurationProperties` metadata |
