# ADR-0058: Toasts travel in their own packets and show over the HUD

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

The shadcn registry's toast component, sonner (ADR-0051), shows short notifications that stack in
a corner and disappear by themselves. In the game they tell players about things that happen
while no screen is open, such as a paid fine or an incoming radio call, and some carry an action
such as "Annehmen". Screens only exist while one is open, so a toast cannot be a node of a
screen tree (ADR-0057).

## Decision

Toasts are sent in their own clientbound packets. A toast has an id chosen by the server, a type
(default, success, info, warning, error or loading), a title, an optional description, optional
action and cancel labels, a duration and whether the player can dismiss it. Showing a toast with
the id of a shown toast replaces it, so a loading toast can turn into a success, and the server
can dismiss a toast by id.

The mod draws toasts stacked at the bottom right, over the HUD and over any open screen. A click
on a toast's action or cancel button sends a serverbound toast action. The server accepts it only
for a toast it showed to that player that is still active and has that button, counts it against
the player's action rate limit, and then runs the button's handler.

## Alternatives considered

### Toasts as vanilla system messages or action bar texts

Its advantage: no mod code, and every client can show them.

It was rejected because they cannot carry buttons, types or stacking, and the action bar holds
only one line at a time.

### Toasts as a small server-opened screen

Its advantage: buttons and validation already exist for screens.

It was rejected because a screen takes the mouse and the keyboard, and a notification must not
interrupt the player.

## Consequences

### What this gives us

Notifications that look like sonner, show anywhere, and can offer actions that the server
validates like screen actions.

### What this costs

- Two clientbound and one serverbound packet, with their own validation path on the server.
- The server keeps the handlers of active toasts per player until they expire or are dismissed.

### Follow-on work

The toast packets, a toast service in the server API, the per-player toast registry on the
server, and toast drawing and clicking in the mod.

### What this forecloses

Toast content beyond text and two buttons. Richer notifications need a new ADR.
