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

## Rotating the token

Change `ROADMAP_TOKEN` and restart. Sessions are signed with the token, so every
existing session ends and everyone signs in again with the new token.

## Backups

Stop the container, then copy `roadmap.db` from the volume, for example:

```bash
docker run --rm -v roadmap-app_roadmap-data:/data -v "$PWD":/backup busybox cp /data/roadmap.db /backup/
```
