# ADR-0048: Screens are presented as full screen, dialog or sheet

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** ADR-0035
- **Superseded by:** none

## Context

Each player has a stack of open server-driven screens (ADR-0035), and so far only the top screen
is shown. A confirmation or a small popup therefore replaces its parent completely, although the
player expects to see it on top of the screen it belongs to, as with a web dialog or a side
drawer.

## Decision

Each player has a stack of open server-driven screens, held by the server and mirrored by the
mod. The server can open a screen on top of an existing one, naming it as the parent. Every
screen has a presentation:
- `SCREEN` replaces what is shown; screens below it are hidden.
- `DIALOG` shows a centered panel over its parent.
- `SHEET` shows a panel sliding in from an edge (left, right, top or bottom) over its parent.

Dialogs and sheets are drawn over everything below them down to the nearest `SCREEN`, which is
dimmed and receives no input. Only the top screen receives input. Escape, or for dialogs and
sheets a click on the dimmed backdrop, closes the top screen if it is closable, tells the server,
and shows the parent again with its state intact. The server can close any screen, which also
closes every screen above it, or close the whole stack. The server API offers a confirmation
helper that opens a dialog with a confirm and a cancel button.

## Alternatives considered

### Only the top screen is shown

Its advantage: one drawing path and no layering, as before.

It was rejected because popups lose the context of the screen they belong to.

### Dialogs only, sheets later

Its advantage: less to build now.

It was rejected because side sheets and drawers are part of the component set the gamemode
needs, and they share almost all of the dialog machinery.

## Consequences

### What this gives us

Confirmations, popups and side panels that appear over the screen they belong to.

### What this costs

- The mod draws several screens per frame and must keep lower ones inert.
- One more field on screen open packets.

### Follow-on work

The presentation field, layered drawing with a dimmed backdrop, the sheet slide-in, and the
confirmation helper.

### What this forecloses

Several screens receiving input at the same time.
