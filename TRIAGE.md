# ☢️ Phase 1 Triage — Reactor Scan Findings

Every finding from the AppCAT assessment runs (`report-20260714100449`,
`report-20260714101315`) plus the Copilot-generated facts docs
(`.github/modernize/assessment/engines/facts/`) and the planted targets in
[README.md](README.md#the-planted-modernization-targets), labeled by type and
assigned to the squad that owns it per [HACKATHON.md](HACKATHON.md#-the-squads-6-people-3-pairs):

| Category | Owning squad | Why |
|---|---|---|
| 🔧 **upgrade** | 🟢 Core Crew | Java/Spring upgrade, build health |
| 🔒 **security** | 🟡 Safety Crew | CVEs, secrets, tests, quality gates |
| ☁️ **cloud-blocker** | 🔴 Launch Crew | Containerization, IaC, Azure deploy, telemetry |

**Totals: 26 findings → 8 upgrade · 13 security · 5 cloud-blocker**

---

## 🔧 Upgrade findings → 🟢 Core Crew

| # | Finding | Location | Severity/Effort | Source |
|---|---|---|---|---|
| U1 | Java 8 has reached end of support | `pom.xml` (`java.version`/source/target) | mandatory · 8 | `azure-java-version-01000` |
| U2 | Spring Framework 5.x has reached end of OSS support | `pom.xml` | mandatory · 8 | `spring-framework-version-01000` |
| U3 | Spring Boot 2.3.12 has reached end of OSS support | `pom.xml` | mandatory · 8 | `spring-boot-to-azure-spring-boot-version-01000` |
| U4 | Jakarta Persistence (`javax.persistence.*`) usage needs `jakarta.*` namespace migration | JPA entities (`Reactor`, `SafetyIncident`, `Employee`) | potential · 5 | `jakarta-database-00002` |
| U5 | SpringFox 2.9.2 (Swagger) is unmaintained and has no Spring Boot 3–compatible release | `SwaggerConfig.java`, `pom.xml` | — | `dependency-map.md` |
| U6 | JUnit 4 + `SpringRunner` tests — `junit-vintage-engine` isn't included by default in Boot 3's test starter | `src/test/**` | — | `dependency-map.md` |
| U7 | Legacy idioms: `new Integer(...)`, `Hashtable`, `StringBuffer` instead of modern equivalents | service classes | — | README planted targets |
| U8 | Static shared `SimpleDateFormat` — not thread-safe | `DateUtils.java` | — | README planted targets |

## 🔒 Security findings → 🟡 Safety Crew

| # | Finding | Location | Severity/Effort | Source |
|---|---|---|---|---|
| S1 | Hard-coded database password in configuration file | `application.properties` | **CRITICAL** | `azure-password-01000` / `configuration-inventory.md` |
| S2 | Hard-coded database username in configuration file | `application.properties` | HIGH | `configuration-inventory.md` |
| S3 | Hard-coded API key (`plant.api.key`) in configuration file | `application.properties` | **CRITICAL** | `configuration-inventory.md` |
| S4 | CRA: hard-coded password duplicated in Java source | `SecretConstants.java` | mandatory · 8 | `cra-hardcoded-credential-password-01000` |
| S5 | CRA: hard-coded API key/secret in Java source | `SecretConstants.java` | mandatory · 8 | `cra-hardcoded-credential-apikey-02000` |
| S6 | Backdoor auth header `X-Smithers-Token` checked against a hardcoded constant (dead-code auth bypass, no real access control) | `LegacyAuditFilter.java` | — | `api-service-contracts.md` |
| S7 | No Spring Security on the classpath — zero authentication/authorization anywhere in the app | app-wide | — | `api-service-contracts.md` |
| S8 | H2 web console exposed at `/h2-console` in all environments with hardcoded credentials — direct DB access vector | `application.properties` | — | `api-service-contracts.md` |
| S9 | No TLS configuration — app listens on plain HTTP | app-wide | — | `api-service-contracts.md` |
| S10 | `commons-text` 1.8 — CVE-2022-42889 (Text4Shell, CVSS 9.8) | `pom.xml` | CRITICAL | `dependency-map.md` |
| S11 | `commons-collections` 3.2.1 — deserialization gadget chain (CVE-2015-6420 and follow-ons) | `pom.xml` | HIGH | `dependency-map.md` |
| S12 | `guava` 20.0 — outdated, predates numerous security/API fixes (current stable 33.x) | `pom.xml` | MEDIUM | `dependency-map.md` |
| S13 | Every request logged via unstructured `System.out` — no audit trail, no redaction, nothing shippable to a SIEM | `LegacyAuditFilter.java`, `DataLoader.java` | — | README planted targets |

## ☁️ Cloud-blocker findings → 🔴 Launch Crew

| # | Finding | Location | Severity/Effort | Source |
|---|---|---|---|---|
| C1 | No Dockerfile found — app isn't containerized | repo root | mandatory · 3 | `dockerfile-00000` |
| C2 | Hardcoded `server.port` — needs to respect `SERVER_PORT`/Azure Container Apps port conventions | `application.properties` | potential · 1 | `spring-boot-to-azure-port-01000` |
| C3 | Restricted configuration properties (Config Server / Eureka) incompatible with Azure Container Apps | `application.properties` | potential · 2 | `spring-boot-to-azure-restricted-config-01000` |
| C4 | H2 in-memory database used as the sole "production" datastore — no persistence, not viable on Azure | `application.properties` | — | `dependency-map.md` / README |
| C5 | No managed-identity path to a database — current design is 100% credential-based, blocking the "zero passwords" Phase 4 goal | `application.properties`, `SecretConstants.java` | — | `plan.md` migration impact |

---

## Notes

- Findings **S1–S5** and **S10–S12** are the highest-priority items for Phase 3
  (Meltdown Prevention) — critical/high CVEs and hard-coded secrets must be
  cleared before anything ships.
- **U1–U4** are the hard dependency chain for Phase 2 (Java → Spring Framework →
  Spring Boot → JPA), already sequenced in
  [plan.md](.github/modernize/springfield-azure-modernization/plan.md).
- **C1–C5** can't be closed until the Phase 2/3 work lands (containerizing an
  app that still hard-codes secrets just ships the vulnerability faster).
- AppCAT's own CVE scanner (`security: []` in both reports) came back empty —
  S10–S12 were caught by the Copilot-generated `dependency-map.md` fact doc,
  not the AppCAT engine. Don't rely on AppCAT alone for CVE coverage; run
  `modernize-java-security` in Phase 3 to confirm against live advisory data.
