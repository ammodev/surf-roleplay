# ADR-0042: Screen actions are accepted only for the top screen

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** ADR-0037
- **Superseded by:** none

## Context

ADR-0037 accepts a screen action for any session in the player's screen stack. Screens stack
(ADR-0035), and only the top screen is shown. A normal player can therefore only act on the top
screen, but a modified client can send actions for a screen hidden below it, such as clicking
"Kaufen" on a shop while a confirmation screen covers it. That contradicts the guarantee ADR-0037
states for itself: a modified client can at most trigger what a normal player could trigger at
that moment.

## Decision

A screen action carries the screen session id, the id of the widget that triggered it, and the
current values of the screen's inputs. The server accepts the action only if all of these hold:
- the session is the top screen of the sending player's screen stack
- the widget exists in the session's current tree, is enabled, and can trigger actions
- every submitted value belongs to an input of that screen, is submitted once, keeps its value if
  the input is disabled, and passes the input's constraints where the triggering button requires
  valid input

Otherwise the action is dropped and logged. Actions are rate-limited per player. Action handlers
are server-side functions bound to the widget; the client never names a handler. Typed screen
actions pass the same top-screen check, and their handlers validate the typed payload. A player's
report that they closed a screen is accepted only for a closable top screen.

## Alternatives considered

### Accept actions for any screen in the stack

Its advantage: a server feature could let a hidden parent react to input, for example a live
list below a dialog, without routing it through the child.

It was rejected because hidden screens cannot be clicked by a normal player, so accepting their
actions only helps modified clients.

## Consequences

### What this gives us

Covered screens are inert, which closes the parent-click hole and matches what the player sees.

### What this costs

- An action that was sent just before the server opened a child is rejected as stale.
- The server keeps the full tree per open screen and checks it on every action.

### Follow-on work

A top-screen check for widget actions, typed actions and close reports, and quieter logging of
stale rejections that come from ordinary latency.

### What this forecloses

Screens that accept input while covered; such interaction has to live on the top screen.
