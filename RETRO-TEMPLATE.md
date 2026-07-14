# 🏆 Phase 6 — Retro Template

*Prep only — humans fill this in during the retro. Scribe: capture verbatim where
possible, especially the prompts.*

## Top 3 Copilot wins

1.
2.
3.

*(Prompt: was there a single prompt/session that saved the most time? The
`javax`→`jakarta` + dependency migration in one pass, or the Bicep IaC scaffolding,
are candidates worth nominating.)*

## Top 3 gotchas

1.
2.
3.

*(Candidates from this run, if the team wants a starting point: Spring Boot 4's
module splitting breaking `@WebMvcTest`/`@DataJpaTest` imports silently; the
Postgres passwordless JDBC driver + auth-plugin dependency not being obvious until
the container crash-looped; RBAC `roleAssignments/write` needing Owner, not
Contributor.)*

## Best prompt of the day 🍩 (Golden Donut nominee)

**Prompt:**

**Why it won:**

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
