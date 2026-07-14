#!/usr/bin/env bash
# Phase 5 — load sanity check: fires N requests at an endpoint and reports p50/p95/p99
# latency plus the success rate. No external load-testing tool required (falls back to
# plain curl timing), but will use `hey` if it's on PATH for nicer concurrency.
#
# Usage:
#   ./scripts/load-test.sh https://<your-container-app>.azurecontainerapps.io/api/reactors 100
#   ./scripts/load-test.sh http://localhost:8080/api/reactors   # defaults to 100 requests

set -euo pipefail

URL="${1:-}"
COUNT="${2:-100}"
if [[ -z "$URL" ]]; then
  echo "Usage: $0 <url> [request-count=100]" >&2
  exit 2
fi

if command -v hey >/dev/null 2>&1; then
  echo "Using 'hey' for $COUNT requests against $URL"
  hey -n "$COUNT" -c 10 "$URL"
  exit 0
fi

echo "'hey' not found; falling back to a sequential curl-based timing loop ($COUNT requests)."
tmpfile="$(mktemp)"
trap 'rm -f "$tmpfile"' EXIT

success=0
for ((i = 1; i <= COUNT; i++)); do
  timing=$(curl -s -o /dev/null -w "%{http_code} %{time_total}" --max-time 15 "$URL" || echo "000 0")
  code="${timing%% *}"
  time_s="${timing##* }"
  echo "$time_s" >> "$tmpfile"
  if [[ "$code" =~ ^2 ]]; then
    success=$((success + 1))
  fi
done

# Sort timings (seconds, ascending) and pick p50/p95/p99 by index.
sorted=$(sort -n "$tmpfile")
total=$(wc -l < "$tmpfile")
p50_idx=$(( (total * 50 + 99) / 100 ))
p95_idx=$(( (total * 95 + 99) / 100 ))
p99_idx=$(( (total * 99 + 99) / 100 ))
p50=$(echo "$sorted" | sed -n "${p50_idx}p")
p95=$(echo "$sorted" | sed -n "${p95_idx}p")
p99=$(echo "$sorted" | sed -n "${p99_idx}p")

echo "---"
echo "Requests: $total  Success: $success  Failure: $((total - success))"
echo "p50: ${p50}s  p95: ${p95}s  p99: ${p99}s"
