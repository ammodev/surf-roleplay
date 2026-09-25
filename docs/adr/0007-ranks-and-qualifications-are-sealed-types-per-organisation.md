# ADR-0007: Ranks and qualifications are sealed types per organisation

- **Status:** Accepted
- **Date:** 2026-09-25
- **Supersedes:** none
- **Superseded by:** none

## Context

Police and SAR identities carry a rank and a set of qualifications. The two
organisations have different ranks and different qualifications: a SAR member can
hold a rescue qualification, a police officer a special forces qualification, and
neither may hold the other's. There were generic rank and qualification types,
but no concrete ranks, no registry, and no way to tell which organisation an
entry belongs to.

## Decision

Ranks and qualifications are defined in code as sealed class hierarchies per
organisation: `PoliceRank`, `SarRank`, `PoliceQualification`, and
`SarQualification`. Each entry is an object with a key, a display name and, for
ranks, an ordering level. Each hierarchy exposes its entries and a lookup by key.
Storage holds only the keys. A police identity's rank is a `PoliceRank`, and its
qualifications are `PoliceQualification`s. SAR works the same way.

## Alternatives considered

### Generic types with an organisation tag

Its advantage: one rank type and one qualification type, and a new organisation
needs no new types.

It was rejected because assigning a SAR qualification to a police identity would
only fail at runtime instead of at compile time.

### Defined in a microservice config file

Its advantage: ranks and qualifications change without a rebuild.

It was rejected because it needs a sync RPC and loses the compile-time typing.

### Managed in the database

Its advantage: fully runtime-editable.

It was rejected because it needs admin tooling to be usable at all.

## Consequences

### What this gives us

Type-safe assignment, with the full set of entries known at compile time.

### What this costs

Adding or renaming a rank or qualification needs a code change and a release.
Two parallel hierarchies must be kept consistent.

### Follow-on work

The concrete German police and SAR entries, lookup by key when loading stored
rows, and handling of stored keys that no longer exist.

### What this forecloses

Runtime editing by staff. Moving to data-driven definitions later requires
replacing the sealed types throughout the API.
