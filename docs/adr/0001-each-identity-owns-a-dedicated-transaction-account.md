# ADR-0001: Each identity owns a dedicated transaction account

- **Status:** Accepted
- **Date:** 2026-09-25
- **Supersedes:** none
- **Superseded by:** none

## Context

A roleplay user can hold several identities (civilian, police, SAR). Every
`RoleplayIdentity` is a surf-transaction `Transactional`, and `RoleplayUser`
routes all money operations to its active identity. Nothing defined which funds
an identity actually operates on, so the money model of the whole plugin was
undefined.

## Decision

Every identity owns exactly one surf-transaction `Account`. The account is
created when the identity is created, its id is stored with the identity, and it
is deleted when the identity is deleted. The identity's convenience money
operations (`balance(currency)`, `deposit(amount, currency)`, ...) target that
account. Money earned as one identity is not available to the player's other
identities.

## Alternatives considered

### Shared player account

Its advantage: no account lifecycle to manage at all. Every identity delegates to
the player's `TransactionUser` and default account, nothing has to be created,
named, or cleaned up, and there is no failure mode between the roleplay
microservice and surf-transaction.

It was rejected because identities are meant to be separate roleplay personas;
a shared balance lets a player move money between a civilian and a police
persona without any in-game transaction, which undermines the separation that
identities exist to provide.

## Consequences

### What this gives us

Separate, auditable finances per persona. Transfers between a player's own
identities become real transactions with a trail.

### What this costs

An extra remote call (account creation) on every identity creation and another
on deletion. Two systems now hold state that must agree: the identity row and
the surf-transaction account. Deleting an identity destroys its balance.

### Follow-on work

An account naming and creation scheme (see ADR-0002). Identity deletion must
delete the account. Identity storage needs an `account_id` column.

### What this forecloses

A shared balance across identities. Reversing it later means merging per-identity
balances into one account per player, a data migration across both the roleplay
database and surf-transaction.
