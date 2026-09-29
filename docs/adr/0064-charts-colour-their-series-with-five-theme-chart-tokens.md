# ADR-0064: Charts colour their series with five theme chart tokens

- **Status:** Accepted
- **Date:** 2026-09-29
- **Supersedes:** none
- **Superseded by:** none

## Context

The chart component of the shadcn registry (ADR-0051) draws several series next to each other,
and each series needs a colour of its own. Screen components take every colour from the theme of
their screen (ADR-0049), so that a police or rescue theme recolours a whole screen. The themes so
far have no colours that are meant to be told apart side by side: primary, secondary, muted,
accent and destructive each carry a meaning.

shadcn solves this with five theme variables, `--chart-1` to `--chart-5`, that a chart config
assigns to its series.

## Decision

Every theme variant has five chart colours, `chart1` to `chart5`, chosen to be told apart from
each other on the theme's background. A chart names the colour of each series by its number, 1
to 5. The server cannot send an arbitrary colour for a series. The default theme uses shadcn's
chart palette for its dark and light variants; other themes may override it.

## Alternatives considered

### Arbitrary colours per series

Its advantage: a screen can match any real-world colour, such as a party's or a company's
colour, and can show more than five clearly different series.

It was rejected because it breaks theming: a chart would keep its colours when the screen's
theme or variant changes, and nothing would keep the colours readable on the background.

### Derive series colours from the primary colour

Its advantage: no new tokens; every theme gets chart colours for free as shades of its primary.

It was rejected because shades of one hue are hard to tell apart, which is the whole point of a
series colour, and it differs from shadcn.

## Consequences

### What this gives us

Charts follow the screen's theme and variant like every other component, with a palette that is
known to be readable, and the wire format for a series colour is a small number.

### What this costs

- Every theme variant carries five more colours that have to be picked and checked for
  contrast.
- Charts with more than five series reuse colours.

### Follow-on work

The five tokens in every theme variant, and the chart node that refers to them.

### What this forecloses

Series in freely chosen colours. Allowing them later is a new field on the chart node and a new
decision.
