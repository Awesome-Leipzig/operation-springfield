# ☕ Before/After One-Pager — Sector 7G Safety Ledger

## Versions

| | Before | After |
|---|---|---|
| Java | 8 (EOL) | 21 LTS |
| Spring Boot | 2.3.12.RELEASE (EOL) | 4.0.7 |
| Spring Framework | 5.x | 7.0.8 |
| Namespace | `javax.*` | `jakarta.*` |
| API docs | SpringFox 2.9.2 | springdoc-openapi 2.8.9 |
| Test framework | JUnit 4 + `SpringRunner` | JUnit 5 + AssertJ + Mockito + Testcontainers |
| Datastore | H2 in-memory (only option) | Azure Postgres Flexible Server (prod) + H2 (dev/test) |
| Hosting | None | Azure Container Apps (live) |

## Security

- **1 real CVE found and fixed**: CVE-2026-55956 (Apache Tomcat, Moderate severity)
  — pinned `<tomcat.version>` to the patched 11.0.24 release.
- **3 planted CVE-laden dependencies removed**: `commons-text` 1.8
  (CVE-2022-42889/Text4Shell), `commons-collections` 3.2.1, `guava` 20.0.
- **Hardcoded secrets eliminated**: `SecretConstants.java` deleted; datasource
  password and API key are now env-var/Key Vault-backed with zero plaintext
  credentials anywhere in code or config (verified via raw byte inspection of
  `application.properties` and `az containerapp show` on the live deployment).
- **Zero passwords in production**: Postgres auth is fully via managed identity +
  Entra token, not a connection-string password.

## Scale of the change

- **82 files changed, 6,168 insertions / 375 deletions** since the legacy seed
  commit (`git diff --shortstat d64cdf6 HEAD`).
- **38 commits**, **8 contributors** (mixed human + AI-agent team).
- **Tests: 0 → 32** (unit, `@WebMvcTest` slices, and a real Testcontainers
  PostgreSQL integration test).

## % done by Copilot

Git authorship shows 3 commits directly by `copilot-swe-agent[bot]` (the GitHub
Copilot coding agent) and 2 more explicitly co-authored by the Copilot CLI — but
that undercounts the real picture: per the hackathon's own premise
([HACKATHON.md](HACKATHON.md)), every human contributor was themselves driving
Copilot Chat, the app modernization VS Code extension, or the Copilot CLI
throughout. The mechanical migration work (namespace rewrites, dependency bumps,
test scaffolding, Bicep IaC authoring, Dockerfile, CI workflow) was Copilot-driven
essentially end to end; the judgment calls (which CVE fix to apply, whether to keep
or revert the API-key enforcement feature, which cost optimization to pick) were
human-reviewed at every step, consistent with "Rules of the Plant" Rule #2 ("humans
review everything").

## Live deployment

- **Endpoint**: https://ca-f26yymfqkwvk4.icyfield-7de6aa92.germanywestcentral.azurecontainerapps.io/
  (`rg-swo-gh-hackathon-team2`, Germany West Central)
- **Smoke test**: 4/4 endpoints return 200 (`/`, `/api/reactors`, `/api/incidents`,
  `/swagger-ui/index.html`)
- **Load test**: 100/100 requests succeeded — p50 81ms / p95 104ms / p99 120ms
  (see [LOAD-TEST-RESULTS.md](LOAD-TEST-RESULTS.md))
- **Telemetry**: confirmed flowing in Application Insights (verified via API query)
- **Cost**: ~$67–69/month. A scale-to-zero optimization was tried and **caused a
  real production incident** (concurrent cold-start replicas raced on Postgres
  schema creation, corrupting one replica's session) — reverted; see
  [COST-ESTIMATE.md](COST-ESTIMATE.md) for the full incident writeup.

## What's still open

- `LegacyAuditFilter` remains audit-only — a Spring Security baseline for the API
  is the next real security gap (TRIAGE.md finding S7).
- Scale-to-zero cost savings (~$39/month) remain unrealized until Hibernate
  auto-DDL is replaced with a proper migration tool (Flyway/Liquibase) that can
  safely handle concurrent replica cold-starts.
- Postgres Flexible Server doesn't scale-to-zero the way Container Apps does;
  stopping it between sessions is a manual further cost optimization.

*Full detail in [MODERNIZATION.md](MODERNIZATION.md), [PLAN.md](PLAN.md),
[CVE-SCAN.md](CVE-SCAN.md), and [UPGRADE-SUMMARY.md](UPGRADE-SUMMARY.md).*
