# ADR-0036: Open screens change through patches by widget id

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

Open screens change while the player looks at them: a list gains a row, a counter ticks, a
button becomes disabled, a validation error appears. The server has to tell the mod what
changed without disturbing what the player is doing, such as typing into a field or scrolling a
list.

## Decision

The server changes an open generic screen by sending patches. A patch names the screen session
and carries a list of operations, each addressed by widget id:
- replace a node
- insert a node into a container at an index
- remove a node
- set a widget's text
- set an input's value
- set whether a widget is enabled

The mod applies the operations in order and keeps focus, scroll position and typed input for
every widget that survives. A typed screen is updated by sending its complete new state.

## Alternatives considered

### Resend the whole tree

Its advantage: one simple update path, and the server never has to compute what changed.

It was rejected because frequent updates, such as a live list or a timer, would resend every
widget each time, and matching old and new trees to keep focus and scroll is error-prone.

## Consequences

### What this gives us

Small update packets and screens that stay stable under the player's cursor.

### What this costs

- More operations to implement and test on both sides.
- The server must keep its copy of the tree in step with every patch it sends.
- A patch addressing an unknown widget id is a server bug that the mod can only log.

### Follow-on work

The patch operations in the protocol, their application in the mod, and a server-side tree
that applies the same operations so that it matches what the player sees.

### What this forecloses

Nothing permanent: a full replace remains possible as a replace of the root node.
