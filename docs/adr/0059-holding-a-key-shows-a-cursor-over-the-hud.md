# ADR-0059: Holding a key shows a cursor over the HUD

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

Toasts can carry action and cancel buttons (ADR-0058), and they are shown while the player moves
around without a screen. Without a screen, Minecraft captures the mouse for looking around, so
there is no cursor to click a button with.

## Decision

While the player holds a key, the mod shows a cursor over the HUD, as holding Alt does in Arma.
The key is a regular key binding in the controls settings, in a roleplay category, and defaults to
the left Alt key. While it is held, the mouse no longer turns the view, clicks go to the toasts
under the cursor instead of the world, and the player keeps walking with the movement keys.
Releasing the key captures the mouse again. While a screen is open, toasts can be clicked with
the screen's cursor without holding the key.

## Alternatives considered

### A key that triggers the newest toast's action

Its advantage: no cursor, and no change to how the mouse behaves.

It was rejected because it reaches only one button of one toast, and players cannot see which
action the key would trigger.

### Toast buttons only while a screen is open

Its advantage: no new input mode.

It was rejected because most toasts appear without a screen, and their actions would be
unreachable.

## Consequences

### What this gives us

Every toast button is clickable at any time, with a binding players can move.

### What this costs

- A new input mode in which the mod releases and re-captures the mouse.
- The default key conflicts with nothing in vanilla, but other mods may bind left Alt.

### Follow-on work

The key binding, the cursor mode, and click handling for toasts over the HUD and over screens.

### What this forecloses

Using the default key for anything else while roleplaying. Other HUD elements that become
clickable later use the same cursor mode.
