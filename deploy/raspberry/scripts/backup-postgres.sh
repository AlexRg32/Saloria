#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
ROOT_DIR=$(cd -- "${SCRIPT_DIR}/.." && pwd)
ENV_FILE="${ENV_FILE:-${ROOT_DIR}/.env.prod}"
OUTPUT_DIR="${BACKUP_DIR:-${ROOT_DIR}/backups}"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

mkdir -p "${OUTPUT_DIR}"

docker compose \
  -f "${ROOT_DIR}/docker-compose.prod.yml" \
  --profile local-db \
  exec -T db \
  pg_dump \
  --clean \
  --if-exists \
  -U "${POSTGRES_USER:-peluqueria}" \
  "${POSTGRES_DB:-peluqueria_db}" | gzip > "${OUTPUT_DIR}/peluqueria-db-${TIMESTAMP}.sql.gz"

echo "Backup created at ${OUTPUT_DIR}/peluqueria-db-${TIMESTAMP}.sql.gz"
