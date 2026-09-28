#!/usr/bin/env bash
# Calls the roadmap REST API: roadmap.sh <METHOD> <path under /api/v1> [json body]
#
# The token comes from ROADMAP_TOKEN, or else from ROADMAP_TOKEN in roadmap-app/.env. It is
# passed to curl through a header file and is never printed. The base URL comes from
# ROADMAP_URL and defaults to the hosted roadmap. The response body is printed; the exit code
# is non-zero for HTTP errors.
set -euo pipefail

if [ $# -lt 2 ]; then
    echo "usage: roadmap.sh <METHOD> <path> [json body]" >&2
    echo "example: roadmap.sh GET /systems/protocol" >&2
    exit 2
fi

METHOD="$1"
API_PATH="$2"
BODY="${3:-}"

ROOT="$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
TOKEN="${ROADMAP_TOKEN:-}"
if [ -z "$TOKEN" ] && [ -f "$ROOT/roadmap-app/.env" ]; then
    TOKEN="$(grep -E '^ROADMAP_TOKEN=' "$ROOT/roadmap-app/.env" | head -n 1 | cut -d= -f2- | tr -d '\r"')"
fi
if [ -z "$TOKEN" ]; then
    echo "ROADMAP_TOKEN is not set and roadmap-app/.env does not define it." >&2
    exit 1
fi

URL="${ROADMAP_URL:-https://rp.slne.dev}"

ARGS=(-sS --fail-with-body -X "$METHOD" "$URL/api/v1$API_PATH" --config -)
if [ -n "$BODY" ]; then
    ARGS+=(-H "Content-Type: application/json" -d "$BODY")
fi

printf 'header = "Authorization: Bearer %s"\n' "$TOKEN" | curl "${ARGS[@]}"
echo
