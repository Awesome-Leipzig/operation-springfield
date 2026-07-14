# Operation Fresh Brew — Implementation Plan (Phases 2–6 + Side Quests)

Source of truth for tasks: `tracker.html` (26 tracker line items across Phase 2–6 + Side
Quests; Phase 1 is explicitly excluded per instructions — it is already checked off).

## ✅ All subagent-doable work complete — final status

Following the pause, the user granted Owner access on `rg-swo-gh-hackathon-team2` and
asked for the app to be deployed there (done), then asked to continue through the
remaining leftovers (also done). **23 of 26 tracker items are done**, **3 are
correctly blocked** (live demo, retro, and Prompt Golf — human/team-only activities
with prep materials ready: `DEMO-SCRIPT.md`, `RETRO-TEMPLATE.md`). Reactor Core
score: **1,050 / 1,150 pts** (only the 3 human-only items remain unchecked).
Phases 2, 3, 4, 5 are 100%; Phase 6 is 30% (one-pager done); Side Quests are 80%
(everything but the team vote).

### 🎉 Side Quests + Phase 6 one-pager completed
- **Testcontainers**: `ReactorRepositoryPostgresIT` proves the JPA layer against a
  real `postgres:16-alpine` container; required adding `spring-boot-testcontainers`
  + `testcontainers-postgresql` (Testcontainers 2.x renamed all module artifacts
  with a `testcontainers-` prefix) and `maven-failsafe-plugin` so `*IT.java` classes
  actually run during `mvn verify` (Surefire ignores them by naming convention).
- **New feature in <30 min**: `POST /api/reactors/{id}/inspect` — stamps
  `lastInspection = now()`, 404 on unknown id, 5 new tests, manually verified live.
- **Azure cost estimate + optimization**: pulled real pricing from the Azure Retail
  Prices API for the actual deployed SKUs (~$67–69/month) — see
  `COST-ESTIMATE.md`. Applied scale-to-zero on the Container App (`minReplicas: 0`
  + HTTP concurrency rule), the single largest cost driver (~$39/month, over half
  the bill) — new estimate ~$28–30/month. Applied and verified against the live
  deployment.
- **`MODERNIZATION.md`**: a teammate had already started this (Phase 2/3 only,
  tracked with a stray `.MD` casing that would have been a broken-link risk on
  GitHub's case-sensitive storage — fixed) — updated in place with the full
  Phase 4–6 + Side Quests journey rather than duplicating it.
- **`ONE-PAGER.md`**: before/after with real numbers — versions, 1 real CVE fixed,
  82 files / 6,168 insertions since the legacy seed commit, 32 tests, live endpoint
  + load-test + cost numbers.
- **Prep materials for the two human Phase 6 items**: `DEMO-SCRIPT.md` (10-minute
  flow) and `RETRO-TEMPLATE.md` (fill-in-the-blanks for the team retro).

### 🐛 A shared-repo hiccup along the way
GitHub CI failed on `LegacyAuditFilterTest` after a teammate's agent
(`copilot-swe-agent[bot]`) added real API-key enforcement to `LegacyAuditFilter`
and then a follow-up commit reverted it back to audit-only — without updating the
test to match. Fixed by aligning the test with the actual current (audit-only)
behavior rather than re-adding the enforcement unilaterally, since that revert was
someone else's deliberate call; flagged it for the team to resolve (TRIAGE.md S7 is
still open). See `MODERNIZATION.md`'s "Important note for next year" section.

### 🔒 Remaining (human-only, not applicable to a subagent loop)

| Task | Status |
|---|---|
| `p6-live-demo` | Prep done (`DEMO-SCRIPT.md`); humans present using the live endpoint. |
| `p6-retro` | Prep done (`RETRO-TEMPLATE.md`); humans fill in during the retro. |
| `sq-prompt-golf` | Team vote — not applicable to the subagent loop. |

## Problem statement

