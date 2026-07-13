# ☢️ OPERATION SPRINGFIELD
### A GitHub Copilot Java Modernization Hackathon — *Contain the Legacy Meltdown*

> **The story:** The Springfield Nuclear Power Plant runs its entire safety-ledger on a
> Java 8 / Spring Boot 2.x monolith written decades ago in Sector 7G. The auditors arrive
> **tomorrow morning**. You have **8 hours** and **one secret weapon — GitHub Copilot
> app modernization** — to scan it, upgrade it, secure it, and evacuate it to Azure
> before the reactor (and the audit) melts down.

---

## 📋 At a Glance

| | |
|---|---|
| **Duration** | 8 hours (one day, e.g. 09:00–17:00) |
| **Team** | 6 engineers → 3 squads of 2 |
| **Difficulty** | High — but every phase is achievable with Copilot doing the heavy lifting |
| **Core toolchain** | VS Code + GitHub Copilot (agent mode) + **GitHub Copilot app modernization for Java** extension, JDK 8→21, Maven, Docker, Azure CLI, `azd` |
| **Scoring** | 1,000 core points + 150 side-quest bonus points |
| **Live tracker** | Open [tracker.html](tracker.html) on a shared screen / projector all day |

---

## 🎯 Goals (what "amazing" looks like at 17:00)

1. **Assess** — a full AppCAT assessment report + architecture diagram of the legacy app, generated with Copilot.
2. **Upgrade** — the app builds and runs on **Java 21 + Spring Boot 3.x**, migrated by Copilot agent mode with a human-reviewed upgrade plan.
3. **Secure** — zero **critical/high CVEs** in dependencies; secrets removed from code.
4. **Migrate** — the app running on **Azure** (Container Apps or App Service) with managed identity to the database — no passwords, anywhere.
5. **Prove it** — green test suite, a smoke-tested live endpoint, telemetry flowing to Application Insights.
6. **Demo it** — a 10-minute "safety inspection" demo + a written before/after summary.

---

## 🧑‍🚒 The Squads (6 people, 3 pairs)

| Squad | Callsign | Primary ownership |
|---|---|---|
| 🟢 **Core Crew** | *"It's my first day"* | Assessment, Java/Spring upgrade, build health |
| 🟡 **Safety Crew** | *"Everything's fine"* | CVEs, secrets, tests, quality gates |
| 🔴 **Launch Crew** | *"Evacuate the plant"* | Containerization, IaC, Azure deploy, telemetry |

Two rotating hats (swap every ~2 hours):
- **Shift Supervisor** 🎩 — keeps time, unblocks people, updates the tracker.
- **Scribe** 📓 — captures the best/worst Copilot prompts of the day for the retro.

> Squads own phases but **swarm** when a phase is on the critical path (esp. Phase 2 and 4).

---

## 🕘 Schedule

| Time | Phase | Focus |
|---|---|---|
| 09:00–09:30 | ⚙️ **H0 — Shift Start** | Kickoff, env check, pick the victim app |
| 09:30–10:15 | 🔍 **Phase 1 — Reactor Scan** | Assessment & discovery (120 pts) |
| 10:15–12:15 | ⚛️ **Phase 2 — Core Upgrade** | Java 21 + Spring Boot 3 (250 pts) |
| 12:15–12:45 | 🍩 **Donut Break** | Mandatory. Homer would insist. |
| 12:45–13:30 | 🧯 **Phase 3 — Meltdown Prevention** | CVEs & secrets (150 pts) |
| 13:30–15:30 | 🚀 **Phase 4 — Cloud Evacuation** | Containerize & deploy to Azure (250 pts) |
| 15:30–16:15 | ✅ **Phase 5 — Safety Inspection** | Tests, smoke test, telemetry (130 pts) |
| 16:15–17:00 | 🏆 **Phase 6 — Audit Day** | Demo, retro, awards (100 pts) |

---

## 🎮 The Challenges

### H0 — Shift Start (gate, no points)
- [ ] Everyone has: VS Code, GitHub Copilot + **app modernization for Java** extension, JDK 21 (and the legacy JDK), Maven, Docker, Azure CLI + `azd`, access to a shared Azure subscription/resource group.
- [ ] Pick the victim app — either **your own legacy Java app**, or a stock legacy target such as a Spring Boot 2.x / Java 8 sample (e.g. an old PetClinic fork or `Azure-Samples/java-migration-copilot-samples`).
- [ ] Repo forked, everyone can build it *as-is* (`mvn clean package` on the old JDK).
- [ ] Tracker open on the big screen, timer started. ☢️

### Phase 1 — 🔍 Reactor Scan *(120 pts)*
| Pts | Challenge |
|---|---|
| 40 | Run the Copilot app modernization **assessment (AppCAT)** — produce the readiness report |
| 30 | Generate an **architecture diagram** + dependency map with Copilot |
| 30 | Triage: label every finding *upgrade / security / cloud-blocker* and assign to a squad |
| 20 | Write `AGENTS.md` / custom instructions so Copilot knows your project conventions |

