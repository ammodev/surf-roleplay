# ADR-0055: Screen texts wrap to the width their container gives them

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

Screen texts are laid out on one line, and the layout measures every node without knowing the
width it will get (ADR-0033). The display components of the shadcn registry (ADR-0051) are full
of running text: alert and card descriptions, empty states, item descriptions, paragraphs and
field descriptions. On one line these texts overflow their panel or force a screen far wider
than its content needs.

## Decision

The layout computes sizes height-for-width, as a browser does. A container passes the width it
can give to its children when it measures them, and a text node's height is the height of its
text wrapped at word boundaries to that width. In a row, texts that do not fit shrink towards
their longest word before anything overflows. A text node may set a maximum number of lines;
the last shown line then ends with an ellipsis.

Buttons, badges, keyboard keys, toggles and other components that shadcn marks as
`whitespace-nowrap` never wrap.

## Alternatives considered

### The server breaks lines

Its advantage: the layout stays a single pass without width constraints, and the server fully
controls where lines break.

It was rejected because only the mod knows the font, the GUI scale and the window size, so the
server cannot know where a line has to break.

### Fixed-width text blocks

Its advantage: a text node with a fixed width could wrap without changing how containers
measure their children.

It was rejected because every screen author would have to pick widths by hand, and texts in
growing or stretched containers could still not follow the space they actually get.

## Consequences

### What this gives us

Running text reads naturally in every component and at every window size and GUI scale, and
screens no longer grow to the width of their longest sentence.

### What this costs

- Measuring depends on the available width, so a node may be measured several times per layout.
  The layout caches measurements per width to keep this cheap.
- Row layout gains a shrink step for wrapping texts, which makes the layout harder to reason
  about.
- Existing screens whose texts were wider than their container now wrap instead of
  overflowing.

### Follow-on work

Width-aware measuring in the mod's layout, wrapped and clamped text drawing, and tests for
wrapping in columns, rows and scroll lists.

### What this forecloses

Laying out a node without knowing its width. Returning to single-line texts would change the
look of every screen that relies on wrapping.
