# ADR-0051: The screen framework covers the shadcn component registry

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** ADR-0033
- **Superseded by:** none

## Context

Generic screens are trees of flex containers and a fixed set of nine widgets (ADR-0033). The
gamemode's UIs need far more: tabs, tables, dialogs, menus, toasts, sliders, calendars, chat
views and charts. The shadcn component registry, 63 components, is a widely known, complete
vocabulary for such UIs.

## Decision

A generic screen is a tree. Its inner nodes are `row` and `column` flex containers with gap,
padding, alignment and per-child fixed, fitting or growing sizing, and the mod computes all
positions. Every node has an id that is unique within its screen.

The widget vocabulary covers every component of the shadcn registry, including direction, form,
resizable and chart. Each component is its own native node kind in the protocol, with its own
widget in the mod and element in the server API, carrying the component's variants, sizes and
states. Components are built in groups: inputs, display, overlays and menus, navigation and
layout, and data, chat and charts.

## Alternatives considered

### Primitives with server-side composites

Its advantage: a few new primitives (a styled box, an icon, text styles) would let the server
API compose cards, badges and alerts without a protocol change for each.

It was rejected because interactive components such as tabs, sliders, comboboxes and calendars
still need native nodes, and composites would behave less like their originals.

### A curated subset

Its advantage: far less code, focused on what roleplay screens need first.

It was rejected in favour of a complete, predictable vocabulary.

## Consequences

### What this gives us

Screens can be designed with a complete, widely known component set, and every component
behaves natively in the mod.

### What this costs

- A large amount of code in the protocol, mod, API and server mapping, and tests for each.
- Every component is part of the wire format and follows its versioning rules.
- Components with little in-game meaning, such as direction, still have to be built and kept.

### Follow-on work

Plans for the five component groups, each verified live.

### What this forecloses

Keeping the widget set small. Removing a component later is a breaking protocol change.
