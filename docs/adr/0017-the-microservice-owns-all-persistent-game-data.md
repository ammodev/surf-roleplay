# ADR-0017: The microservice owns all persistent game data

- **Status:** Accepted
- **Date:** 2026-09-26
- **Supersedes:** none
- **Superseded by:** none

## Context

The roleplay microservice persisted only users, identities, ranks, qualifications
and licenses. The gamemode adds items, inventories, vehicles, houses, gangs,
businesses, phones, records and economy state, and each of these needs a home.

## Decision

The roleplay microservice stores all persistent game data. Paper servers load what
an online player or an active world object needs, and write through microservice
RPCs.

## Alternatives considered

### Paper writes world-bound data to its own database

Its advantage: vehicles, houses and zones would avoid an RPC hop, and the Paper
plugin would own the data it uses most.

It was rejected because it would split the data model across two writers and two
schemas, and the dashboard would have to know which system owns what.

## Consequences

### What this gives us

One schema and one place for data access rules, usable by every server and tool.

### What this costs

Every write is an RPC, so latency-sensitive paths need batching and caching. The
microservice also grows considerably.

### Follow-on work

Tables, DTOs and RPCs for every new domain, plus caching rules on Paper.

### What this forecloses

Paper-only persistence shortcuts. Moving data out of the microservice later means
migrating it and re-pointing every client.
