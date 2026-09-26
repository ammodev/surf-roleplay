# ADR-0021: Vehicles use client prediction with server reconciliation

- **Status:** Accepted
- **Date:** 2026-09-26
- **Supersedes:** none
- **Superseded by:** none

## Context

Vehicles are driven through the client mod, and the server is authoritative
(ADR-0020). With 150 or more players and real-world latency, a driver who had to
wait for the server on every input would feel noticeable steering lag.

## Decision

The driver's client simulates its vehicle immediately from local input, using the
same physics as the server. The server simulates the same inputs and sends its
authoritative state, and the client corrects any drift. Other players see
interpolated vehicle states.

## Alternatives considered

### The server simulates, clients only interpolate

Its advantage: one simulation, no reconciliation logic, and no chance of a
client-side desync.

It was rejected because drivers would feel input lag equal to their round-trip
time.

## Consequences

### What this gives us

Responsive driving while server authority is kept.

### What this costs

Deterministic shared physics on both sides, plus input buffering, reconciliation
and smoothing code. Debugging also gets harder.

### Follow-on work

A shared physics module, input and state packets, reconciliation and
interpolation, and tolerance tuning.

### What this forecloses

Physics that only runs on one side. Changing the physics requires releasing the
mod and the server together.
