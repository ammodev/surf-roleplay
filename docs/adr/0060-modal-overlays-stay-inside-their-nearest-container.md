# ADR-0060: Modal overlays stay inside their nearest container

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

Inline dialogs, alert dialogs, sheets and drawers are modal overlays opened on the client
(ADR-0057). In the browser, shadcn draws them over the whole page, and they were placed over
the whole game window the same way. Roleplay screens are often devices inside the game, such as
a phone or a police tablet, and a sheet or drawer opened on such a device must stay on the
device instead of covering the whole window.

## Decision

A modal overlay is placed inside, and dims only, the nearest overlay container around its host.
An overlay container is a node that marks a region, such as the frame of a phone drawn inside a
larger screen. Without an overlay container around the host, the overlay stays inside the
visible content of the screen panel it belongs to. Dialogs are centered in that area, and sheets
and drawers attach to its edges.

Popovers, menus, hover cards and tooltips are not modal and keep being placed next to their
triggers anywhere in the window.

## Alternatives considered

### The whole window

Its advantage: it matches shadcn, which draws modal content over the whole page, and needs no
extra node.

It was rejected because a device drawn as a screen could not show a sheet or drawer of its own
without covering everything around it.

### The screen panel only

Its advantage: no new node; a device that is its own screen already works.

It was rejected because a device drawn inside a larger screen, next to other content, could not
confine its overlays.

## Consequences

### What this gives us

Devices and other framed regions can use sheets, drawers and dialogs that stay inside them.

### What this costs

- One more container node in the protocol, the mod and the API.
- Modal placement depends on the tree around the host, not only on the window.
- A dialog in a small container is squeezed to that container's size.

### Follow-on work

The overlay container node, placement and backdrop inside the container or the panel content,
and tests for both.

### What this forecloses

A modal overlay that breaks out of its container. That would need an explicit option later.
