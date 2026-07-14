# Operation Fresh Brew — Implementation Plan (Phases 2–6 + Side Quests)

Source of truth for tasks: `tracker.html` (26 tracker line items across Phase 2–6 +
Side Quests; Phase 1 is excluded — it was already complete before this effort started).

## ✅ Final status

**24 of 26 tracker items are done.** **2 remain blocked** on human-only activities
(prep materials are ready for both):

| Task | Status |
|---|---|
| `p6-live-demo` — 10-min live demo | Prep done (`DEMO-SCRIPT.md`); humans present using the live endpoint. |
| `p6-retro` — team retro | Prep done (`RETRO-TEMPLATE.md`); humans fill in during the retro. |

**Prompt Golf resolved**: the team's Golden Donut went to the original session-kickoff
prompt ("Use the playwright mcp to navigate to tracker.html and write a plan... loop
for subagents which run in a loop to fulfill the tasks and validate the
functionality") — recorded in `RETRO-TEMPLATE.md`. Rationale: it drove the entire
modernize → deploy → test loop end-to-end with minimal human intervention over ~3
hours.

**Reactor Core score: 1,080 / 1,150 pts.** Phases 2, 3, 4, 5 are 100%; Phase 6 is 30%
(one-pager done, demo/retro pending humans); Side Quests are 100%.

**Key decision carried through the whole effort**: Java target stayed at **21 LTS**
(matches `pom.xml` and hackathon scoring) rather than the Java 25 mentioned in
`AGENTS.md`'s aspirational guidelines — treated as out of scope, not a blind bump.

## 🚀 Live deployment

- **Endpoint**: https://ca-f26yymfqkwvk4.icyfield-7de6aa92.germanywestcentral.azurecontainerapps.io/
- **Resource group**: `rg-swo-gh-hackathon-team2` (Germany West Central) — pre-existing;
  `infra/main.bicep` deploys *into* it via an `existing` resource lookup rather than
  creating a new one.
- **azd environment**: `swo-gh-hackathon-team2`
- **Zero passwords**: Postgres auth is via the user-assigned managed identity
  (registered as Postgres AAD administrator) + `com.azure:azure-identity-extensions`
  JDBC auth plugin — no `SPRING_DATASOURCE_PASSWORD` anywhere. The one remaining app
  secret (`plant.security.api-key`) is Key Vault-backed via the Container App's
  managed-identity secret reference.
- **Verified live**: smoke test 4/4, load test 100/100 (p50 81ms / p95 104ms / p99
  120ms — see `LOAD-TEST-RESULTS.md`), telemetry confirmed flowing in Application
  Insights, `mvn clean verify` 32/32 tests green, GitHub CI green.
- **Cost**: ~$67–69/month (see `COST-ESTIMATE.md` for the full breakdown and why a
  scale-to-zero optimization was tried and reverted — below).

## 🛠️ Real issues found and fixed along the way

1. **RBAC permission gap**: the deploying account had subscription-level Contributor
   only, not `Microsoft.Authorization/roleAssignments/write` on the resource group,
   so the IaC's `AcrPull`/`Key Vault Secrets User` role assignments failed. Added an
   `assignRoles` bool parameter to `resources.bicep` as a documented fallback
   (ACR admin credentials + plain env var) for future runs without sufficient
   permission; unblocked once the user was made Owner on the RG. Note: `az`/`azd`
   needed a fresh `az login` after the permission grant — the cached token didn't
   reflect the new role until re-authenticated.
2. **Missing Postgres JDBC driver**: the app crash-looped in the cloud
   (`Failed to load driver class org.postgresql.Driver`) because `pom.xml` only had
   H2. Added `org.postgresql:postgresql` (BOM-managed) +
   `com.azure:azure-identity-extensions:1.2.9`, and appended
   `?sslmode=require&authenticationPluginClassName=com.azure.identity.extensions.jdbc.postgresql.AzurePostgresqlAuthenticationPlugin`
   to the Bicep-generated `SPRING_DATASOURCE_URL`.
3. **Azure CLI log/query commands failed with `CERTIFICATE_VERIFY_FAILED`** behind a
   corporate proxy — worked around with native PowerShell (`Invoke-RestMethod` +
   `az account get-access-token`) instead of `az containerapp logs show` /
   `az monitor log-analytics query`.
4. **Stale live deployment**: after adding the `/inspect` feature, `azd provision`
   was run but not `azd deploy` — provision only updates infrastructure, never the
   container image. The live endpoint kept 404ing on the new route until `azd deploy`
   was run.
5. **Production incident — scale-to-zero cold-start replica race**: a cost
   optimization set `minReplicas: 0`. When traffic arrived after an idle period,
   Container Apps cold-started **two replicas concurrently**; both raced to run
   Hibernate's schema auto-DDL against the same empty Postgres database, corrupting
   one replica's session and causing ~50% of requests (round-robined to the broken
   replica) to intermittently fail with `relation "reactor" does not exist` — this
   is what showed up live as flickering whitelabel/500 errors. Fixed in three layers:
   - Made `spring.jpa.hibernate.ddl-auto` configurable (was hardcoded `create-drop`,
     fine for ephemeral H2 but dangerous for shared Postgres); defaults to `update`
     in production via `SPRING_JPA_HIBERNATE_DDL_AUTO`.
   - Added a guard in `DataLoader` so replica restarts against an already-seeded
     database don't insert duplicate rows.
   - **Reverted `minReplicas` back to `1`** — the real fix, since even `update` mode
     isn't safe against concurrent *first-time* schema creation. Scale-out (1→3)
     under load remains enabled and safe. Full incident writeup and the path to
     safely re-enable scale-to-zero later (Flyway/Liquibase) are in
     `COST-ESTIMATE.md`.
