# Plan 0003: Roadmap agent access

- **Status:** In progress
- **Date:** 2026-09-27
- **Accepted proposal:** Add a REST API, a remote MCP endpoint and a skill so agents can read the hosted roadmap and post their progress
- **Decision records:** ADR-0025, ADR-0026

## Goal

The roadmap app has a `progress_updates` table. Each entry holds a summary, an
optional next step, an optional task, an optional commit hash linked to
`REPO_URL`, and an agent author. Updates appear:

- as a timeline on each system page
- on a new Updates page
- as the latest update on each catalogue card

Requests with `Authorization: Bearer <ROADMAP_TOKEN>` can use:

- **`/api/v1`**: a JSON REST API to read systems, phases, decisions, questions,
  people and updates, and to update systems, add and update tasks, post updates
  and add open questions.
- **`/api/mcp`**: a stateless streamable-HTTP MCP endpoint exposing the same
  operations as tools.

Every write requires `agent` and `onBehalfOf`, is attributed as
`<agent> (for <person>)`, and lands in the change log. Requests without a valid
token get 401.

The skill `.claude/skills/surf-roadmap/SKILL.md` tells agents how to connect and
when to update. The README documents the setup. The GHCR image is rebuilt from
`master`. Lint, typecheck, tests and build pass, and every declaration has a TSDoc
comment.

## Out of scope

- Separate or per-agent API tokens (ADR-0026).
- A local stdio MCP server or npm package (ADR-0025).
- Real-time push to open browsers.
- Deleting systems, tasks, people or updates through the API.
- Rate limiting.
- Deploying to Coolify; the image is published, deployment stays manual.

## Steps

Each step ends in a commit with a Conventional Commits message and no attribution
line.

### Step 1 — Store progress updates

**Does:** Adds the `progress_updates` table (id, system id, optional task id,
summary, optional next step, optional commit hash, author, agent flag,
timestamp). Adds a `postUpdate` mutation that validates the input: a non-empty
summary, a known system, a task belonging to that system, and a commit hash of
7 to 40 hex characters. It inserts the entry and writes a change-log entry. Adds
an `addQuestion` mutation. Adds an `agentAuthor(agent, onBehalfOf)` helper that
formats `<agent> (for <person>)` and rejects empty names.

**Ends in:** Updates and questions can be written with validation and logging.

**Verified by:** Vitest tests for each validation rule, for logging, and for the
author format; `npm test` passes.

**Pushes:** no

### Step 2 — Add the bearer guard and REST API

**Does:**

- Adds `requireBearer(request)` and exempts `/api/*` from the cookie redirect in
  the middleware. API routes check the bearer themselves and answer 401 JSON.
- Adds REST routes under `/api/v1`:
  - `GET systems` (filters), `GET systems/:id` (with tasks and updates), `GET phases`
  - `GET decisions`, `GET questions`, `GET people`, `GET updates`
  - `PATCH systems/:id`, `POST systems/:id/tasks`, `PATCH tasks/:id`
  - `POST systems/:id/updates`, `POST questions`
- Write bodies require `agent` and `onBehalfOf`. Errors return 400 with a
  message; unknown ids return 404.

**Ends in:** The REST API works with the token and refuses requests without it.

**Verified by:** Vitest tests for `requireBearer`. Against `npm run start`:

- `GET /api/v1/systems` without a token returns 401, and with a token returns 200
  with 60 systems.
- `PATCH` a system and `POST` an update, then `GET` the system and see both.
- A write without `agent` returns 400.

**Pushes:** no

### Step 3 — Serve the MCP endpoint

**Does:** Adds `@modelcontextprotocol/sdk` and a route handler at `/api/mcp`
that builds a stateless MCP server per request with the web-standard
streamable-HTTP transport. It checks the bearer and registers these tools, each
calling the same query or mutation functions as the REST API:

- `list_systems`, `get_system`, `list_phases`, `list_updates`
- `update_system`, `add_task`, `update_task`, `post_update`, `add_question`

**Ends in:** An MCP client with the token can list and call the tools.

**Verified by:** A script using the SDK's streamable-HTTP client against
`npm run start`: it lists the nine tools, calls `get_system`, calls `post_update`,
and the update appears in `GET /api/v1/updates`. Without the token, the client
connection fails with 401.

**Pushes:** no

### Step 4 — Show updates in the UI

**Does:**

- Adds an updates timeline to the system page: summary, next step, task, commit
  link, author, and an agent badge.
- Adds an Updates page with the global feed, linked in the navigation.
- Shows the latest update's summary and age on each catalogue card.

**Ends in:** Posted updates are visible in all three places.

**Verified by:** Post an update through the API, then fetch the system page, the
Updates page and the catalogue and find the summary and the commit link in each.

**Pushes:** no

### Step 5 — Write the skill and the setup documentation

**Does:** Adds `.claude/skills/surf-roadmap/SKILL.md`. It covers:

- connecting with `claude mcp add --transport http`, plus a curl fallback
- finding the matching system and task
- the update rules: In progress on start; a progress update with commit hash and
  summary per commit, even before pushing; tasks done and the system to Review or
  Done on finish; Blocked plus an open question on blockers
- always sending `agent` and `onBehalfOf`

Updates `roadmap-app/README.md` with the API and MCP setup and `REPO_URL`.

**Ends in:** An agent reading the skill knows how and when to update the roadmap.

**Verified by:** Every tool name and REST path in the skill matches the
implemented ones, checked by grepping the route and tool definitions.

**Pushes:** no

### Step 6 — Verification

**Does:** Checks the whole goal against the Docker image and publishes it.

**Ends in:** The goal is confirmed, and the new image is on GHCR.

**Verified by:**

1. `npm run lint && npm run typecheck && npm test && npm run build` exit 0.
2. `docker compose up --build -d`; the REST checks from Step 2 and the MCP script
   from Step 3 pass against the container.
3. Updates show on the system page, the Updates page and the catalogue.
4. The TSDoc scan finds no undocumented declarations.
5. `git push origin master`, and the "Roadmap app image" workflow succeeds.

**Pushes:** yes — the user asked for the GHCR image to be rebuilt, and the image
build only runs in CI.

## Push points

- Step 6 pushes `master` because the GHCR image is built only by the GitHub
  workflow, and the user asked for the rebuilt image.

## Risk

Step 3 is most likely to go wrong. The MCP SDK's web-standard transport and its
behaviour inside a Next.js route handler (streaming responses, stateless mode) are
the least known pieces. If the transport does not work in a route handler, the
agent tries the SDK's Node transport through a Node request adapter within the
same SDK. If neither works, it stops and asks, because a different approach
would contradict ADR-0025.

## If reality contradicts this plan

The agent stops and asks. It does not silently rewrite this plan to match what
actually happened, and it does not continue past a step whose stated end state was
not reached.
