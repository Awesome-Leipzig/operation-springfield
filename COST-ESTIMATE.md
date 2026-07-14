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
traffic, and scales back up automatically on the next incoming request (cold start
adds a few seconds of latency — acceptable for a dev/hackathon endpoint, not for a
latency-sensitive production SLA).

**Applied and verified live**: `azd provision` re-deployed the change; confirmed via
`az containerapp show` that `minReplicas` is now `0`; re-ran `scripts/smoke-test.sh`
against the live endpoint — all 4 checks still return 200 (the scale rule correctly
wakes a replica on demand).

**New estimated total: ≈ $28–30/month** (saves ~$39/month, ~57% reduction) when the
app is mostly idle between demo/testing sessions — actual savings scale with how much
of the month the app sits at zero traffic.

## Other optimization candidates (not applied, noted for later)

- Postgres Burstable B1ms is already the smallest paid tier; further savings would
  require stopping the server entirely between sessions (`az postgres flexible-server
  stop`), which isn't automated here since Postgres doesn't support consumption-based
  scale-to-zero the way Container Apps does.
- Container Registry Basic ($5.10/month flat) has no lower paid tier; the only
  further optimization would be deleting the registry entirely between hackathon
  sessions and re-creating it on next `azd up` (adds ~1 minute to redeploy).
