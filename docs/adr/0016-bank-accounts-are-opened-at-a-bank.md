# ADR-0016: Bank accounts are opened at a bank

- **Status:** Accepted
- **Date:** 2026-09-26
- **Supersedes:** none
- **Superseded by:** none

## Context

Every identity was given a surf-transaction account when it was created. The
roleplay now separates cash, which players carry as items, from bank accounts.
Opening an account is meant to be a roleplay step, and it also provides an IBAN,
cards and shared accounts.

## Decision

An identity has no bank account until its player opens one at a bank in-game. An
identity can hold cash without an account. Opening an account creates the
surf-transaction account and an IBAN for that identity.

## Alternatives considered

### Create the account together with the identity

Its advantage: every identity can receive transfers and paychecks from the start,
and no identity is ever without a place for money.

It was rejected because it removes the bank as a roleplay destination and turns
the account into an invisible technical detail.

## Consequences

### What this gives us

A bank visit as part of onboarding, and accounts that exist only for identities
whose players chose to open one.

### What this costs

Every money flow (paychecks, benefits, invoices, fines) needs a rule for
identities without an account. Existing identity accounts need migration.

### Follow-on work

Bank account opening, IBAN issuing, card items, handling of payments to identities
without an account, and migration of the existing identity-bound accounts.

### What this forecloses

Code can no longer assume that an identity has an account.
