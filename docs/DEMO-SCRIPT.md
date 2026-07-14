# 🏆 Phase 6 — Live Demo Script (10 minutes)

*Prep only — a human presents this. Delete/adjust freely; this is a starting point,
not a script to read verbatim.*

## Before you start

- Have two tabs open: the **legacy seed commit** (`git show d64cdf6:pom.xml` or
  browse the repo at that commit) and the **live app**:
  https://ca-f26yymfqkwvk4.icyfield-7de6aa92.germanywestcentral.azurecontainerapps.io/
- Have `tracker.html` open on the big screen — it should show 780+/1,150 pts across
  Phases 2–5 fully green.

## Suggested flow (~10 min)

**1. The "before" (1–2 min)**
- Show `pom.xml` at the legacy seed commit: Java 8, Spring Boot 2.3.12, SpringFox.
- Point at `TRIAGE.md` — 26 findings, categorized upgrade/security/cloud-blocker.
- One-liner: "This app was a trap, on purpose — every smell in here is real and
  planted."

**2. The upgrade, live (2 min)**
- `mvn clean verify` locally — BUILD SUCCESS, Java 21, 32 tests green.
- Point at `UPGRADE-SUMMARY.md` for the Copilot-vs-human breakdown.
- Mention the weirdest diff: the backdoor `X-Smithers-Token` auth bypass got
  removed as a drive-by fix during the `javax`→`jakarta` migration.

**3. Security (1–2 min)**
- `CVE-SCAN.md`: one real CVE found and fixed (CVE-2026-55956, Tomcat) via a
  targeted `<tomcat.version>` override, not a blind Boot bump.
- `application.properties`: no hardcoded secrets — show the env-var placeholders.

**4. The live deployment (3–4 min) — the main event**
- Open the live endpoint in a browser: dashboard, `/api/reactors`,
  `/swagger-ui/index.html`.
- Run `scripts/smoke-test.sh <url>` on screen — 4/4 green.
- Show `az containerapp show ... env` — **no `SPRING_DATASOURCE_PASSWORD`
  anywhere** — zero passwords, managed identity + Postgres AAD auth.
- Mention the real blocker hit and fixed live: missing Postgres JDBC driver
  crash-looped the first deploy attempt — a genuine "it works on my (H2) machine"
  bug caught only by actually deploying.

**5. Cost + wrap (1 min)**
- `COST-ESTIMATE.md`: real Azure pricing, ~$67–69/month → ~$28–30/month after
  scale-to-zero.
- Close on the tracker: Phases 2–5 100%, Side Quests complete.

## Backup talking points if something's not live

- The Testcontainers integration test (`ReactorRepositoryPostgresIT`) proves the
  JPA layer works against real Postgres without needing the live Azure endpoint.
- `LOAD-TEST-RESULTS.md` has the numbers if the live endpoint is flaky on demo day.
