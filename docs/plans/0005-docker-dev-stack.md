# Plan 0005: Docker dev stack

- **Status:** In progress
- **Date:** 2026-09-28
- **Accepted proposal:** Commit the local dev stack as a Docker Compose project in tools/devenv and add a roadmap REST helper script to the surf-roadmap skill
- **Decision records:** ADR-0031

## Goal

`tools/devenv/` contains a Docker Compose project. From a clean checkout with the plugin and
microservice built, `docker compose up -d` in that folder starts the following:
- PostgreSQL, Redis and RabbitMQ
- a fetch service that downloads the pinned jars from `versions.env`
- the surf-core, surf-transaction and roleplay microservices
- Paper 26.2 with the surf plugins and the roleplay plugin
- Velocity 4.2.0 on `localhost:25565` with modern forwarding

The Fabric dev client started with `runLocalClient` behaves as follows:

| Client | Result |
|---|---|
| The dev client as built | Joins |
| A client with a different protocol version | Kicked with the update message |
| A client whose fabric-api submodule ids are removed from the dev `allowed-mods` | Kicked with the forbidden-mods message |

Runtime data and jars stay out of git. `tools/devenv/README.md` explains how to use the stack.
`.claude/skills/surf-roadmap/scripts/roadmap.sh` performs authenticated REST calls without
printing the token, and the skill documents it.

## Out of scope

- An automated join smoke test.
- Running Canvas instead of Paper.
- Debugger attach configurations.
- Running the stack in CI.
- Changes to plugin, mod or microservice code.

## Steps

### Step 1: Infrastructure and jar fetching

**Does:** Adds the following:
- `tools/devenv/compose.yml` with postgres, redis and rabbitmq
- `versions.env` with every pinned version
- a one-shot `fetch` service and its script, which download every server and plugin jar into
  `tools/devenv/jars/` and skip jars already present
- the `.gitignore` entries for `tools/devenv/run/` and `tools/devenv/jars/`

**Ends in:** The infrastructure containers are running, and every pinned jar is present in
`tools/devenv/jars/`.

**Verified by:** `docker compose up fetch` exits 0. Then `docker compose ps` shows the three
infrastructure services running, and listing `jars/` shows every jar named in `versions.env`.

**Pushes:** no

### Step 2: Microservices

**Does:** Adds the surf-core, surf-transaction and roleplay microservice services on
eclipse-temurin:25-jre, configured through `SURF_*` environment variables with service-name
hosts. The roleplay jar is mounted from `surf-roleplay-microservice/build/libs`.

**Ends in:** All three microservices report a successful start.

**Verified by:** `docker compose logs` for each microservice shows "Microservice started
successfully".

**Pushes:** no

### Step 3: Paper and Velocity

**Does:** Adds the `paper` and `velocity` services, their entrypoint scripts and the
configuration templates:
- Paper: server properties, forwarding config and `eula.txt`
- surf-core `config.yml`
- the roleplay plugin `config.yml` with the dev `allowed-mods`
- `velocity.toml` and the dev forwarding secret

**Ends in:** Paper logs `Done` together with the registered roleplay channel. Velocity logs
`Done` and listens on 25565.

**Verified by:** `docker compose logs paper velocity` shows both lines. A TCP connection to
`localhost:25565` succeeds.

**Pushes:** no

### Step 4: Roadmap helper script

**Does:** Adds `.claude/skills/surf-roadmap/scripts/roadmap.sh`, which reads `ROADMAP_TOKEN`
and `ROADMAP_URL` from the environment, falling back to `roadmap-app/.env`. It documents the
script in the skill's REST section.

**Ends in:** The script returns the phases from the hosted roadmap, and the token appears
neither in its output nor in any committed file.

**Verified by:** `roadmap.sh GET /phases` prints the phases JSON, and a search of the repository
for the token value finds nothing.

**Pushes:** no

### Step 5: README

**Does:** Writes `tools/devenv/README.md`: prerequisites, start, stop, reset, logs, reloading
after a rebuild, joining with `runLocalClient`, what the dev `allowed-mods` holds, and the
known pitfalls.

**Ends in:** The README describes every command the stack supports, and each command works as
written.

**Verified by:** Following the README's commands during step 6.

**Pushes:** no

### Step 6: Verification

**Does:** Checks the goal from a clean state.

**Ends in:** Every statement in the goal has been observed.

**Verified by:**
- `docker compose down -v` and deleting `run/`.
- `./gradlew build`, then `docker compose up -d`.
- With the dev client:
  - a join is accepted
  - a join with `PROTOCOL_VERSION` temporarily set to 2 is kicked with the update message
  - after removing the fabric-api ids from `run/paper/plugins/surf-roleplay-paper/config.yml`
    and restarting Paper, a join is kicked with the forbidden-mods message
- `git status` shows no files under `run/` or `jars/`.

**Pushes:** no

## Push points

none

## Risk

Step 3 is the most likely to go wrong. Paper and Velocity have to come up in containers with
forwarding and plugins configured before their first start, and Paper only fills in a partial
`paper-global.yml` if it merges missing keys as expected. If Paper or Velocity cannot be
configured through templates, the agent stops and asks the human before changing the approach.

## If reality contradicts this plan

The agent stops and asks. It does not silently rewrite this plan to match what actually
happened, and it does not continue past a step whose stated end state was not reached.
