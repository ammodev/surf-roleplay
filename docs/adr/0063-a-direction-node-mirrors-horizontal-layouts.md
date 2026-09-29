# ADR-0063: A direction node mirrors horizontal layouts

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

The shadcn registry (ADR-0051) includes a direction provider that sets the text direction of a
subtree to left-to-right or right-to-left, so that components lay themselves out for languages
written from right to left. Player text is German (ADR-0008), so right-to-left text is not
needed today, but the registry is ported in full, and screens such as a car's mirror-image
dashboard or an Arabic sign in the world can use mirrored layouts.

Minecraft's font draws glyphs left to right only and has no bidirectional text support.

## Decision

A direction node sets the direction of its subtree to left-to-right or right-to-left. Inside a
right-to-left subtree, the mod mirrors horizontal layouts: rows place their children from the
right, start and end alignment swap, and components with a side (sheets, sidebars, carousel
buttons, menu chevrons, breadcrumb separators) use the mirrored side. Text itself is still drawn
left to right; only its alignment inside its box is mirrored. A nested direction node overrides
the direction for its own subtree.

## Alternatives considered

### Leave the direction provider out

Its advantage: no mirroring code in every layout and component, and nothing a German-only
server needs.

It was rejected because the registry is ported in full and mirrored layouts are useful for
in-world devices.

### Reverse the text too

Its advantage: real right-to-left text for languages that need it.

It was rejected because Minecraft's font has no bidirectional support, and reversing characters
without shaping would produce wrong text for the languages that need it.

## Consequences

### What this gives us

Every component can be laid out mirrored with one wrapping node, as with shadcn's provider.

### What this costs

- Layouts and components with a side must read the direction from their ancestors.
- Tests have to cover both directions for components with a side.

### Follow-on work

The direction node, mirroring in the flex layout and in components with a side, and tests.

### What this forecloses

Real right-to-left text rendering; that would need bidirectional support in the font renderer
and a new decision.
