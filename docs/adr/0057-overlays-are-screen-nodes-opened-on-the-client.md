# ADR-0057: Overlays are screen nodes opened on the client

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

The shadcn registry (ADR-0051) has a group of overlays: popover, hover card, tooltip, dropdown
menu, context menu, menubar, dialog, alert dialog, sheet and drawer. In the browser each is a
trigger and a content that the page shows on top of everything, without asking a server. The
screen framework can already stack whole server-opened screens as dialogs and sheets
(ADR-0048), but every such screen costs a round trip to open, and its inputs belong to a
different screen than the one it was opened from. A menu or a tooltip that waits for the server
would feel broken.

## Decision

Every overlay component is a node in the screen's tree. It holds its trigger and its content as
children, and the mod opens and closes it without asking the server: on a click of the trigger,
on a right click for a context menu, and on hovering for tooltips and hover cards. Its content is
part of the screen, so inputs inside it submit with the screen's other inputs and are validated
the same way (ADR-0042). An overlay's open state is an input value; an overlay can opt into
change events that report it, and the server can open or close it with a patch.

Overlays stack: a menu's sub-menu, or a select inside a popover, opens above the overlay it
belongs to. A click outside an overlay closes it and every overlay above it. Dialogs, alert
dialogs, sheets and drawers are modal: they dim everything below them and keep the keyboard focus
inside their content.

Server-opened dialog and sheet screens stay for flows whose next screen the server decides.

## Alternatives considered

### Every overlay is a server-opened screen

Its advantage: one mechanism, already built, and the server always knows what is open.

It was rejected because menus, tooltips and hover cards would wait for a round trip, and the
inputs of a popover would not submit with the screen they belong to.

### Overlays travel in their own packets

Its advantage: the screen tree stays free of hidden content, and overlays could be shown over any
screen.

It was rejected because the overlay content would need its own ids, validation and patches,
duplicating what the screen tree already provides.

## Consequences

### What this gives us

Overlays open at once and behave like their shadcn originals, and their inputs need no separate
handling on the server.

### What this costs

- The mod's single popover becomes a stack with focus trapping, modal backdrops and nested menus.
- Content that is not shown still travels with the screen and counts towards its size.
- Every overlay adds an input value to every submit of its screen.

### Follow-on work

An overlay stack in the mod, a patch that opens and closes overlays, open state input rules on
the server, and the overlay nodes, widgets and elements.

### What this forecloses

Overlays that are not part of a screen, such as a menu opened from the HUD. Those need their own
mechanism, as toasts get one.
