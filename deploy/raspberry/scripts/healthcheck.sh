#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${1:-${APP_API_BASE_URL:-http://localhost:8080}}"

curl -fsS "${BASE_URL%/}/v3/api-docs" >/dev/null
echo "Healthcheck OK for ${BASE_URL%/}/v3/api-docs"
