# Phase 5 — Load Sanity Check

**Local baseline**: `scripts/load-test.sh http://localhost:8094/api/reactors 100`
(local `mvn spring-boot:run`, Java 21, H2 in-memory datastore, single instance, no
concurrency).

## Local results

| Metric | Value |
|---|---|
| Requests | 100 |
| Success | 100 (100%) |
| Failures | 0 |
| p50 | 5.5 ms |
| p95 | 20.9 ms |
| p99 | 24.8 ms |

## Live Azure results (final, post-incident-fix)

**Command**: `scripts/load-test.sh https://ca-f26yymfqkwvk4.icyfield-7de6aa92.germanywestcentral.azurecontainerapps.io/api/reactors 100`
— run against the live Container App (Postgres Flexible Server, `minReplicas: 1`,
after the scale-to-zero cold-start race incident was fixed; see
[COST-ESTIMATE.md](COST-ESTIMATE.md)).

| Metric | Value |
|---|---|
| Requests | 100 |
| Success | 100 (100%) |
| Failures | 0 |
| p50 | 81 ms |
| p95 | 104 ms |
| p99 | 120 ms |

No meltdowns. ☕ Also confirmed no data duplication from the DataLoader guard: still
exactly 4 reactors / 5 incidents after the load test, matching the original seed.

## Notes

- The local run validates the script and establishes a local baseline (H2,
  sequential, no network hop) — the live Azure numbers above are the real
  production-representative measurement.
- If `hey` (https://github.com/rakyll/hey) is installed, the script automatically uses
  it instead for proper concurrent load (`-c 10`) rather than the sequential curl
  fallback used here.
