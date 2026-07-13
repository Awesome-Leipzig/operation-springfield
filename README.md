# ☢️ Sector 7G Safety Ledger

The Springfield Nuclear Power Plant's safety ledger — a **deliberately legacy**
Java 8 / Spring Boot 2.3 app. It is the "victim app" for
**[Operation Springfield](HACKATHON.md)**, an 8-hour GitHub Copilot Java
modernization hackathon.

> Every `☢️ LEGACY ALERT` comment in the code marks something the hackathon
> challenges will make you fix — with Copilot doing the heavy lifting.

## What's in the box

| File | What it is |
|---|---|
| [HACKATHON.md](HACKATHON.md) | The full hackathon plan: goals, squads, phases, points, rules |
| [tracker.html](tracker.html) | Live progress tracker — open it on the big screen, no server needed |
| `src/` | The legacy app you'll modernize |

## Run the legacy app (before you fix it)

**Prereqs:** JDK 8 or 11, Maven 3.6+

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
| http://localhost:8080/swagger-ui.html | SpringFox Swagger UI (doomed in Boot 3) |
| http://localhost:8080/h2-console | H2 console |

Run the (JUnit 4) tests:

```bash
mvn test
```

## The planted modernization targets

This app is a trap, on purpose. Your Copilot-powered mission:

| Smell | Where | Fix (Phase) |
|---|---|---|
| Java 8 + Spring Boot 2.3 (EOL) | `pom.xml` | Upgrade to Java 21 + Boot 3.x (Phase 2) |
| `javax.persistence` / `javax.servlet` | entities, `LegacyAuditFilter` | `jakarta.*` (Phase 2) |
| SpringFox 2.9 | `SwaggerConfig` | springdoc-openapi (Phase 2) |
| JUnit 4 + `SpringRunner` | `src/test` | JUnit 5 (Phase 2) |
| `new Integer(...)`, `Hashtable`, `StringBuffer` | services | Modern Java (Phase 2) |
| Static shared `SimpleDateFormat` (not thread-safe) | `DateUtils` | `java.time` (Phase 2) |
| CVE-laden deps: commons-text 1.8, commons-collections 3.2.1, guava 19 | `pom.xml` | Bump/remove (Phase 3) |
| Hardcoded credentials (fake) | `SecretConstants`, `application.properties` | Managed identity / env config (Phase 3) |
| `System.out` logging | `LegacyAuditFilter`, `DataLoader` | SLF4J + App Insights (Phase 4–5) |
| H2 in-memory "production" DB | `application.properties` | Azure database (Phase 4) |

## How to run the hackathon

1. Read [HACKATHON.md](HACKATHON.md) — goals, 3 squads, 6 phases, points.
2. Open [tracker.html](tracker.html) on a projector. Hit **Start shift**.
3. Fork this repo, make sure everyone can build it *as-is*.
4. Point GitHub Copilot's **app modernization for Java** tooling at it and start Phase 1.
5. Check off challenges on the tracker as you go. Confetti is earned, not given.

*All credentials in this repository are fictional and planted for training. Any resemblance to real secrets, living or rotated, is purely coincidental.*