### 🚀 Live deployment details
- **Resource group**: `rg-swo-gh-hackathon-team2` (Germany West Central), pre-existing —
  the IaC was adjusted to deploy *into* it (`infra/main.bicep` now references it via
  an `existing` resource lookup rather than creating a new one) instead of the
  originally-assumed `rg-${environmentName}` pattern.
- **azd environment**: `swo-gh-hackathon-team2`, `AZURE_RESOURCE_GROUP` set explicitly.
- **Live endpoint**: https://ca-f26yymfqkwvk4.icyfield-7de6aa92.germanywestcentral.azurecontainerapps.io/
  — smoke-tested (4/4 checks 200), load-tested (100/100 success, p50 83 ms / p95
  104 ms / p99 108 ms), telemetry confirmed flowing in Application Insights (14
  successful requests recorded via API query).
- **Zero passwords confirmed**: `az containerapp show` on the live app shows no
  `SPRING_DATASOURCE_PASSWORD` anywhere — Postgres auth is fully via the user-assigned
  managed identity (registered as Postgres AAD administrator) + `com.azure:azure-identity-extensions`
  JDBC auth plugin; the only other secret (`plant.security.api-key`) is Key
  Vault-backed via the Container App's managed-identity secret reference.

### 🛠️ Real issues hit and fixed during deployment
1. **RBAC permission gap**: the deploying account only had subscription-level
   Contributor (via a group), not `Microsoft.Authorization/roleAssignments/write` on
   the resource group — so the IaC's `AcrPull`/`Key Vault Secrets User` role
   assignments failed. Added an `assignRoles` bool parameter to `resources.bicep` as
   a documented fallback path (ACR admin credentials + plain env var) for future runs
   without sufficient permission; this run was unblocked once the user was made
   Owner on the RG (**note**: after the permission grant, `az`/`azd` needed a fresh
   `az login` — the old cached token didn't reflect the new role until re-authenticated).
2. **Missing Postgres JDBC driver**: the app crash-looped in the cloud
   (`Failed to load driver class org.postgresql.Driver`) because `pom.xml` had no
   Postgres driver — H2 was the only datasource dependency. Added
   `org.postgresql:postgresql` (BOM-managed version) + `com.azure:azure-identity-extensions:1.2.9`,
   and updated the Bicep-generated `SPRING_DATASOURCE_URL` to include
   `?sslmode=require&authenticationPluginClassName=com.azure.identity.extensions.jdbc.postgresql.AzurePostgresqlAuthenticationPlugin`
   so the managed identity's AAD token is used transparently as the JDBC password.
3. **Azure CLI log/query commands failed with `CERTIFICATE_VERIFY_FAILED`** behind a
   corporate proxy — worked around by using native PowerShell (`Invoke-RestMethod`
   with `az account get-access-token`) instead of `az containerapp logs show` /
   `az monitor log-analytics query`, which respects the Windows trusted cert store.

## Problem statement

The tracker shows 0% on every phase except Phase 1, but inspection of the actual repo
shows the codebase is **already far ahead of the tracker's checkboxes** — a prior session
("Migration version 1.0" commit) already executed most of the Phase 2/3 code changes.
The plan below is written against *actual repo state*, not tracker state, so subagents
don't redo completed work. Each phase lists what's **already done**, what's **remaining**,
and how the validation loop proves it.

### Confirmed current state (verified by direct inspection, not assumed)
- `pom.xml`: Spring Boot **4.0.0** parent, `java.version=21`, springdoc-openapi 2.8.9,
  H2, JUnit 5 (via `spring-boot-starter-test`). No SpringFox. No `commons-text`,
  `commons-collections`, or `guava` dependencies present.
- No `javax.*` imports anywhere in `src/` — `LegacyAuditFilter` uses `jakarta.servlet.*`,
  `Reactor`/entities use `jakarta.persistence.*`.
- `SecretConstants.java` does not exist (already deleted, per AGENTS.md rule).
- `application.properties` has **no hardcoded secrets** — datasource password and
  `plant.api.key` are `${ENV_VAR}` placeholders with a comment pointing at Key Vault.
- `DateUtils` uses `java.time` exclusively (no `SimpleDateFormat`). No `Hashtable`,
  `StringBuffer`, or `new Integer(...)` found in service classes.
- `Dockerfile` exists: multi-stage `maven:3.9.11-eclipse-temurin-21` build →
  `eclipse-temurin:21-jre` runtime, respects `SERVER_PORT`, exposes 8080.
- `azure-container-app.yaml` exists: Container Apps manifest with Key Vault-backed
  secrets and `identity: system` (managed identity) for datasource creds + API key —
  a **manual ARM-ish manifest**, not `azd`-generated Bicep/Terraform, and **not yet
  deployed**.
- No `.github/workflows/` — **no CI pipeline exists yet**.
- No telemetry/Application Insights config, no smoke-test script, no load-test script,
  no Testcontainers usage, no `MODERNIZATION.md`.
- Tests present: `PlantApplicationTests` (context load) and `ReactorServiceTest` (JUnit 5
  + AssertJ + Mockito, 3 tests) — thin coverage, nothing for `IncidentService`,
  `EmployeeService`, controllers, or the filter.

### Confirmed decisions (from user)
- **Java target: 21** (matches current `pom.xml` and hackathon scoring; do not bump to
  Java 25 — that's a documentation/aspirational mismatch in AGENTS.md, out of scope).
- **Azure Phase 4**: subagents will generate IaC and prep everything up to the deploy
  step, but real `az`/`azd` deployment requires human-provided credentials/subscription
  access — those specific tracker items are explicit **human-in-the-loop checkpoints**,
  not something the loop auto-executes.
- **Orchestration shape**: one sequential subagent loop **per phase**. Phase *N*'s loop
  must finish (all tasks `done` or explicitly `blocked` with reason) before Phase
  *N+1*'s loop starts, since phases are already temporally/dependency-ordered in the
  tracker (e.g., Phase 4 containerization depends on Phase 2's green build; Phase 5
  smoke test depends on Phase 4's live endpoint).

---

## Orchestration design (the "subagent loop")

1. **Controller** (this session or a follow-up session) owns the SQL `todos`/`todo_deps`
   tables, seeded 1:1 with tracker line items, grouped by phase.
2. For each phase in order (2 → 3 → 4 → 5 → 6 → Side Quests):
   a. Query `todos` where `status='pending'` and phase = current, respecting
      `todo_deps` (a task with an incomplete dependency is skipped until ready).
   b. Launch a **background subagent** scoped to exactly one task at a time (or a small
      batch of independent tasks in the same phase) with full context: task
      description, acceptance criteria (below), relevant file paths, and the
      project's build/test commands.
   c. Subagent implements the change, then **runs the validation command** for that
      task itself (e.g., `mvn clean verify`, `docker build`, `mvn spring-boot:run` +
      curl smoke checks) and reports pass/fail with evidence (command output).
   d. Controller marks the todo `done` only after validation evidence is confirmed;
      otherwise `blocked` with the reason logged in the todo description, and the loop
      retries or escalates to the human.
   e. Human-in-the-loop checkpoints (Azure credentials, live demo, team retro, prompt
      golf vote) are marked `blocked` until a human unblocks them — the loop does not
      stall on these; it proceeds with everything else in the same phase that doesn't
      depend on them, then pauses the phase-advance until the checkpoint clears.
   f. Once all of a phase's todos are `done` (or acceptably `blocked` on a
      human checkpoint with the rest of the phase complete), advance to the next
      phase's loop.
3. **Validation gate between phases**: `mvn clean verify` must be green before
   advancing out of Phase 2, and before starting Phase 4 (containerization needs a
   working build) and Phase 5 (test/telemetry work needs a running app).

---

## Phase 2 — ⚛️ Core Upgrade (250 pts) — mostly DONE, needs verification + docs

| Tracker item | Pts | State | Remaining work |
|---|---|---|---|
| Copilot upgrade plan (Java 8→21, Boot 2→3) reviewed & approved | 40 | Partial — `.github/modernize/springfield-upgrade-and-containerization/plan.md` exists | Resolve its two open questions (confirm Java 21, confirm target host), get a human sign-off note added to the plan doc |
| Agent-mode upgrade executed (javax→jakarta, APIs, build) | 80 | **Done** (verified: no `javax.*`, entities/filter use `jakarta.*`, legacy idioms gone) | Just confirm/record evidence; no code change expected |
| `mvn clean verify` GREEN on Java 21 | 50 | Unverified | Run `mvn clean verify` locally; fix any failures found |
| App boots locally, key endpoints respond | 40 | Unverified | `mvn spring-boot:run`, curl `/`, `/api/reactors`, `/api/incidents`, `/swagger-ui/index.html` |
| Upgrade summary: Copilot vs human fixes, weirdest diff | 40 | Missing | Write a short summary doc (can live in `MODERNIZATION.md` or a dedicated `UPGRADE-SUMMARY.md`) |

**Validation for this phase's loop**: `mvn clean verify` exits 0; app starts and the four
endpoints above return 2xx.

---

## Phase 3 — 🧯 Meltdown Prevention (150 pts) — secrets DONE, CVE + CI remain

| Tracker item | Pts | State | Remaining work |
|---|---|---|---|
| CVE scan + Copilot-drafted fix plan | 50 | Missing | Run a dependency CVE scan (`mvn org.owasp:dependency-check-maven:check` or GitHub Advisory/`mvn versions:display-dependency-updates`); draft fix plan for anything found |
| All critical/high CVEs fixed, build still green | 50 | Likely mostly done (known offenders already removed) | Act on scan results from previous task; re-run `mvn clean verify` |
| Secrets purged from code/config | 30 | **Done** (verified: no `SecretConstants.java`, `application.properties` uses env placeholders) | Just confirm/record evidence |
| CI quality gate (build + test + dependency review) | 20 | Missing (no `.github/workflows/`) | Add a GitHub Actions workflow: `mvn -B clean verify` + a dependency-review step (e.g., `actions/dependency-review-action` on PRs) |

**Validation for this phase's loop**: dependency scan report generated with zero
unresolved critical/high findings; CI workflow file present and green on a test push/PR.

---

## Phase 4 — 🚀 Cloud Evacuation (250 pts) — Dockerfile DONE, deploy needs human

| Tracker item | Pts | State | Remaining work |
|---|---|---|---|
| Containerization plan + Dockerfile; image runs locally | 40 | **Done** (Dockerfile exists) | Verify: `docker build` succeeds, `docker run` serves traffic on 8080 |
| IaC generated (Bicep/Terraform via azd) | 50 | Partial (`azure-container-app.yaml` is a manual manifest, not azd-scaffolded) | Run `azd init`/`azd infra synth` (or hand-author) to produce `azure.yaml` + `infra/*.bicep` covering Container App, database, Key Vault, App Insights |
| **DEPLOYED to Azure, reachable over HTTPS** | 80 | Not started | 🔒 **Human checkpoint**: requires real Azure subscription/login (`az login`, `azd auth login`, `azd up`). Subagent prepares everything up to this command; a human runs/approves the actual deploy |
| Azure DB with managed identity — zero passwords | 50 | Partial (manifest wires Key Vault + managed identity, but no real DB provisioned) | Part of the same IaC (bicep) work; 🔒 provisioning itself needs human Azure access |
| Config externalized; one-command redeploy | 30 | Mostly done (env-var driven already) | Add a one-command redeploy script/`azd up` alias; document it in README |

**Validation for this phase's loop**: `docker build && docker run` serves the app
locally; IaC files pass `az bicep build`/`azd provision --preview` (dry-run) without
requiring the actual `azd up`. Real deployment items are logged `blocked: needs human
Azure credentials` until a human runs them, then a subagent verifies the live HTTPS
endpoint afterward.

---

## Phase 5 — ✅ Safety Inspection (130 pts) — mostly missing, depends on Phase 4

| Tracker item | Pts | State | Remaining work |
|---|---|---|---|
| Test suite green in cloud build (+ new tests) | 40 | Thin coverage today | Add tests for `IncidentService`, `EmployeeService`, controllers, `LegacyAuditFilter` |
| Scripted smoke test vs live endpoint passes | 30 | Missing | Write a script (curl/`http` based) hitting the deployed endpoint; 🔒 needs Phase 4's live URL |
| Telemetry flowing in Application Insights | 30 | Missing | Add `applicationinsights-spring-boot-starter` (or Azure Monitor OpenTelemetry) + connection string env var; 🔒 needs Phase 4's App Insights resource |
| Load sanity check (100 reqs), p95 recorded | 30 | Missing | Simple script (e.g., `hey`/`ab`/curl loop) against local or live endpoint, record p95 |

**Validation for this phase's loop**: expanded test suite green in `mvn clean verify`;
smoke-test script exits 0 against the live endpoint (once unblocked); load-test script
produces a recorded p95 number.

---

## Phase 6 — 🏆 Audit Day (100 pts) — human-facilitated deliverables

| Tracker item | Pts | State | Remaining work |
|---|---|---|---|
| 10-min live demo: old vs new + live Azure endpoint | 50 | 🔒 Human activity | Subagent can prep demo script/talking points; humans present |
| Before/after one-pager (versions, CVEs, LOC, % by Copilot) | 30 | Missing | Subagent-generatable: pull versions from git history/pom.xml diffs, CVE counts from Phase 3 scan, LOC diff via `git diff --stat` |
| Retro: Copilot wins, gotchas, best prompt of the day | 20 | 🔒 Human activity | Subagent can draft a retro template; humans fill it in |

**Validation for this phase's loop**: one-pager doc exists with real numbers pulled from
repo history/scan output; demo script and retro template exist as drafts for humans.

---

## Side Quests (bonus, 150 pts) — any time, lowest priority

| Tracker item | Pts | State | Remaining work |
|---|---|---|---|
| Prompt Golf (team vote) | 30 | 🔒 Human activity | N/A for subagent loop |
| Testcontainers integration tests via Copilot pass | 30 | Missing | Add `spring-boot-testcontainers` + a Postgres/H2 Testcontainers integration test |
| Ship a new feature in <30 min with Copilot | 30 | Missing | Small, scoped feature (e.g., a new `/api/reactors/{id}/inspect` endpoint) |
| Azure cost estimate + one optimization applied | 30 | Missing | Estimate via Azure Pricing Calculator/`azd` output; apply one optimization (e.g., scale-to-zero, right-sized SKU) |
| Time Capsule — `MODERNIZATION.md` | 30 | Missing | Generate a comprehensive `MODERNIZATION.md` summarizing the whole journey |

---

## Notes / risks

- Because Phase 2/3 code is already largely done, the biggest real remaining
  engineering effort is **Phase 4 (real Azure deploy)** and **Phase 5 (telemetry +
  smoke/load testing)** — both gated on human-provided Azure access.
- The CVE scan (Phase 3) hasn't actually been run yet — even though the known planted
  CVE dependencies were removed, an automated scan is still needed to close that
  tracker item with evidence and catch anything new (e.g., in Spring Boot 4.0.0's own
  transitive tree).
- Tracker UI state (localStorage in the browser) is decoupled from repo state; once
  work is validated, someone needs to manually check off the corresponding tracker
  boxes (or extend the loop to drive the tracker via Playwright at the end of each
  phase).
