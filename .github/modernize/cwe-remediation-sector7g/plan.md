# Modernization Plan: CWE Remediation - Sector 7G Safety Ledger

**Project**: sector-7g-safety-ledger

---

## Technical Framework

- **Language**: Java 21
- **Framework**: Spring Boot (Spring Framework)
- **Build Tool**: Maven
- **Database**: JPA-backed persistence (Spring Data JPA)
- **Key Dependencies**: Spring Web, Spring Data JPA

---

## Overview

This plan remediates the source-code security weaknesses (CWE findings) identified in
the security assessment `report-20260714114157`. The application currently persists
domain entities with little or no validation, contains dead and overflow-prone logic,
bypasses its own service layer for data access, does not enforce API-key access
control, and lacks an audit trail for security-critical writes. This plan will:

- Ensure critical entity fields are initialized and validated before persistence.
- Remove dead/always-false logic and prevent silent integer-overflow miscalculations.
- Route data access through the designated service layer to enforce business rules.
- Enforce access control on safety-critical endpoints and add audit logging.

The work is scoped strictly to the seven selected CWE categories and is sequenced so
that overlapping files (controllers, services) are modified one task at a time.

---

## Scope

Scoped to the following selected assessment categories:

| # | CWE | Title | Category | Severity | Story Points |
|---|-----|-------|----------|----------|--------------|
| 1 | CWE-456 | Missing Initialization of a Variable | Code Quality | Potential | 2 |
| 2 | CWE-570 | Expression is Always False | Code Quality | Optional | 1 |
| 3 | CWE-665 | Improper Initialization | Code Quality | Potential | 3 |
| 4 | CWE-682 | Incorrect Calculation | Code Quality | Potential | 5 |
| 5 | CWE-1057 | Data Access Operations Outside of Expected Data Manager Component | Code Quality | Potential | 5 |
| 6 | CWE-732 | Incorrect Permission Assignment for Critical Resource | Credentials & Secrets | Optional | 5 |
| 7 | CWE-778 | Insufficient Logging | Credentials & Secrets | Potential | 3 |

---

## Remediation Impact Summary

| Task | CWE | Primary Affected Files |
|------|-----|------------------------|
| 001 | CWE-456 | `model/SafetyIncident.java`, `web/IncidentController.java` |
| 002 | CWE-570 | `service/IncidentService.java` |
| 003 | CWE-665 | `web/ReactorController.java`, `web/IncidentController.java` |
| 004 | CWE-682 | `service/ReactorService.java`, `service/IncidentService.java` |
| 005 | CWE-1057 | `bootstrap/DataLoader.java` |
| 006 | CWE-732 | `config/LegacyAuditFilter.java` |
| 007 | CWE-778 | `service/IncidentService.java`, `service/ReactorService.java` |

---

## Tasks

Detailed task definitions (requirements, dependencies, success criteria) are tracked in
[.metadata/tasks.json](.metadata/tasks.json). Tasks execute sequentially in the order
below to avoid conflicting edits to shared controller and service files.

1. **001 — Resolve CWE-456**: Initialize/validate critical `SafetyIncident` fields before persistence.
2. **002 — Resolve CWE-570**: Remove the always-false dead-code branch in `IncidentService.capitalizeWords()`.
3. **003 — Resolve CWE-665**: Add controller-boundary validation for reactor and incident creation.
4. **004 — Resolve CWE-682**: Prevent silent integer overflow in reactor-output and donut aggregations.
5. **005 — Resolve CWE-1057**: Route `DataLoader` data access through the service layer.
6. **006 — Resolve CWE-732**: Enforce API-key access control on safety-critical endpoints.
7. **007 — Resolve CWE-778**: Add SLF4J audit logging to security-critical write operations.

**Success criteria (all tasks)**: the project compiles (`mvn clean verify`) and all
existing unit tests pass after each remediation.

---

## Open Questions & Questionnaire

_No clarification questions were raised. Each selected category maps to exactly one
remediation solution, so no disambiguation was required._
