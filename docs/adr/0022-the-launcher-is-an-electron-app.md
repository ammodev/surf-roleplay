# ADR-0022: The launcher is an Electron app

- **Status:** Accepted
- **Date:** 2026-09-26
- **Supersedes:** none
- **Superseded by:** none

## Context

The required client mod (ADR-0012) must reach players together with the right
Fabric version, whitelisted extra mods and assets. The game must also refuse to
start with mods that are not whitelisted. No launcher exists yet.

## Decision

The project ships its own launcher, built with Electron and kept in this
repository. It signs players in with their Microsoft account and syncs files from
a versioned manifest on the project's CDN. It starts the game only with mods on
the whitelist managed in the dashboard.

## Alternatives considered

### A Kotlin Compose Desktop launcher

Its advantage: the same language as the rest of the project, with a smaller
runtime than Electron.

It was rejected in favour of a UI built with web technology.

### An existing launcher with a modpack

Its advantage: no launcher development at all.

It was rejected because it cannot enforce the mod whitelist or the project's own
update flow.

## Consequences

### What this gives us

Full control over installation, updates and mod enforcement, with a web UI.

### What this costs

- A large runtime download.
- A JavaScript/TypeScript codebase next to the Kotlin one.
- Code signing.
- Auto-update infrastructure.

### Follow-on work

Microsoft authentication, the manifest format and CDN, Java runtime management,
the whitelist sync, and release signing.

### What this forecloses

Players cannot use other launchers to join.
