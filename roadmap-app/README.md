# surf-roleplay Roadmap

Self-hosted roadmap and tracker for the surf-roleplay gamemode: system catalogue with
full specifications, kanban board, phase roadmap, decisions, open questions, people
and an activity log. Next.js 15, Tailwind, Drizzle and SQLite.

## Configuration

| Variable | Required | Default | Meaning |
| --- | --- | --- | --- |
| `ROADMAP_TOKEN` | yes | none | Shared login token. The app refuses to start without it. |
| `DATABASE_PATH` | no | `./data/roadmap.db` (`/app/data/roadmap.db` in Docker) | SQLite file. |
| `COOKIE_SECURE` | no | `false` | Set to `true` when served over HTTPS. |
| `REPO_URL` | no | `https://github.com/ammodev/surf-roleplay` | Base URL commit hashes in progress updates link to. |

On first start the database is created and filled from `seed/roadmap.ts`. After that
the database is the source of truth; editing the seed does not change existing data.

## Run locally

```bash
npm ci
ROADMAP_TOKEN=change-me npm run dev
```

Checks: `npm run lint`, `npm run typecheck`, `npm test`, `npm run build`.

## Run with Docker

```bash
ROADMAP_TOKEN=change-me docker compose up --build -d
```

The database lives in the `roadmap-data` volume.

## Deploy on Coolify

The GitHub workflow `.github/workflows/roadmap-app-image.yml` builds
`ghcr.io/ammodev/surf-roleplay-roadmap` on every push to `master` or `main` that
touches `roadmap-app/`, and can be started by hand. Tags: `latest` and `sha-<commit>`.

1. In Coolify, create a Docker Compose resource from `docker-compose.coolify.yml`.
2. Set `ROADMAP_TOKEN` in its environment variables.
3. Assign a domain to the `roadmap` service on port 3000.
4. If the GHCR package is private, add a registry credential for `ghcr.io` in Coolify
   (a GitHub token with `read:packages`), or make the package public.

## Agent access (MCP and REST)

Agents read the roadmap and post progress through the same token, sent as
`Authorization: Bearer <ROADMAP_TOKEN>`. Every write must include `agent` and
`onBehalfOf`, and is shown as `<agent> (for <person>)`.

- **MCP:** `https://<host>/api/mcp` (streamable HTTP). For Claude Code:

  ```bash
  claude mcp add --transport http surf-roadmap https://<host>/api/mcp     --header "Authorization: Bearer <ROADMAP_TOKEN>"
  ```

- **REST:** `https://<host>/api/v1`. The routes are listed in the `surf-roadmap`
  skill (`.claude/skills/surf-roadmap/SKILL.md`), which also tells agents when to
  update.

Smoke test against a running instance (the `--write` flag posts a test update):

```bash
node scripts/mcp-smoke.mjs https://<host> <ROADMAP_TOKEN>
```

## Rotating the token

Change `ROADMAP_TOKEN` and restart. Sessions are signed with the token, so every
existing session ends and everyone signs in again with the new token. Agent MCP
configurations must be updated with the new token too.

## Backups

Stop the container, then copy `roadmap.db` from the volume, for example:

```bash
docker run --rm -v roadmap-app_roadmap-data:/data -v "$PWD":/backup busybox cp /data/roadmap.db /backup/
```
