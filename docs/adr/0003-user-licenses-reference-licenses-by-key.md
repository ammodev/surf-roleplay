# ADR-0003: User licenses reference licenses by key

- **Status:** Accepted
- **Date:** 2026-09-25
- **Supersedes:** none
- **Superseded by:** none

## Context

License definitions (`CarLicense`, `TruckLicense`, ...) are code objects that
carry an Adventure `Key`. A license held by an identity must be stored and sent
over RPC, so it needs a stable reference to its definition. The held-license
type referenced definitions by a UUID that no definition had, and the registry
offered lookups by UUID and by name, neither of which matched the definitions.

## Decision

A held license references its definition by the definition's `Key`, stored and
transmitted as the string form (for example `roleplay:car_license`).
`LicenseRegistry` looks definitions up by `Key`.

## Alternatives considered

### Add a UUID to every license definition

Its advantage: fixed-width identifiers, and the lookup that already existed
could have stayed.

It was rejected because every definition would carry a second, opaque identifier
next to the key it already has, and stored rows would be unreadable without the
code.

## Consequences

### What this gives us

One identifier per definition. Stored rows are readable, and the key namespace
lines up with the rest of the Adventure-based API.

### What this costs

Stored keys are longer than UUIDs. Keys must never be renamed once released.

### Follow-on work

Change the held-license type, the registry interface, and the license table to
use keys.

### What this forecloses

Renaming a license key freely. A rename now requires a data migration of every
stored row that references it.
