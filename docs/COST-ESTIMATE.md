# Side Quest — Azure Cost Estimate + Optimization

**Scope**: pricing pulled live from the Azure Retail Prices API
(`https://prices.azure.com/api/retail/prices`) for `germanywestcentral`, matched
against the actual SKUs deployed in `rg-swo-gh-hackathon-team2` — not a generic
estimate, the real provisioned configuration.

## Deployed resources & monthly cost (before optimization)

| Resource | SKU | Meter | Unit price | Monthly estimate* |
|---|---|---|---|---|
| Postgres Flexible Server (compute) | Standard_B1ms, Burstable | B1MS compute | $0.0199/hour | **$14.53** |
| Postgres Flexible Server (storage) | 32 GB | Storage Data Stored | $0.14/GB/month | **$4.48** |
| Postgres Flexible Server (backup) | 7-day retention, LRS | Backup Storage Data Stored | $0.10/GB/month | **~$2** (small DB) |
| Container App (compute) | 0.5 vCPU / 1 GiB, **minReplicas: 1** (always on) | vCPU Active + Memory Active | $0.000024/vCPU-sec, $0.000003/GiB-sec | **$39.42** |
| Container Registry | Basic | Basic Registry Unit | $0.17/day | **$5.10** |
| Log Analytics + App Insights | PerGB2018 | Pay-as-you-go Data Ingestion | $2.99/GB | **~$1–3** (low hackathon traffic) |
| Key Vault | Standard | Per 10K operations | negligible | **<$0.10** |
| **Total (before optimization)** | | | | **≈ $67–69/month** |

\* 730 hours/month assumed for always-on compute; USD pricing (region default currency).

## ⚠️ Update: scale-to-zero was reverted after a real production incident

**This optimization was applied, verified working, then had to be reverted** — see
below. Left here in full because the incident and its diagnosis are the actual
valuable lesson learned, not just the happy-path story.

## Optimization applied: scale-to-zero on the Container App

The single largest line item was the Container App's **always-on replica**
(`minReplicas: 1`) at **~$39/month** — over half the total bill — for a
hackathon/dev workload that doesn't need 24/7 uptime.

**Change** (`infra/resources.bicep`):
```diff
- minReplicas: 1
+ minReplicas: 0
+ rules: [{ name: 'http-scale-rule', http: { metadata: { concurrentRequests: '10' } } }]
```

With `minReplicas: 0` and an HTTP concurrency scale rule, the Container App scales
down to **zero replicas** (zero compute cost) after the `cooldownPeriod` (300s) of no
traffic, and scales back up automatically on the next incoming request. Initially
verified working: `azd provision` re-deployed the change, `az containerapp show`
confirmed `minReplicas: 0`, and `scripts/smoke-test.sh` passed immediately after.

**New estimated total at the time: ≈ $28–30/month** (~57% reduction).

## 🐛 The incident: cold-start replica race corrupted the schema

Sometime after the initial verification, live traffic (smoke tests, load tests, and
a manual browser check) arrived after the app had scaled to zero. Container Apps
cold-started **two replicas concurrently** to handle the burst. Both replicas
independently ran Hibernate's schema management (`spring.jpa.hibernate.ddl-auto`,
originally `create-drop` everywhere) against the same empty Postgres database **at
the same time**:

- Both replicas' Hibernate startup saw no `reactor`/`employee`/`safety_incident`
  tables and both attempted `CREATE TABLE` concurrently.
- One replica's transaction won; the other's `CREATE TABLE` failed, corrupting that
  replica's Hibernate session for its entire lifetime.
- Container Apps round-robins requests across replicas, so roughly half of all
  requests hit the broken replica and got `500 Internal Server Error` /
  `relation "reactor" does not exist`, while the other half (routed to the healthy
  replica) succeeded — explaining the intermittent, confusing symptom the user saw
  ("the page is loading" one moment, 500s the next).

**Root cause, layered:**
1. `create-drop` is fine for the ephemeral H2 dev database but actively dangerous
   against a shared, persistent Postgres instance — first fix: made it configurable
   (`SPRING_JPA_HIBERNATE_DDL_AUTO`, defaulting to `update` in production via
   `infra/resources.bicep`) so a replica restart never *drops* existing tables.
2. Even `update` mode isn't safe for **concurrent, simultaneous first-time schema
   creation** — Hibernate's auto-DDL has no built-in distributed lock, so two
   replicas racing to create the same missing tables for the first time can still
   corrupt one of them.
3. The real trigger was **`minReplicas: 0`**: scaling from zero is exactly the
   condition where multiple replicas can cold-start simultaneously against an empty
   database. Scaling *out* from an already-running, already-schema'd replica
   (1 → 3 under load) never has this problem, since by then the schema already
   exists and `update` is a safe no-op.

**Fix**: reverted `minReplicas` to `1` in `infra/resources.bicep`. Exactly one
replica is now always running, so schema creation only ever happens once,
non-concurrently, on the very first deploy. Scale-out (1 → 3) under load remains
enabled and safe.

**Verified after the revert**: `mvn clean verify` still green locally; redeployed
via `azd provision` + `azd deploy`; `scripts/smoke-test.sh` — 4/4 checks 200;
`scripts/load-test.sh` — 100/100 requests succeeded (p50 81ms / p95 104ms / p99
120ms); confirmed no data duplication (still exactly 4 reactors / 5 incidents,
matching the original seed — the `DataLoader` guard added alongside this fix
correctly skipped re-seeding since the tables already had data); telemetry
confirmed still flowing in Application Insights; zero-passwords claim re-confirmed
(`SPRING_DATASOURCE_PASSWORD` still absent from the container env).

## Corrected total: no cost optimization currently applied

**≈ $67–69/month** — back to the original estimate. Scale-to-zero remains a valid
idea in principle, but this app's Hibernate auto-DDL approach makes it unsafe
without further work (see below). This is an honest "we tried it, it broke
production, we reverted it" outcome — arguably a more useful Side Quest artifact
than a cost graph that quietly hid a real bug.

## Path to safely re-enable scale-to-zero (not implemented, noted for later)

- Replace Hibernate auto-DDL entirely with a proper migration tool (Flyway or
  Liquibase), which uses a database-level lock so only one instance can run
  migrations at a time regardless of how many replicas cold-start concurrently.
  This is the correct long-term fix and would make `minReplicas: 0` safe again.
- Alternatively, set `spring.jpa.hibernate.ddl-auto=validate` in production (never
  create or modify schema at runtime) and run schema creation as a one-time,
  separate deploy step — also removes the race entirely, at the cost of an extra
  manual/CI step before first deploy to a new environment.

## Other optimization candidates (not applied, noted for later)

- Postgres Burstable B1ms is already the smallest paid tier; further savings would
  require stopping the server entirely between sessions (`az postgres flexible-server
  stop`), which isn't automated here since Postgres doesn't support consumption-based
  scale-to-zero the way Container Apps does.
- Container Registry Basic ($5.10/month flat) has no lower paid tier; the only
  further optimization would be deleting the registry entirely between hackathon
  sessions and re-creating it on next `azd up` (adds ~1 minute to redeploy).
