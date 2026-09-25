# ADR-0011: The license registry lives in core-client-common

- **Status:** Accepted
- **Date:** 2026-09-25
- **Supersedes:** none
- **Superseded by:** none

## Context

Service implementations in this project are registered with `@AutoService`,
processed by surf-api's KSP processor. In `surf-roleplay-core-common` that
processor fails the build as soon as the module contains an `@AutoService`
class: its `finish` step reads the name of a source file that KSP has already
invalidated ("PSI has changed since creation"). The same processor works in
`surf-roleplay-core-client-common`. The license registry implementation needs a
home where it can be registered.

## Decision

The `LicenseRegistry` implementation lives in `surf-roleplay-core-client-common`
and is registered there with `@AutoService`. `surf-roleplay-core-common`
contains no `@AutoService` classes.

## Alternatives considered

### Hand-written service file in core-common

Its advantage: the registry stays available to every runtime, including the
microservice.

It was rejected because it would be the only service registered by hand, and
the file would have to be kept in sync manually.

### Fix the processor upstream

Its advantage: removes the root cause, and keeps `@AutoService` usable in
core-common.

It was rejected for now because the work here would be blocked until a new
surf-api release is published.

## Consequences

### What this gives us

A working build with the project's usual registration mechanism.

### What this costs

The microservice has no license registry, so it cannot turn stored license keys
into `License` objects. It only stores and returns keys.

### Follow-on work

Keep `@AutoService` out of core-common until the processor is fixed upstream.

### What this forecloses

License resolution in the microservice, such as server-side requirement checks.
Adding it later needs either the upstream processor fix or a hand-written
service file.
