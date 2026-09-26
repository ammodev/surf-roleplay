# ADR-0015: Game content is defined in config or the database

- **Status:** Accepted
- **Date:** 2026-09-26
- **Supersedes:** none
- **Superseded by:** none

## Context

Licenses, ranks and qualifications were defined as sealed Kotlin types, and items
were planned the same way. The gamemode will have hundreds of items, prices,
locations, recipes, ranks and tuning values. Staff and the external dashboard need
to change them without a code release.

## Decision

All game content and tuning is defined in configuration files or the database,
whichever fits each kind of data. That covers items, licenses, ranks,
qualifications, prices, recipes, locations and numeric tuning. Code defines
behaviours, such as what a food item or a container does. Data defines the
concrete content that uses those behaviours. Content keeps stable string keys.

## Alternatives considered

### Content as Kotlin types

Its advantage: compile-time safety. An invalid reference fails the build, and a
rank can never be assigned to the wrong organisation.

It was rejected because every content change would need a developer and a
release, and the dashboard could not edit content.

### Code types with only numbers in config

Its advantage: it keeps type safety for the set of content while still allowing
balance tuning without a release.

It was rejected because adding an item, a rank or a license would still need a
release.

## Consequences

### What this gives us

Staff and the dashboard can add and change content live, and balance changes need
no deployment.

### What this costs

Validation moves from the compiler to load time, so invalid references surface
at runtime. Content also needs reload handling and schema checks.

### Follow-on work

- Content registries with validation and reload.
- A schema for each content kind.
- Migration of the existing sealed rank, qualification and license types to data.
- Editor support in the dashboard or in in-game tools.

### What this forecloses

Exhaustive `when` over ranks or licenses in code is no longer possible. Moving
content back into code would mean freezing it for staff again.
