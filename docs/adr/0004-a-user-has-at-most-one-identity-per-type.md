# ADR-0004: A user has at most one identity per type

- **Status:** Accepted
- **Date:** 2026-09-25
- **Supersedes:** none
- **Superseded by:** none

## Context

Identities come in three types: civilian, police, and SAR. Without a limit, a
player could hold any number of identities of one type, which affects account
naming, storage constraints, and how the rest of the API finds "the police
identity" of a player.

## Decision

A user holds at most one identity of each type. Creating a second identity of an
existing type fails. The database enforces this with a unique constraint on
(user, type).

## Alternatives considered

### Unlimited identities

Its advantage: players could run several personas of the same type, for example
two civilians with separate lives.

It was rejected because it complicates account naming and every lookup of a
player's identity by type, without a current roleplay need.

### Exactly one civilian plus optional others

Its advantage: every user always has a civilian identity, so there is always a
default persona to fall back to.

It was rejected because it forces automatic identity and account creation for
every player who ever joins, including those who never roleplay.

## Consequences

### What this gives us

A player's identities are addressable by type, and account names can be derived
from player and type (ADR-0002).

### What this costs

A player who wants a fresh persona of a type must delete the old one first,
losing its balance, licenses, rank, and qualifications.

### Follow-on work

The unique constraint, a creation check that reports the conflict, and a unit
test for the rule.

### What this forecloses

Multiple personas of one type. Allowing them later changes account naming, the
unique constraint, and every lookup by type.
