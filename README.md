# ☢️ Sector 7G Safety Ledger

The Springfield Nuclear Power Plant's safety ledger — modernized to
Java 21 / Spring Boot 4. It is the "victim app" for
**[Operation Fresh Brew](HACKATHON.md)**, an 8-hour GitHub Copilot Java
modernization hackathon.

> Every `☢️ LEGACY ALERT` comment in the code marks something the hackathon
> challenges will make you fix — with Copilot doing the heavy lifting.

🚀 **Live deployment**: https://ca-f26yymfqkwvk4.icyfield-7de6aa92.germanywestcentral.azurecontainerapps.io/
(Azure Container Apps, `rg-swo-gh-hackathon-team2`, Germany West Central — Postgres
Flexible Server with managed-identity/zero-password auth, telemetry in Application
Insights).

## What's in the box

| File | What it is |
|---|---|
| [HACKATHON.md](HACKATHON.md) | The full hackathon plan: goals, squads, phases, points, rules |
| [tracker.html](tracker.html) | Live progress tracker — open it on the big screen, no server needed |
| `src/` | The legacy app you'll modernize |

## Run the app

**Prereqs:** JDK 21, Maven 3.9+ — the full hackathon toolchain (VS Code ≥ 1.113,
GitHub Copilot + the **GitHub Copilot app modernization** extension
(`vscjava.migrate-java-to-azure`), Docker, `az`, `azd`, and the optional Copilot CLI
`modernize-java` plugin) is listed in
[HACKATHON.md → Toolchain & Prerequisites](HACKATHON.md#-toolchain--prerequisites-install-before-shift-start).

```bash
mvn spring-boot:run
```

Then:

| URL | What you get |
|---|---|
| http://localhost:8080/ | Plant dashboard (Thymeleaf) |
| http://localhost:8080/api/reactors | Reactor REST API |
| http://localhost:8080/api/incidents | Safety incidents |
| http://localhost:8080/api/incidents/donuts | A critical plant KPI 🍩 |
| http://localhost:8080/swagger-ui/index.html | OpenAPI Swagger UI |
| http://localhost:8080/h2-console | H2 console |

Run the tests:

```bash
mvn test
```

## Containerize and deploy to Azure Container Apps

Build the container image:

```bash
docker build -t sector-7g-safety-ledger:latest .
```

### Option A — one-command deploy with `azd` (recommended)

Full IaC (Container App, Container Registry, Postgres Flexible Server with
Entra-only/managed-identity auth, Key Vault, Application Insights + Log Analytics)
lives in [`azure.yaml`](azure.yaml) and [`infra/`](infra). It's been validated with
`az bicep build` (compiles cleanly) but **not yet deployed** — an actual deploy
requires an authenticated Azure subscription:

```bash
az login
azd auth login
azd up               # provisions infra + builds/pushes the image + deploys, in one command
```

Redeploy after a code change with just:

```bash
azd deploy
```

### Option B — manual `az containerapp` with the static manifest

For a quicker/simpler deploy against an already-existing Container Apps environment,
`azure-container-app.yaml` is a hand-authored manifest (Key Vault-backed secrets,
system-assigned managed identity) you can feed directly to the CLI:

```bash
az containerapp create --yaml azure-container-app.yaml
```

## The planted modernization targets

This app is a trap, on purpose. Your Copilot-powered mission:

| Smell | Where | Fix (Phase) |
|---|---|---|
| Java 8 + Spring Boot 2.3 (EOL) | `pom.xml` | Upgraded to Java 21 + Boot 4.x |
| `javax.persistence` / `javax.servlet` | entities, `LegacyAuditFilter` | Migrated to `jakarta.*` |
| SpringFox 2.9 | `SwaggerConfig` | Replaced with springdoc-openapi |
| JUnit 4 + `SpringRunner` | `src/test` | Migrated to JUnit 5 |
| `new Integer(...)`, `Hashtable`, `StringBuffer` | services | Replaced with modern Java patterns |
| Static shared `SimpleDateFormat` (not thread-safe) | `DateUtils` | Replaced with `java.time` |
| CVE-laden deps: commons-text 1.8, commons-collections 3.2.1, guava 20 | `pom.xml` | Removed |
| Hardcoded credentials (fake) | `SecretConstants`, `application.properties` | Replaced with env/Key Vault configuration |
| `System.out` logging | `LegacyAuditFilter`, `DataLoader` | Replaced with SLF4J |
| H2 in-memory "production" DB | `application.properties` | Azure database (Phase 4) |

## How to run the hackathon

1. Read [HACKATHON.md](HACKATHON.md) — goals, 3 squads, 6 phases, points, and the **Toolchain & Prerequisites** section (install everything the day before; the preflight script takes 10 minutes).
2. Open [tracker.html](tracker.html) on a projector. Hit **Start shift**.
3. Fork this repo, make sure everyone can build it *as-is*.
4. Open the **GitHub Copilot modernization pane** in VS Code → *Start Assessment* (installs AppCAT on first run), then drive the `modernize` agents through Phases 1–6.
5. Check off challenges on the tracker as you go. Confetti is earned, not given.

*All credentials in this repository are fictional and planted for training. Any resemblance to real secrets, living or rotated, is purely coincidental.*
