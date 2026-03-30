#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
REPO_DIR=$(cd -- "${SCRIPT_DIR}/../../.." && pwd)
DEPLOY_DIR="${REPO_DIR}/deploy/raspberry"
ENV_FILE="${ENV_FILE:-${DEPLOY_DIR}/.env.prod}"
GIT_REMOTE="${GIT_REMOTE:-origin}"
DEPLOY_BRANCH="${DEPLOY_BRANCH:-main}"
DEPLOY_PROFILES="${DEPLOY_PROFILES:-cloudflare}"
SKIP_GIT_PULL="${SKIP_GIT_PULL:-0}"
HEALTHCHECK_ATTEMPTS="${HEALTHCHECK_ATTEMPTS:-20}"
HEALTHCHECK_INTERVAL="${HEALTHCHECK_INTERVAL:-3}"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "Missing env file: ${ENV_FILE}"
  exit 1
fi

if [[ ! -d "${REPO_DIR}/.git" ]]; then
  echo "Repository not found at ${REPO_DIR}. Clone the repo on the Raspberry first."
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

if [[ "${SKIP_GIT_PULL}" != "1" ]]; then
  current_branch=$(git -C "${REPO_DIR}" branch --show-current)

  if [[ "${current_branch}" != "${DEPLOY_BRANCH}" ]]; then
    git -C "${REPO_DIR}" checkout "${DEPLOY_BRANCH}"
  fi

  git -C "${REPO_DIR}" pull --ff-only "${GIT_REMOTE}" "${DEPLOY_BRANCH}"
fi

profile_args=()
for profile in ${DEPLOY_PROFILES//,/ }; do
  if [[ -n "${profile}" ]]; then
    profile_args+=(--profile "${profile}")
  fi
done

run_compose_up() {
  docker compose \
    --env-file "${ENV_FILE}" \
    -f "${DEPLOY_DIR}/docker-compose.prod.yml" \
    "${profile_args[@]}" \
    up -d --build --remove-orphans
}

if ! run_compose_up; then
  echo "Docker Compose build failed on Raspberry. Cleaning builder state and retrying once without BuildKit..."
  docker buildx stop >/dev/null 2>&1 || true
  docker buildx rm --all-inactive --force >/dev/null 2>&1 || true
  docker builder prune --all --force >/dev/null 2>&1 || true

  COMPOSE_DOCKER_CLI_BUILD=0 DOCKER_BUILDKIT=0 run_compose_up
fi

for attempt in $(seq 1 "${HEALTHCHECK_ATTEMPTS}"); do
  if "${DEPLOY_DIR}/scripts/healthcheck.sh" "${APP_API_BASE_URL:-http://localhost:8080}"; then
    exit 0
  fi

  if [[ "${attempt}" -lt "${HEALTHCHECK_ATTEMPTS}" ]]; then
    sleep "${HEALTHCHECK_INTERVAL}"
  fi
done

echo "Healthcheck failed after ${HEALTHCHECK_ATTEMPTS} attempts."
exit 1
