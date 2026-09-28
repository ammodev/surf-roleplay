# ADR-0039: The mod UI is built on an own toolkit

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

The mod renders every server-driven screen (ADR-0032) with flex layout (ADR-0033), and later
the phone, tablet, inventory and HUD. Minecraft's vanilla screens offer basic widgets but no
layout system that fits server-described trees. The mod targets Minecraft 26.2 and must follow
every Minecraft update.

## Decision

The mod builds its UI on its own small toolkit, written in the mod on top of vanilla rendering:
- a component tree
- a pure-Kotlin flex layout engine that does not depend on Minecraft classes
- the widget implementations
- one roleplay theme, defined as constants in the mod

It adds no UI library dependency.

## Alternatives considered

### A UI library such as owo-lib

Its advantage: a ready-made component and layout system with many widgets, so far less UI code
has to be written and tested.

It was rejected because it adds a runtime dependency that must be released for every Minecraft
version the mod targets, and its component model would have to be bent to fit server-described
trees and patches.

### Vanilla screens and widgets only

Its advantage: no extra code layer, and screens look and behave like Minecraft's own.

It was rejected because vanilla has no layout engine for nested server-described trees, so
positions would still have to be computed by hand, and styling is limited to vanilla sprites.

## Consequences

### What this gives us

Full control over layout, styling and behaviour, a layout engine that can be unit-tested
without Minecraft, and no dependency to wait for on a Minecraft update.

### What this costs

- Upfront work for layout, focus handling, scrolling and text input.
- Every widget is ours to maintain and fix.

### Follow-on work

The toolkit itself, its tests, and adapting its rendering layer on every Minecraft update.

### What this forecloses

Adopting a UI library later means porting every screen built on the toolkit.
