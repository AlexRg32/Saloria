#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 /path/to/backup.sql.gz"
  exit 1
fi

BACKUP_FILE=$1

if [[ ! -f "${BACKUP_FILE}" ]]; then
  echo "Backup file not found: ${BACKUP_FILE}"
  exit 1
fi

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
ROOT_DIR=$(cd -- "${SCRIPT_DIR}/.." && pwd)
ENV_FILE="${ENV_FILE:-${ROOT_DIR}/.env.prod}"

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

gunzip -c "${BACKUP_FILE}" | docker compose \
  -f "${ROOT_DIR}/docker-compose.prod.yml" \
  --profile local-db \
  exec -T db \
  psql \
  -U "${POSTGRES_USER:-peluqueria}" \
  -d "${POSTGRES_DB:-peluqueria_db}"

echo "Restore completed from ${BACKUP_FILE}"
