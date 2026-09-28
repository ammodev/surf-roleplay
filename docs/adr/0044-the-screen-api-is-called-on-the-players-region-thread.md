# ADR-0044: The screen API is called on the player's region thread

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

The city server runs on Canvas, a Folia-based fork, where each region of the world ticks on its
own thread and a player is owned by the thread of the region they stand in. The screen API
(ADR-0038) keeps per-player state that open, patch, close and action handling all change.
Feature code may call it from timers, other players' handlers or asynchronous work. Guarding the
state with locks leaves user handlers running under a lock, and two players whose handlers touch
each other's screens can deadlock two region threads.

## Decision

Every method of the screen API that reads or changes a player's screens must be called on the
thread that owns that player. The implementation checks this on every call and throws if it is
called from another thread. It does not lock the state. Code on another thread schedules the
call onto the player's scheduler first. Player screen packets are handled on the player's thread
as well.

## Alternatives considered

### Lock every screen operation

Its advantage: callers may use the API from any thread without scheduling.

It was rejected because handlers would run under the lock or need careful release, and locks on
two players' states can deadlock across regions.

## Consequences

### What this gives us

A simple, lock-free state model without deadlocks, and wrong-thread calls fail loudly at their
source.

### What this costs

- Callers on timers, the global region or asynchronous code must schedule onto the player.
- A call for a player from another player's handler must be scheduled as well.

### Follow-on work

The ownership check on every entry point, and thread documentation on the API types.

### What this forecloses

Calling the screen API directly from asynchronous code. Allowing it later means adding
synchronisation to every operation.
