# ADR-0009: License grants enforce requirements unless forced

- **Status:** Accepted
- **Date:** 2026-09-25
- **Supersedes:** none
- **Superseded by:** none

## Context

Licenses declare requirements; for example, a truck license requires a car
license. Granting a license can either check those requirements or leave the
check to the caller. Staff occasionally need to grant a license regardless of
requirements, for example to repair data.

## Decision

Granting a license checks its requirements against the identity. If they are not
met, the grant fails with a result that carries the per-requirement breakdown.
A `force` parameter skips the check.

## Alternatives considered

### Always enforce

Its advantage: no way to create a license state that violates the rules.

It was rejected because staff have no way to repair or seed data.

### Never enforce

Its advantage: a simpler grant operation, with callers free to apply their own
rules.

It was rejected because every caller would have to remember the check, and
forgetting it silently breaks the rules.

## Consequences

### What this gives us

Rules hold by default, and staff keep a deliberate escape hatch.

### What this costs

A result type with several outcomes instead of a plain success. Forced grants can
still create rule-violating states.

### Follow-on work

A grant result type and unit tests for both the enforced and the forced paths.

### What this forecloses

Nothing significant. Removing the flag later is a small API change.
