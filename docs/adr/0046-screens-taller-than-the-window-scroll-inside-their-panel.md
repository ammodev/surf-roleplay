# ADR-0046: Screens taller than the window scroll inside their panel

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

Generic screens are laid out by the mod within the window (ADR-0033). Minecraft's automatic GUI
scale picks the largest scale that fits the window, so on large displays the window is only
about 200 GUI pixels high. A screen whose content is taller than that has its root clamped to
the window, and its lower widgets spill out of the panel where they cannot be reached. The same
screen fits when the window is a little smaller and the automatic scale drops.

## Decision

A screen's content is laid out at its full preferred height. If it is taller than the space the
window leaves for the panel, the panel is capped at the window height and its content scrolls
vertically, with the mouse wheel and a scroll bar at the panel's right edge. A scroll list under
the mouse scrolls first; the panel scrolls when the list cannot scroll further. The width keeps
being clamped to the window.

## Alternatives considered

### Scale the screen down

Its advantage: the whole screen stays visible at once, and nothing needs scrolling.

It was rejected because text shrinks below the GUI scale the player chose, and on large
screens it can become unreadable.

### Lower the GUI scale while a roleplay screen is open

Its advantage: the screen looks the same as in a smaller window, with no new interaction.

It was rejected because it overrides the player's setting and changes the size of everything
else on screen while the roleplay screen is open.

## Consequences

### What this gives us

Every widget of every screen stays reachable at every GUI scale, and text keeps the player's
chosen size.

### What this costs

- A second scroll layer on top of scroll lists, with nested hit-testing and clipping.
- Players may not notice that a screen has more content below.

### Follow-on work

Panel scrolling in the screen host, scroll offsets in hit-testing and dropdown lists, and
scrolling the focused widget into view.

### What this forecloses

Screens that rely on being seen whole at once must be designed to fit a small window.
