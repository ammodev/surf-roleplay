# ADR-0006: The active identity is not persisted

- **Status:** Accepted
- **Date:** 2026-09-25
- **Supersedes:** none
- **Superseded by:** none

## Context

A user acts through one active identity at a time, and money operations fail
while none is active. Whether that choice survives a rejoin decides whether it
must be stored, and what state a player is in right after login.

## Decision

The active identity exists only in the memory of the server the player is on.
After every login no identity is active until one is set.

## Alternatives considered

### Persist the active identity

Its advantage: players continue as the persona they left with, without choosing
again.

It was rejected because a player must consciously choose a persona each session,
and persisting it would require an extra column and a write RPC on every switch.

### Activate the civilian identity on login

Its advantage: players who have a civilian identity can act immediately.

It was rejected for the same reason: the persona choice is meant to be deliberate
each session.

## Consequences

### What this gives us

No storage or RPC for switching identities. Switching is instant and local.

### What this costs

Right after login, every identity-dependent operation fails until an identity is
set. Callers must handle "no active identity".

### Follow-on work

A later identity selection flow (command or GUI) so players can pick a persona.

### What this forecloses

Resuming a persona automatically. Adding it later needs a column, an RPC, and a
decision about the login state.
