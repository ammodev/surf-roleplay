# Plan 0002: Roadmap app

- **Status:** In progress
- **Date:** 2026-09-26
- **Accepted proposal:** Build a self-hosted roadmap and tracker app for the surf-roleplay gamemode, seeded with the full specification from the 2026-09-26 question rounds
- **Decision records:** ADR-0023, ADR-0024

## Goal

`docker compose up --build` in `roadmap-app/` starts the app on port 3000 in
Docker Desktop. A visitor without a session is redirected to `/login`. Entering the
`ROADMAP_TOKEN` and a display name opens the app, which shows:

- every seeded domain, system, task, phase, decision and open question from the
  gamemode specification
- a system catalogue with full spec text
- a phase roadmap with dependencies
- a kanban board where dragging a system changes its status
- a decisions and open questions view
- a people list whose entries can be assigned as owners

Status, owner, priority, notes and tasks are editable. Every change is written to a
change log with the display name and a timestamp, and it survives a container
restart because the SQLite file lives on a named volume. The UI is English and
follows the system light or dark theme. `npm run lint`, `npm run typecheck`,
`npm test` and `npm run build` pass, and every function, component, type and
exported constant has a TSDoc comment.

## Out of scope

- Individual user accounts, roles or per-person permissions (ADR-0024).
- Any code for the gamemode itself: mod, server plugin, microservice, launcher.
- Deploying to a remote server, TLS, a domain name or a reverse proxy.
- Backups of the SQLite volume.
- Integration with the external roleplay dashboard or surf services.
- Real-time push between open browsers; other viewers see changes on reload or
  navigation.
- Localisation; the UI is English only.
- A CI workflow for the app.

## Steps

Each step ends in one or more commits with a Conventional Commits message and no
attribution line. Commits name their paths explicitly.

### Step 1 — Scaffold the Next.js app

**Does:** Creates `roadmap-app/` with Next.js 15 (App Router, TypeScript, strict
mode), Tailwind CSS, ESLint, Vitest, and `output: "standalone"` in the Next config.
Adds scripts `dev`, `build`, `start`, `lint`, `typecheck`, `test`. Adds
`roadmap-app/.gitignore` for `node_modules`, `.next` and `data/`. Adds a minimal
page with the app name.

**Ends in:** A buildable, empty app in `roadmap-app/`.

**Verified by:** `npm run lint && npm run typecheck && npm run build` in
`roadmap-app/` exits 0.

**Pushes:** no

### Step 2 — Define the database schema

**Does:** Adds Drizzle ORM with better-sqlite3 and the schema:

- `people` (name, role: dev/builder/staff)
- `domains`
- `phases`, `phase_dependencies`
- `systems`: domain, phase, title, summary, spec (markdown), status, priority,
  owner, notes
- `tasks`: system, title, status, priority, owner
- `decisions`: title, text, ADR reference, date
- `open_questions`: title, text, resolved flag, system reference
- `change_log`: entity, entity id, field, old value, new value, author, timestamp

Statuses are Not started, Design, In progress, Review, Done and Blocked.
Priorities are MVP, Later and Nice to have. Adds a database module that opens
`DATABASE_PATH` (default `./data/roadmap.db`), enables WAL, and creates the schema
on first open.

**Ends in:** Opening the database creates every table.

**Verified by:** a Vitest test that opens a temporary database file and asserts
that all tables exist; `npm test` passes.

**Pushes:** no

### Step 3 — Write the seed and import it on first start

**Does:** Adds `roadmap-app/seed/roadmap.ts`, a typed, versioned seed containing
every domain, system (with spec text), task, phase with dependencies, decision and
open question agreed in the 2026-09-26 question rounds, organised by the phases
P0 to P6 of the accepted proposal. Adds an importer that runs when the
database has no systems, inside one transaction, and records a `seed` entry in
the change log.

**Ends in:** A fresh database contains the full seed. A second start does not
duplicate it.

**Verified by:** Vitest tests that import into a temporary database, assert the
row counts match the seed, run the import again, and assert the counts are
unchanged; `npm test` passes.

**Pushes:** no

### Step 4 — Add token login and the session guard

**Does:**

- Adds `/login`, which asks for the token and a display name.
- Adds a server action that compares the token with `ROADMAP_TOKEN` using a
  constant-time comparison. On success it sets an httpOnly, SameSite=Lax cookie
  holding an HMAC of a session id signed with the token.
- Adds middleware that redirects every request without a valid cookie to
  `/login`, except `/login` itself and static assets.
- Stores the display name in local storage and sends it with every mutation.
- Adds a logout action.
- Refuses to start when `ROADMAP_TOKEN` is unset.