6. **Shared-repo test drift**: a teammate's agent (`copilot-swe-agent[bot]`) added
   real API-key enforcement to `LegacyAuditFilter`, then a follow-up commit reverted
   it back to audit-only without updating the test — breaking GitHub CI. Fixed by
   aligning the test with the actual current (audit-only) behavior rather than
   re-adding the enforcement unilaterally, since the revert was someone else's
   deliberate call. `TRIAGE.md` finding S7 (no Spring Security on the API) is still
   open — flagged for the team.

## Phase-by-phase completion summary

| Phase | Points | Status | Evidence |
|---|---|---|---|
| Phase 2 — Core Upgrade | 250/250 | ✅ Done | `mvn clean verify` green (Java 21, Boot 4.0.7); `UPGRADE-SUMMARY.md` |
| Phase 3 — Meltdown Prevention | 150/150 | ✅ Done | 1 real CVE found & fixed (Tomcat, CVE-2026-55956); `CVE-SCAN.md`; `.github/workflows/ci.yml` |
| Phase 4 — Cloud Evacuation | 250/250 | ✅ Done | Live at the endpoint above; `infra/` Bicep; zero passwords confirmed |
| Phase 5 — Safety Inspection | 130/130 | ✅ Done | 32 tests incl. a real Testcontainers Postgres IT; smoke/load scripts validated live; telemetry confirmed |
| Phase 6 — Audit Day | 30/100 | 🔶 Partial | One-pager done (`ONE-PAGER.md`); demo + retro need humans (prep ready) |
| Side Quests | 150/150 | ✅ Done | Testcontainers, new feature (`/api/reactors/{id}/inspect`), cost estimate, `MODERNIZATION.md`, and Prompt Golf (team vote, recorded in `RETRO-TEMPLATE.md`) all done |

Full detail for each of these lives in its own doc rather than duplicated here:
`UPGRADE-SUMMARY.md`, `CVE-SCAN.md`, `COST-ESTIMATE.md`, `LOAD-TEST-RESULTS.md`,
`MODERNIZATION.md`, `ONE-PAGER.md`, `DEMO-SCRIPT.md`, `RETRO-TEMPLATE.md`.
