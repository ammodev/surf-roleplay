# ADR-0037: Screen actions are validated against the server-held screen

- **Status:** Superseded by ADR-0042
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** ADR-0042

## Context

Players act on screens by clicking buttons and submitting input. The mod can be modified
(ADR-0020), so a client can send actions for screens it never saw, for buttons that are
disabled or hidden, or with input values outside the allowed range. The server already holds
the current tree of every open screen, because it applies its own patches to it (ADR-0036).

## Decision

A screen action carries the screen session id, the id of the widget that triggered it, and the
current values of the screen's inputs. The server accepts the action only if all of these hold:
- the session is in the sending player's screen stack
- the widget exists in the session's current tree, is enabled, and can trigger actions
- every submitted value belongs to an input of that screen and passes that input's constraints

Otherwise the action is dropped and logged. Actions are also rate-limited per player. Action
handlers are server-side functions bound to the widget. The client never names a handler.
Typed screen actions pass the same session check, and their handlers validate the typed
payload.

## Alternatives considered

### Single-use action tokens

Its advantage: each clickable widget carries an opaque token that is valid for one render and
one use, which rejects replayed and stale clicks without the server keeping a tree.

It was rejected because the server keeps the tree anyway, so tokens add no protection that the
tree check lacks, and every patch would have to reissue them.

## Consequences

### What this gives us

A modified client can at most trigger actions that a normal player could trigger on that
screen at that moment.

### What this costs

- The server keeps a full tree per open screen and checks it on every action.
- Input constraints exist twice: enforced as UX in the mod and again on the server.

### Follow-on work

Constraint validation on the server, a per-player action rate limit, and logging of rejected
actions.

### What this forecloses

Actions that are not tied to an open screen session; those need their own packets.
