# ADR-0032: Server-driven screens combine widget trees and typed screens

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

The mod renders all player-facing UI (ADR-0012), and the server is authoritative (ADR-0020), so
the server decides which screen a player sees and what it contains. The gamemode needs two
very different kinds of screens:
- many simple forms, menus, confirmations and lists (shops, Bürgeramt forms, staff tools)
- a few complex, highly interactive screens (inventory, phone, minimap, tablet)

Every screen needs the same plumbing: open, update, close, and send player actions back.

## Decision

Server-driven screens come in two kinds that share one session layer:
- **Generic screens.** The server sends a tree of generic widgets that the mod renders without
  knowing what the screen is for.
- **Typed screens.** The mod implements the screen in code. The server sends a screen type key
  and a ProtoBuf state payload whose schema is defined, per type, in the protocol module
  together with the schema of the actions the screen sends back.

Both kinds are opened, updated and closed through the same session packets and share session
ids, stacking and action routing.

## Alternatives considered

### Typed screens only

Its advantage: every screen is hand-built in the mod, so each can look and behave exactly as
intended, and the wire format per screen is small and fully typed.

It was rejected because every new form or menu, however simple, would need a mod release and
new protocol types.

### Generic widget trees only

Its advantage: new screens need no mod release at all, and there is only one rendering path to
maintain.

It was rejected because the inventory, phone, minimap and tablet need interaction that a
generic widget set cannot express well, such as drag and drop, map rendering and custom
drawing.

## Consequences

### What this gives us

Simple screens ship with a server update alone, and complex screens still get native,
purpose-built UI, without duplicating the session plumbing.

### What this costs

- Two rendering paths in the mod and two builder paths on the server.
- Each new feature must choose which kind of screen it uses.
- Typed screen state schemas become part of the protocol and follow its versioning rules.

### Follow-on work

The session packets, the widget tree model, a typed screen registry in the protocol module, a
typed screen registry in the mod, and the server-side API for both kinds.

### What this forecloses

Removing either kind later means rewriting every screen built on it.
