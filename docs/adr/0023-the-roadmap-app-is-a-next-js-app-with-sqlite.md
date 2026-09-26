# ADR-0023: The roadmap app is a Next.js app with SQLite

- **Status:** Accepted
- **Date:** 2026-09-26
- **Supersedes:** none
- **Superseded by:** none

## Context

The gamemode has well over a hundred systems across mod, server, launcher and
content. Developers, builders and staff need one place to see each system's
specification, phase, owner and status, and to update it. Most of these people
have no claude.ai accounts, and the data should stay on the team's own
infrastructure.

## Decision

The roadmap and tracker is a self-hosted web app in `roadmap-app/` in this
repository. It is built with Next.js 15 (App Router), Tailwind CSS and Drizzle ORM
on a single SQLite file (better-sqlite3), and it ships as a Docker image that
keeps the database on a volume.

## Alternatives considered

### A hosted claude.ai artifact with a shared database

Its advantage: no hosting, no build, and it can be published immediately.

It was rejected because every viewer would need a claude.ai account in the
organisation, and the data would live outside the team's infrastructure.

### A JSON file instead of SQLite

Its advantage: the simplest possible storage, readable and editable by hand.

It was rejected because concurrent edits from several people would overwrite each
other.

### Prisma instead of Drizzle

Its advantage: a widely known ORM with generated clients and a migration tool.

It was rejected because it needs a separate engine binary and a generation step,
which adds weight to the Docker image. Drizzle works directly with better-sqlite3.

## Consequences

### What this gives us

A tool the whole team can use with nothing more than a token, with the data on
their own server.

### What this costs

A Node.js/TypeScript codebase in the repository, a native module (better-sqlite3)
that must be built for the Docker image's platform, and a server to host it.

### Follow-on work

The app itself, its seed data, its Docker image, and backups of the SQLite volume.

### What this forecloses

Running several app instances against one database. SQLite supports a single
writer process, so scaling beyond one container would need a database change.