**Ends in:** Only visitors who entered the correct token reach the app.

**Verified by:** Vitest tests for the token comparison and cookie signing.
Against `npm run start` with `ROADMAP_TOKEN=test`:

- `curl -I http://localhost:3000/` returns a redirect to `/login`.
- A wrong token shows an error.
- The right token sets the cookie, and a follow-up request returns 200.

**Pushes:** no

### Step 5 — Build the system catalogue and system detail

**Does:** Adds the app shell (navigation, light and dark theme through
`prefers-color-scheme`). Adds the catalogue grouped by domain with status,
priority and owner chips, and filters by phase, status, priority and owner. Adds a
system detail page with the rendered spec, editable status, priority, owner (from
the people list) and notes, a task list with add, edit, reorder-free status
changes and delete, and the system's change history. Every mutation is a server
action that writes the change log with the display name.

**Ends in:** Systems and tasks can be browsed and edited, and each edit appears in
the change history.

**Verified by:** manual check against `npm run start`: change a system's status and
a task's owner, reload, see both persisted and listed in the history with the
display name.

**Pushes:** no

### Step 6 — Build the kanban board

**Does:** Adds a board with one column per status that shows systems as cards,
filterable by phase and domain. Dragging a card to another column, using native
HTML drag and drop with a keyboard-accessible status menu as a fallback, updates
the status through the same server action.

**Ends in:** Moving a card changes the system's status.

**Verified by:** manual check: drag a card from Not started to Design, reload, the
card stays in Design and the change log shows the move.

**Pushes:** no

### Step 7 — Build the phase roadmap

**Does:** Adds a roadmap view listing phases P0 to P6 in order, each with its
goal, its systems, a progress bar computed from system statuses, and the phases it
depends on.

**Ends in:** The roadmap shows every phase with correct progress.

**Verified by:** manual check: mark one system in P0 as Done, and P0's progress
increases accordingly.

**Pushes:** no

### Step 8 — Build decisions, open questions and people

**Does:** Adds a decisions view (ADR reference, date, text), an open questions view
with a resolve toggle and a link to the related system, and a people view to add,
rename, change the role of and remove people. Removing a person clears their
ownership after a confirmation step on the page.

**Ends in:** Decisions are listed, questions can be resolved, and people can be
managed and assigned.

**Verified by:** manual check: add a person, assign them to a system, resolve an
open question, reload, all persisted.

**Pushes:** no

### Step 9 — Package the app for Docker

**Does:** Adds a multi-stage `Dockerfile` (dependency install with build tools
for better-sqlite3, Next build, slim runtime running the standalone server as a
non-root user) and `docker-compose.yml` exposing port 3000, passing
`ROADMAP_TOKEN` from the environment or an `.env` file, and mounting a named
volume at the database directory. Adds `roadmap-app/README.md` with run, token
rotation and backup notes.

**Ends in:** The app runs from the image with a persistent database.

**Verified by:** `docker compose up --build -d` in `roadmap-app/` succeeds and
`curl -I http://localhost:3000/` returns a redirect to `/login`.

**Pushes:** no

### Step 10 — Verification

**Does:** Runs the full check of the goal against the Docker container and the
source.

**Ends in:** Every statement in the goal is confirmed.

**Verified by:**

1. `npm run lint && npm run typecheck && npm test && npm run build` exit 0.
2. `docker compose up --build -d`. Without a cookie, `/` redirects to `/login`.
   Logging in with the token and a name shows the catalogue with all seeded
   domains.
3. Change a system's status on the kanban and a task's owner on the detail page.
   Run `docker compose restart` and confirm both persisted, with the change log
   naming the display name.
4. Check the roadmap, decisions, open questions and people views render seeded
   data.
5. Check the page in light and dark system themes.
6. Search the source for exported or declared functions and components without a
   TSDoc comment and find none.
7. `docker compose down` (keeping the volume).

**Pushes:** no

## Push points

none

## Risk

Step 9 is most likely to go wrong. better-sqlite3 is a native module that must be
compiled for the image's platform, and Next's standalone output does not always
trace native modules into the runtime image. If the image fails to build or the
container cannot load the module, the agent inspects the build output and fixes
the Dockerfile within the chosen stack, for example by copying the module
explicitly or aligning base images. If a fix would require changing the database
library or dropping SQLite, the agent stops and asks, because that contradicts
ADR-0023.

## If reality contradicts this plan

The agent stops and asks. It does not silently rewrite this plan to match what
actually happened, and it does not continue past a step whose stated end state was
not reached.
