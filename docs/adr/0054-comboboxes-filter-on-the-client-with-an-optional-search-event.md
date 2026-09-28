# ADR-0054: Comboboxes filter on the client with an optional search event

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

Comboboxes and command palettes let the player type to narrow down a list of options. The list
can be filtered by the mod as the player types, or by the server for each query. Most lists in the
gamemode are small, such as organisations, vehicles or items in a shop, but some, such as a
register of all citizens, are too large to send in full.

## Decision

The server sends a combobox's or command palette's options, and the mod filters them as the
player types, matching case-insensitively on each option's label. A combobox can opt into search
events: the mod then also reports the query as a change event (ADR-0052), and the server can
replace the options with a patch.

## Alternatives considered

### Server-side search only

Its advantage: lists of any size, and search rules decided entirely by the server.

It was rejected because every keystroke would wait for a server round trip, which feels slow for
the small lists that make up most uses.

## Consequences

### What this gives us

Instant filtering for ordinary lists, and a way to handle large lists when needed.

### What this costs

- Every option of an ordinary list is sent with the screen.
- Two filtering paths, one on the mod and one on the server, can disagree on matching rules.

### Follow-on work

Client-side filtering in the combobox and command widgets, and the search event option.

### What this forecloses

Fuzzy or ranked matching without a mod release, unless the server takes over through search
events.
