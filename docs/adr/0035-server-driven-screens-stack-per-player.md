# ADR-0035: Server-driven screens stack per player

- **Status:** Superseded by ADR-0048
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** ADR-0048

## Context

Many screens open other screens: a phone opens apps, a shop list opens an item detail, a form
opens a confirmation. The server has to know which screens a player has open in order to
validate actions and to close screens when the game state changes.

## Decision

Each player has a stack of open server-driven screens, held by the server and mirrored by the
mod. The server can open a screen on top of an existing one, naming it as the parent. Only the
top screen is shown. When the player presses Escape or back, the mod closes the top screen,
tells the server, and shows the parent again with its state intact. A screen marked as not
closable ignores Escape. The server can close any screen, which also closes every screen above
it, or close the whole stack.

## Alternatives considered

### One screen at a time

Its advantage: a single open screen per player is simpler on both sides, with no parent
tracking and no hidden screens holding state.

It was rejected because every sub-menu would have to rebuild its parent screen from scratch on
return, losing typed input and scroll position.

## Consequences

### What this gives us

Natural back navigation for nested screens, with the parent's state preserved for free.

### What this costs

- Hidden screens keep their state and server-side handlers in memory until they are closed.
- The server and the mod must agree on the stack, which needs careful handling of races
  between a server close and a player close.

### Follow-on work

The stack on the server and in the mod, close propagation to child screens, and cleanup when a
player quits or changes server.

### What this forecloses

Several independent screens visible side by side; the stack shows only its top.
