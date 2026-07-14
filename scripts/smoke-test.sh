#!/usr/bin/env bash
# Phase 5 — scripted smoke test against a live (or local) Sector 7G Safety Ledger
# endpoint. Exercises the same four endpoints validated locally in Phase 2/4:
# dashboard, reactors API, incidents API, and the OpenAPI/Swagger UI.
#
# Usage:
#   ./scripts/smoke-test.sh https://<your-container-app>.azurecontainerapps.io
#   ./scripts/smoke-test.sh http://localhost:8080   # local/dev run
#
# Exits non-zero (and prints which check failed) on any non-2xx response, so it
# can be wired straight into CI or a post-deploy step.

set -euo pipefail

BASE_URL="${1:-}"
if [[ -z "$BASE_URL" ]]; then
  echo "Usage: $0 <base-url>" >&2
  echo "Example: $0 https://ca-abc123.happybush-12345678.eastus.azurecontainerapps.io" >&2
  exit 2
fi
BASE_URL="${BASE_URL%/}"

declare -a CHECKS=(
  "/"
  "/api/reactors"
  "/api/incidents"
  "/swagger-ui/index.html"
)

failures=0
for path in "${CHECKS[@]}"; do
  url="${BASE_URL}${path}"
  status=$(curl -s -o /dev/null -w "%{http_code}" --max-time 15 "$url" || echo "000")
  if [[ "$status" =~ ^2 ]]; then
    echo "✅ $url -> $status"
  else
    echo "❌ $url -> $status"
    failures=$((failures + 1))
  fi
done

if [[ "$failures" -gt 0 ]]; then
  echo "Smoke test FAILED: $failures/${#CHECKS[@]} checks did not return 2xx." >&2
  exit 1
fi

echo "Smoke test PASSED: all ${#CHECKS[@]} checks returned 2xx."
