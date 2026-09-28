# ADR-0038: The screen API is public in surf-roleplay-api

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

Every player-facing feature opens server-driven screens. Some of those features may live in
separate plugins built on the roleplay API rather than inside the roleplay Paper plugin. The
screen framework needs a server-side API to build and open screens.

## Decision

The API for building and opening server-driven screens is part of the public roleplay API:
- the screen and widget tree model and its builder DSL live in
  `surf-roleplay-api-client-common`
- opening, patching and closing screens for a Paper player lives in
  `surf-roleplay-api-client-paper`

The implementation, including the translation to protocol packets, lives in
`surf-roleplay-paper`. Protocol classes do not appear in the public API.

## Alternatives considered

### An internal API in the Paper plugin

Its advantage: it can change freely without breaking other code, and it can be moved into the
API modules later once it has settled.

It was rejected because other plugins would have no way to open screens, and moving it later
would break every caller in the Paper plugin.

## Consequences

### What this gives us

Any plugin on the roleplay API can build screens without touching the protocol.

### What this costs

- The API needs stable signatures and full documentation from the start.
- A second model layer that mirrors the protocol's widget classes, plus the mapping between
  the two.

### Follow-on work

The API interfaces and builder DSL, the Paper implementation, and the mapping to protocol
packets.

### What this forecloses

Breaking changes to the screen API now affect every plugin that uses it.
