# ADR-0045: The mod drops screen opens for a parent it no longer has

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

Screens stack per player (ADR-0035), and both sides apply the same rule to an open: without a
parent, or with a parent that is not open, every open screen is replaced. When the player closes
screen P at the same moment the server opens child C on top of P, the mod no longer has P and
shows C as a new root, while the server receives the close of P and closes C with it. The
player then sees a screen the server has already closed, and is stuck if it is not closable.

## Decision

When the mod receives a screen open with a parent session that it does not have open, it does
not show the screen. It reports the new session as closed to the server instead. An open without
a parent keeps replacing every open screen. The server's stack rule does not change.

## Alternatives considered

### Keep the shared rule

Its advantage: one rule for both sides, with no special case in the mod.

It was rejected because the race leaves the player on a dead screen, which a non-closable screen
turns into a stuck player.

## Consequences

### What this gives us

A close by the player always wins over a child opened at the same moment, and the two stacks
converge again.

### What this costs

- A child opened right as its parent closes is lost; the feature that opened it sees it closed.
- The mod applies one extra check before the shared stack rule.

### Follow-on work

The check in the mod's screen handling.

### What this forecloses

Using an open with a stale parent as a way to replace the stack; features that want a new root
open it without a parent.
