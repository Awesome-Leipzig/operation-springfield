# Phase 5 — Load Sanity Check

**Command**: `scripts/load-test.sh http://localhost:8094/api/reactors 100`
(local `mvn spring-boot:run`, Java 21, H2 in-memory datastore, single instance, no
concurrency — see notes below).

## Results

| Metric | Value |
|---|---|
| Requests | 100 |
| Success | 100 (100%) |
| Failures | 0 |
| p50 | 5.5 ms |
| p95 | 20.9 ms |
| p99 | 24.8 ms |

No meltdowns. ☕

## Notes

- This run is **local**, sequential (no concurrency), against the in-memory H2
  datastore — it validates the script and establishes a local baseline, not a
  production load profile.
- Re-run against the live Azure endpoint once Phase 4's deployment (human checkpoint)
  completes: `./scripts/load-test.sh https://<container-app-fqdn>/api/reactors 100`.
- If `hey` (https://github.com/rakyll/hey) is installed, the script automatically uses
  it instead for proper concurrent load (`-c 10`) rather than the sequential curl
  fallback used here.
