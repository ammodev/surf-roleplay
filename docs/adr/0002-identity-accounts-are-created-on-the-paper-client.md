# ADR-0002: Identity accounts are created on the Paper client

- **Status:** Accepted
- **Date:** 2026-09-25
- **Supersedes:** none
- **Superseded by:** none

## Context

Each identity owns a transaction account (ADR-0001). The surf-transaction API is
available at runtime only where the surf-transaction plugin is installed, which
is the Paper servers; the roleplay microservice has no surf-transaction runtime.
Account names are globally unique, 3 to 32 characters long, and may not be a
UUID. Creating an account and persisting an identity therefore happen in two
different systems, and the step between them can fail.

## Decision

The Paper client creates the account before sending the create-identity RPC, and
passes the resulting account id to the microservice. The account is named
`rp-<type>-<prefix>`, where `<prefix>` is the first 8 hex digits of the player
UUID. Before creating, the client looks the name up: an existing account owned
by the same player is reused; an account owned by someone else causes the prefix
to be lengthened until the name is free. An account left behind by a failed RPC
is therefore picked up again by the next attempt.

## Alternatives considered

### Create the account in the microservice

Its advantage: identity and account are created in one server-side flow, so the
two can be kept consistent in one place and no orphan account ever reaches a
client.

It was rejected because the microservice would need a working surf-transaction
client at runtime, a new runtime dependency and deployment coupling for the
microservice.

### Delete the account when the RPC fails

Its advantage: no account ever outlives a failed creation.

It was rejected because the delete is itself a remote call that can fail, so it
cannot guarantee cleanup; reuse by name converges on retry without a second
failure-prone step.

### Name accounts after the player name

Its advantage: readable account names such as `Steve-police`.

It was rejected because player names change and are reused by other players,
which breaks both the reuse lookup and name uniqueness.

## Consequences

### What this gives us

The microservice stays free of surf-transaction. Retries are idempotent with
respect to accounts. Names are stable across player renames.

### What this costs

Identity creation is a multi-step client flow with a lookup, a possible create,
and an RPC. Orphan accounts can exist between a failed attempt and a retry.
Account names are not human-friendly.

### Follow-on work

Name generation with collision fallback, and unit tests for it. The Paper plugin
must have surf-transaction available at runtime.

### What this forecloses

Creating identities from anywhere without surf-transaction, such as the
microservice itself or Velocity. Moving creation server-side later requires
adding surf-transaction to the microservice and changing the RPC contract.
