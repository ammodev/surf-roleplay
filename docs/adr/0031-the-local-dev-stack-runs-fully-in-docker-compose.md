# ADR-0031: The local dev stack runs fully in Docker Compose

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

Testing the roleplay plugin and client mod live needs a whole stack:
- PostgreSQL, Redis and RabbitMQ
- the surf-core, surf-transaction and roleplay microservices
- a Paper server with the surf plugins, behind a Velocity proxy with modern forwarding

surf-core cannot accept joins without the proxy, and its database code does not run on
MariaDB. Setting this up by hand took a large part of the first protocol work, and the result
lived only outside the repository. Developers and agents work on Windows as well as Unix.

## Decision

The repository ships the local dev stack in `tools/devenv/` as one Docker Compose project that
runs every part in containers:
- the infrastructure
- a one-shot fetch service that downloads pinned plugin and server jars
- the three microservices
- Paper and Velocity

The roleplay plugin and microservice jars are mounted from the Gradle build output. Versions
are pinned in `tools/devenv/versions.env`. Runtime data and downloaded jars are not committed.
The dev Paper server ships with the Minecraft EULA accepted, for every developer who uses the
stack.

## Alternatives considered

### Infrastructure in Docker, Java services on the host

Its advantage: the Java processes run natively, which makes attaching a debugger and reading
logs straightforward.

It was rejected because the host scripts depend on the shell and the operating system (process
handling, socket path limits and path conversion in Git Bash), so they behave differently on
Windows and Unix.

### Gradle tasks that start the stack

Its advantage: one entry point through the build tool everyone already runs, and it works on
every platform through the JVM.

It was rejected because it mixes build and runtime concerns, and Gradle is awkward at
supervising long-running server processes.

## Consequences

### What this gives us

One command starts a complete, reproducible stack on any machine with Docker, and the setup
knowledge lives in the repository.

### What this costs

- Docker is required for live testing.
- Debugging the server needs a remote debug port instead of a local process.
- The pinned versions must be bumped by hand when the surf plugins or Minecraft update.
- Everyone using the stack accepts the Minecraft EULA through the committed setting.

### Follow-on work

Keep `versions.env` in step with the versions the Gradle build compiles against, and extend
the stack when new services or plugins join the gamemode.

### What this forecloses

Nothing permanent: host-based setups remain possible, but they are not maintained.
