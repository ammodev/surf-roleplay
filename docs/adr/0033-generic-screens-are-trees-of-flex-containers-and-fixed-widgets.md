# ADR-0033: Generic screens are trees of flex containers and fixed widgets

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

Generic screens are described by the server and rendered by the mod (ADR-0032). The wire
format has to say which widgets a screen holds and where they go. Players run the client at
different window sizes and GUI scales, so the positions must adapt.

## Decision

A generic screen is a tree. Its inner nodes are `row` and `column` flex containers with gap,
padding, alignment and per-child fixed or grow sizing, and the mod computes all positions. Its
leaves come from a fixed widget set:
- `label` (styled text)
- `button`
- `text_input`, `number_input`, `checkbox` and `dropdown`, which carry constraints (max length,
  number range, required)
- `scroll_list`, a scrollable column of child nodes
- `image`, a texture from the mod or resource pack by identifier
- `progress`, a progress bar

Every node has an id that is unique within its screen.

## Alternatives considered

### Absolute positions

Its advantage: the mod only draws what it is told, with no layout engine, and the server has
full control over every pixel.

It was rejected because fixed positions break across window sizes and GUI scales.

### A fixed column grid

Its advantage: much simpler to lay out than nested flex containers, and still adapts to the
window width.

It was rejected because forms, lists with side panels and toolbars do not fit a single grid
well.

## Consequences

### What this gives us

Screens that adapt to any window size, built from a small vocabulary the server can combine
freely.

### What this costs

- A flex layout engine in the mod, which needs its own tests.
- Every new widget kind is a protocol change and needs a mod release.

### Follow-on work

The widget and container model in the protocol module, the layout engine and widget
implementations in the mod, and builders for the tree in the server API.

### What this forecloses

Pixel-exact generic screens; those have to be typed screens.
