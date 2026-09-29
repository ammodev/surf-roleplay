# ADR-0065: Data table state lives on the client as one JSON value

- **Status:** Accepted
- **Date:** 2026-09-29
- **Supersedes:** none
- **Superseded by:** none

## Context

The data table of the shadcn registry (ADR-0051) sorts by a column, filters rows by a text,
shows one page of rows at a time and lets the player select rows. In the browser all of this
runs in the page. Tabs, accordions and similar components already keep their state on the
client as an input value with optional change events (ADR-0062), and inputs submit with actions
(ADR-0052).

Unlike those components, a data table has four pieces of state at once. The input value of a
widget is a single string, and filter texts may contain any character, so a simple separated
list cannot carry them.

## Decision

Sorting, filtering, paging and row selection of a data table run in the mod without asking the
server. The table's whole view is its input value, a JSON object:

```json
{"sort": "name", "desc": false, "filter": "rtw", "page": 0, "selected": ["r1", "r4"]}
```

- `sort` is the key of the sorted column, or absent for the row order the server sent;
- `desc` is whether it sorts descending;
- `filter` is the filter text, empty for none;
- `page` is the index of the shown page, from 0;
- `selected` is the ids of the selected rows, in row order.

The value submits with every action and is validated on the server: the column must be sortable,
the page must exist for the filtered rows, and the selected rows must exist and be selectable. A
table can opt into change events that report the new value, and the server can set the value
with a patch.

## Alternatives considered

### Separate inputs for sort, filter, page and selection

Its advantage: each piece is a plain value like the other inputs, and a screen can listen to
only the part it needs.

It was rejected because one table would need four ids and four rules that must stay consistent,
such as a page that exists only for the current filter.

### The server sorts, filters and pages

Its advantage: tables larger than one screen's worth of data could be loaded page by page, and
the server always knows what the player sees.

It was rejected because every click on a header or key in the filter would wait for a round
trip, and the chosen behaviour for data tables is client-side like shadcn's.

## Consequences

### What this gives us

Data tables react at once, and the server still learns the whole view, including the selection,
with the next action or a change event.

### What this costs

- All rows of a table travel with the screen, so very large tables make large screens.
- The server parses and validates a JSON value, and a malformed one is rejected.
- Sorting is by a per-cell sort key the server sends, since the mod cannot compare arbitrary
  cell content.

### Follow-on work

The table and data table nodes, widgets and elements, the value rule on the server, and a
parser and writer for the value on both sides.

### What this forecloses

Server-side paging of large data sets within one table. It would need a new decision and a
table that loads rows on demand.
