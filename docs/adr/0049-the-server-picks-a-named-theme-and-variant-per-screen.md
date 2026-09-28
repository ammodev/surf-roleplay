# ADR-0049: The server picks a named theme and variant per screen

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

The mod's toolkit draws every screen with one roleplay theme defined as constants (ADR-0039).
The gamemode's organisations need their own look: police and SAR screens, such as the MDT or the
Einsatzleiter tablet, should be recognisable at a glance, and screens should work in light and
dark. The shadcn component set the toolkit adopts is built on named design tokens.

## Decision

The mod ships a set of design tokens (background, foreground, card, popover, primary, secondary,
muted, accent, destructive, border, input, ring, radius and their foreground pairs) for each of
the themes `default`, `sar` and `police`, each in a `LIGHT` and a `DARK` variant. Every screen
open names the theme and variant to draw with. The mod falls back to `default` for an unknown
theme. Widgets draw only with tokens, never with fixed colours.

This replaces the single constant theme of ADR-0039; the rest of ADR-0039 stands.

## Alternatives considered

### The server sends the token values

Its advantage: palettes become content, changeable without a mod release (ADR-0015).

It was rejected because every screen open would carry the whole token set, and the mod would
have to validate arbitrary colours.

### A player setting

Its advantage: players choose what they find readable.

It was rejected because the look should belong to the organisation whose screen it is.

## Consequences

### What this gives us

Screens that show which organisation they belong to, in light or dark, from one set of widgets.

### What this costs

- New or changed palettes need a mod release.
- Every widget must be restyled onto tokens.

### Follow-on work

The token sets, the theme and variant fields on screen opens, and restyling every widget.

### What this forecloses

Palettes defined by staff at runtime, unless a later decision sends tokens from the server.
