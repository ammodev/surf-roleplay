# ADR-0026: Agents authenticate with the shared login token

- **Status:** Accepted
- **Date:** 2026-09-27
- **Supersedes:** none
- **Superseded by:** none

## Context

The MCP endpoint and REST API (ADR-0025) must not be public. People already share
one login token for the web UI (ADR-0024), and each agent acts for one of those
people.

## Decision

Agents authenticate with the same `ROADMAP_TOKEN`, sent as
`Authorization: Bearer <token>` on every `/api` request. Every write must name the
agent and the person it acts for. The app records the author as
`<agent> (for <person>)` and marks it as an agent change.

## Alternatives considered

### A separate API token

Its advantage: the human login and agent access could be rotated independently,
and a leaked agent config would not grant UI access.

It was rejected to keep a single secret to manage.

### Per-agent tokens managed in the app

Its advantage: individual revocation, and authorship that is verified rather than
self-declared.

It was rejected as out of proportion for a small team, the same reasoning as in
ADR-0024.

## Consequences

### What this gives us

No new secret, and agents can be set up with the token people already have.

### What this costs

A token copied into an agent's configuration file grants full UI access too.
Rotating the token breaks every agent configuration at once, and agent
attribution is self-declared.

### Follow-on work

Bearer validation in the middleware and API routes, attribution fields on every
write, and documentation on where the token is stored.

### What this forecloses

Revoking a single agent without rotating the token for everyone.
