# ADR-0005: Clients cache player data only while the player is online

- **Status:** Accepted
- **Date:** 2026-09-25
- **Supersedes:** none
- **Superseded by:** none

## Context

The microservice is the source of truth for users, identities, ranks,
qualifications, and licenses. Paper servers need that data locally and quickly.
Several Paper servers run at once, so a change made on one server could leave
another server's copy stale.

## Decision

A Paper server loads a player's data from the microservice when the player
logs in and drops it when the player quits. Every write goes through a
microservice RPC, and the local copy is replaced with the state the RPC returns.
Servers do not notify each other of changes.

## Alternatives considered

### Microservice broadcasts changes

Its advantage: every server's cache stays correct, including when a player is
changed from a server they are not on.

It was rejected because it needs an event channel and handling on every client,
while a player is normally connected to only one Paper server at a time.

### Redis pub/sub invalidation between clients

Its advantage: correct invalidation across servers using surf-redis, which is
already on the classpath, without changing the microservice.

It was rejected for the same reason: extra messaging for a case that normal play
does not produce.

## Consequences

### What this gives us

A simple cache with one owner per player and no cross-server messaging.

### What this costs

If a player is changed from a server they are not connected to, their server
keeps the old data until they rejoin. Login waits on an RPC, and a failing
microservice prevents login.

### Follow-on work

A login listener that loads the user, a quit listener that evicts it, and cache
replacement after every write RPC.

### What this forecloses

Reliable cross-server edits of online players. Adding them later means adding
an invalidation or event channel and handling it on every client.
