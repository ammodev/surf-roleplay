# ADR-0020: The server is authoritative

- **Status:** Accepted
- **Date:** 2026-09-26
- **Supersedes:** none
- **Superseded by:** none

## Context

With a required client mod (ADR-0012), much of the game runs in client code that
players could modify. Money, items, positions, injuries and police tracking are
all worth cheating on.

## Decision

The server is the authority for all game state. The mod sends inputs and requests.
The server validates them and sends back the resulting state. No client message
directly changes money, items, health or ownership.

## Alternatives considered

### Trust the client for movement

Its advantage: simpler vehicle and player movement code, with no correction logic.

It was rejected because teleport and speed cheats would become trivial.

## Consequences

### What this gives us

A modified client can at most request actions that the server would allow anyway.

### What this costs

More server work per action, and latency on every interaction that waits for the
server.

### Follow-on work

Validation for every request packet, and prediction where latency would hurt
(see ADR-0021 for vehicles).

### What this forecloses

Client-side shortcuts for game state.
