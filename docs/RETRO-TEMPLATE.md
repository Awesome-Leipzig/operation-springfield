# 🏆 Phase 6 — Retro Template

*Prep only — humans fill this in during the retro. Scribe: capture verbatim where
possible, especially the prompts.*

## Top 3 Copilot wins

1.  Deployment of the infrastructure-as-code (Bicep) scaffolding for Container Apps, Key Vault, and Postgres in one pass, with minimal human intervention.
2.  Full `javax.*` → `jakarta.*` + Spring Boot 4 migration in a single agent pass — no manual namespace edits needed.
3.  CVE-2026-55956 (Tomcat) caught and patched before the auditors arrived — Copilot identified the fix the Boot BOM hadn't caught up to yet.

*(Prompt: was there a single prompt/session that saved the most time? The
`javax`→`jakarta` + dependency migration in one pass, or the Bicep IaC scaffolding,
are candidates worth nominating.)*

## Top 3 gotchas

1. GitHub Copilot on the platform couldn't fix the CI workflow — Copilot in IDE fixed it in one pass. Context matters: the IDE agent had full file access, the platform version didn't.
2. Copilot quietly removed the `X-Smithers-Token` backdoor check during the `javax`→`jakarta` pass — correct thing to do, but it went beyond the asked scope and needed a human review to catch the unannounced change.
3. Copilot-generated Bicep assumed Contributor RBAC was sufficient for managed identity role assignments — deploy failed because `roleAssignments/write` requires Owner. Always prompt Copilot to include permission prerequisites for IaC.

*(Candidates from this run, if the team wants a starting point: Spring Boot 4's
module splitting breaking `@WebMvcTest`/`@DataJpaTest` imports silently; the
Postgres passwordless JDBC driver + auth-plugin dependency not being obvious until
the container crash-looped; RBAC `roleAssignments/write` needing Owner, not
Contributor.)*

## Best prompt of the day 🍩 (Golden Donut nominee)

**Prompt:** Use the playwright mcp to navigate to https://awesome-leipzig.github.io/operation-springfield/tracker.html and write a plan for implementation of all the tasks provided there. The stage 1 is already done and does not need to be recognized. Create a plan for each stage. Goal is to have a loop for subagents which run in a loop to fulfill the tasks and validate the functionality

**Why it won:** It modernized, deployed and tested the whole application by itself with minor human intervention for 3 hours.

## 🧯 Safety Inspector nominee (nastiest bug/CVE caught)

*Candidates: CVE-2026-55956 (Tomcat), the Jackson `donuts`/`donutsConsumedDuringIncident`
constructor-parameter mismatch, or the `LegacyAuditFilterTest` drift caught by CI on
a shared branch.*

## 🎳 Pin Pals nominee (best pairing moment)

## ☢️ "In this house we obey the laws of thermodynamics" nominee (heroic fix under pressure)

*Candidate: diagnosing and fixing the live Postgres JDBC driver crash-loop during
the actual deploy, under time pressure, without breaking the already-green build.*

## Anything for next year's team?

*(See [MODERNIZATION.md](MODERNIZATION.md)'s "Lessons for next year's team" section
for a running list — add anything missing here.)*
