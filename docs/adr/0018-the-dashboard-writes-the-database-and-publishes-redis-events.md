# ADR-0018: The dashboard writes the database and publishes Redis events

- **Status:** Accepted
- **Date:** 2026-09-26
- **Supersedes:** none
- **Superseded by:** none

## Context

An external roleplay dashboard will manage organisation members, content and staff
actions while players are online. Paper servers cache player data while a player
is online, and those caches must reflect dashboard changes without a rejoin.

## Decision

The dashboard writes to the roleplay database directly. After each write it
publishes a change event on Redis that names what changed. Paper servers subscribe
to these events and reload the affected data from the microservice.

## Alternatives considered

### The dashboard writes through the microservice

Its advantage: the microservice stays the only writer, so validation and
invariants live in one place, and it can publish the events itself.

It was rejected so that the dashboard, which is developed separately, can work
against the database without waiting for microservice RPCs.

## Consequences

### What this gives us

Live updates from the dashboard, with no dependency on microservice endpoints.

### What this costs

There are two writers to one schema. Invariants enforced in the microservice must
be enforced by the dashboard too, and schema changes must be coordinated with it.
A missed event leaves a stale cache until the player rejoins.

### Follow-on work

A Redis event schema, subscribers on Paper, and shared documentation of the
database schema and invariants for the dashboard.

### What this forecloses

The microservice cannot assume it has seen every write, and any caching inside the
microservice needs invalidation from the same events.
