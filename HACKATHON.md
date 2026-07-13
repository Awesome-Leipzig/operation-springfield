# ☕ OPERATION FRESH BREW
### A GitHub Copilot Java Modernization Hackathon — *Contain the Legacy Meltdown*

> **The story:** Leipzig is Gotham tonight. Rain on the rooftops, sirens in the distance,
> and beneath the city hums a reactor controlled by one app: a Java 8 / Spring Boot 2.3
> relic last deployed when its coffee machine still took coins. Tomorrow morning the
> auditors descend, and if the app fails, the reactor fails — and the city goes dark
> and glowing. There is no Batman. There is only the signal in the sky: **☕**. It shines
> over an office coffee corner where six Java developers answer the call the only way
> they know how — they order another round and open VS Code. No capes, no gadgets;
> just laptops, double espressos, and **GitHub Copilot app modernization**. You have
> **8 hours** to brew stale Java into fresh Java — scan it, upgrade it, secure it, ship
> it to Azure — before the reactor melts down. Leipzig deserves better heroes.
> Tonight, it gets caffeinated ones.

---

## 📋 At a Glance

| | |
|---|---|
| **Duration** | 8 hours (one day, e.g. 09:00–17:00) |
| **Team** | 6 engineers → 3 squads of 2 |
| **Difficulty** | High — but every phase is achievable with Copilot doing the heavy lifting |
| **Core toolchain** | VS Code + GitHub Copilot agent mode + **GitHub Copilot app modernization** extension — full list in [Toolchain & Prerequisites](#-toolchain--prerequisites-install-before-shift-start) |
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

## 🧰 Toolchain & Prerequisites (install before shift start)

### Accounts & licenses

| Requirement | Notes |
|---|---|
| GitHub account + **GitHub Copilot subscription** | Any plan (Free/Pro/Pro+/Business/Enterprise); agent mode enabled |
| Azure subscription | Contributor on a shared resource group (needed for Phase 4 only) |

### Per-laptop software (all 6 people)

| Tool | Version / details |
|---|---|
| **Visual Studio Code** | 1.113 or later |
| **GitHub Copilot in VS Code** | Signed in to your GitHub account (Copilot + Copilot Chat) |
| **GitHub Copilot app modernization** extension | Marketplace ID: `vscjava.migrate-java-to-azure` — restart VS Code after install |
| Extension Pack for Java | `vscjava.vscode-java-pack` (language support, debugger, Maven) |
| **JDK — two versions** | Legacy JDK (8 or 11) to run the app as-is **and** target JDK 21 |
| **Maven** | 3.6+ with access to Maven Central |
| Git | Project must be Git-managed (it is) |
| Docker Desktop | Phase 4 containerization |
| Azure CLI (`az`) + Azure Developer CLI (`azd`) | Phase 4 deploy; run `az login` and `azd auth login` beforehand |

> **AppCAT** (the assessment engine) does NOT need pre-installing — the modernization
> agent detects and installs it on first “Start Assessment” run. Budget ~5 min for that
> first run, or trigger it once the day before.

### The Copilot agents you'll drive all day

| Agent / entry point | What it does | Used in |
|---|---|---|
| **GitHub Copilot modernization pane** (VS Code sidebar) | *Start Assessment*, *Upgrade Java Runtime & Frameworks*, task tree (Upgrade Spring Boot, Jakarta EE, Generate Unit Test Cases…) | Phases 1, 2, 5 |
| **`modernize` custom agent** (Copilot Chat agent picker) | Orchestrates assess → plan → execute; generates `plan.md` / `progress.md` / `summary.md`; uses Claude Sonnet by default | Phases 1–3 |
| **`modernize-java-upgrade`** | JDK/Spring/Jakarta upgrade with build + test validation loop | Phase 2 |
| **`modernize-java-security`** | CVE scan and dependency fixes | Phase 3 |
| **Copilot agent mode** (plain chat) | Everything else: Dockerfile, IaC, tests, smoke scripts — e.g. *“Upgrade my Java project to Java 21”* also works as a plain prompt | All phases |

### Optional: terminal-only path (GitHub Copilot CLI)

For squads that prefer the CLI over VS Code:

```bash
npm install -g @github/copilot
copilot plugin marketplace add microsoft/modernize-java
copilot plugin install modernize-java@modernize-java
# then, from the project directory:
copilot --model claude-sonnet-4.6 --agent modernize-java:modernize-java
# prompt: upgrade to Java 21 + Spring Boot 3.x
```

### 10-minute preflight (run on every laptop the day before)

```powershell
code --version          # >= 1.113
code --list-extensions | Select-String "vscjava.migrate-java-to-azure"
java -version           # legacy JDK on PATH (8 or 11)
mvn -version            # 3.6+
docker --version
az account show         # logged in, correct subscription
azd version
mvn clean package       # legacy app builds as-is ✔
```

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
- [ ] [Toolchain & Prerequisites](#-toolchain--prerequisites-install-before-shift-start) green on all 6 laptops (preflight script passes).
- [ ] Pick the victim app — this repo's **Sector 7G Safety Ledger**, your own legacy Java app, or a stock target like `Azure-Samples/java-migration-copilot-samples`.
- [ ] Repo forked, everyone can build it *as-is* (`mvn clean package` on the old JDK).
- [ ] Everyone has opened the **GitHub Copilot modernization pane** once (AppCAT installed).
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
- [ ] Copilot licenses active for all 6; agent mode verified in VS Code (≥ 1.113)
- [ ] **GitHub Copilot app modernization** extension (`vscjava.migrate-java-to-azure`) installed on every laptop; run one assessment so AppCAT is downloaded
- [ ] Both JDKs (legacy 8/11 + target 21), Maven, Docker, `az`, `azd` installed — run the preflight script from the Toolchain section
- [ ] Optional: Copilot CLI + `modernize-java` plugin for terminal fans
- [ ] Victim app forked into a team org; branch protection off for the day
- [ ] [tracker.html](tracker.html) tested on the projector (state auto-saves in the browser)
- [ ] Donuts. Non-negotiable. 🍩