### Phase 2 — ⚛️ Core Upgrade *(250 pts)* — the big one
| Pts | Challenge |
|---|---|
| 40 | Copilot generates the **upgrade plan** (Java 8→21, Boot 2→3.x); team reviews & approves it |
| 80 | Execute the upgrade with Copilot agent mode — `javax→jakarta`, deprecated APIs, build files |
| 50 | `mvn clean verify` **green on Java 21** |
| 40 | App boots locally & key endpoints respond |
| 40 | Upgrade summary: what Copilot changed, what humans fixed, weirdest diff |

### Phase 3 — 🧯 Meltdown Prevention *(150 pts)*
| Pts | Challenge |
|---|---|
| 50 | CVE scan of dependencies; Copilot drafts the fix plan |
| 50 | All **critical/high CVEs fixed**, build still green |
| 30 | Secrets/credentials removed from code & config (ready for managed identity) |
| 20 | Add a CI quality gate (GitHub Actions: build + test + dependency review) |

### Phase 4 — 🚀 Cloud Evacuation *(250 pts)*
| Pts | Challenge |
|---|---|
| 40 | Copilot generates the **containerization plan** + Dockerfile; image builds & runs locally |
| 50 | IaC generated (Bicep/Terraform via `azd`) for app + database + App Insights |
| 80 | **Deployed to Azure** (Container Apps or App Service) and reachable over HTTPS |
| 50 | Database on Azure with **managed identity** auth — zero connection-string passwords |
| 30 | Config externalized (App Configuration / env vars), one-command redeploy works |

### Phase 5 — ✅ Safety Inspection *(130 pts)*
| Pts | Challenge |
|---|---|
| 40 | Test suite green in the cloud build; add tests for anything Copilot touched blindly |
| 30 | Scripted **smoke test** against the live Azure endpoint passes |
| 30 | Telemetry visible in **Application Insights** (requests, failures, dependency calls) |
| 30 | Load sanity check (e.g. 100 requests) — no meltdowns, p95 recorded |

### Phase 6 — 🏆 Audit Day *(100 pts)*
| Pts | Challenge |
|---|---|
| 50 | 10-min live demo: old app vs new app, the journey, live Azure endpoint |
| 30 | Before/after one-pager: Java/Boot versions, CVE count, LOC changed, % done by Copilot |
| 20 | Retro: top 3 Copilot wins, top 3 gotchas, best prompt of the day (Scribe presents) |

### 🌟 Side Quests *(150 bonus pts — any squad, any time)*
| Pts | Quest |
|---|---|
| 30 | **Prompt Golf** — solve a migration task with the single most elegant Copilot prompt (team votes) |
| 30 | Generate integration tests with Testcontainers via Copilot, and they pass |
| 30 | Add a genuinely new feature to the modernized app in <30 min using Copilot |
| 30 | Cost estimate of the Azure footprint + one optimization applied |
| 30 | **Time Capsule** — commit a `MODERNIZATION.md` so good that next year's team needs zero context |

---

## 🏅 Awards (17:00)

- 🏆 **Employee of the Month** — squad with the most points on the leaderboard
- 🍩 **The Golden Donut** — best Copilot prompt of the day
- 🧯 **Safety Inspector** — the person who caught the nastiest bug/CVE
- 🎳 **Pin Pals** — best pairing moment (nominated by the team)
- ☢️ **"In this house we obey the laws of thermodynamics"** — most heroic fix under pressure

---

## 📏 Rules of the Plant

1. **Copilot first.** Try Copilot (agent mode / app modernization tools) before hand-editing. Hand-edits are allowed — but log them for the retro.
2. **Humans review everything.** No blind merges of agent output. Every phase has a 2-minute squad review.
3. **The tracker is law.** A challenge isn't done until it's checked on the big screen (Shift Supervisor verifies).
4. **Timebox, don't rabbit-hole.** Stuck >20 min → raise it at the reactor (tracker screen), swarm or skip.
5. **Demo > perfect.** A deployed app with one known wart beats a flawless app on localhost.

---

## ✅ Expected Outcomes

By end of day the team walks away with:
- A **real modernized app**: Java 21 / Spring Boot 3.x, CVE-clean, running on Azure with managed identity and telemetry.
- **Muscle memory** for the Copilot app modernization workflow: assess → plan → upgrade → secure → containerize → deploy.
- A reusable **playbook** (`MODERNIZATION.md`, prompts library, retro notes) for modernizing the *next* app.
- Data for leadership: % of migration executed by Copilot, time per phase, before/after CVE and version deltas.
- A team that had *fun* doing a migration. (Yes, really.)

---

## 🧰 Facilitator Checklist (day before)

- [ ] Azure subscription + resource group + RBAC for all 6 (Contributor on the RG)
- [ ] Quota check: Container Apps / App Service plan, Azure Database (PostgreSQL/MySQL), App Insights
- [ ] Copilot licenses with agent mode + app modernization extension verified on every laptop
- [ ] Victim app forked into a team org; branch protection off for the day
- [ ] [tracker.html](tracker.html) tested on the projector (state auto-saves in the browser)
- [ ] Donuts. Non-negotiable. 🍩
