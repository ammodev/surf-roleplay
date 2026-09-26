# ADR-0019: An own zone system replaces WorldGuard

- **Status:** Accepted
- **Date:** 2026-09-26
- **Supersedes:** none
- **Superseded by:** none

## Context

Many features need areas with behaviour: fields, mines, territories, speed limits,
safe zones, radio dead zones, protected reserves, apartment boundaries and
robbery areas. The server runs a Folia-based fork with 150 or more players. The
map is not built yet, so staff must be able to place every location in-game.

## Decision

The gamemode uses its own zone system. Zones are polygons with a minimum and
maximum height, carry typed flags, and are looked up in a way that is aware of
region threads. Staff place and edit zones in an in-game editor in the client mod,
and zones are stored in the database. WorldGuard is not used.

## Alternatives considered

### WorldGuard regions with custom flags

Its advantage: it is mature, staff already know it, and it has protection
features built in.

It was rejected because its region model and editing workflow do not match the
in-game editor and database storage, and its behaviour on Folia-based forks is a
risk.

## Consequences

### What this gives us

Zones as first-class gameplay data, editable live, with flags tailored to the
gamemode.

### What this costs

Protection, lookup and editing must all be built and tested in-house.

### Follow-on work

The zone model, a spatial index, a flag registry, the in-game editor, and build
protection for zones that need it.

### What this forecloses

Plugins built on WorldGuard cannot rely on these zones.
