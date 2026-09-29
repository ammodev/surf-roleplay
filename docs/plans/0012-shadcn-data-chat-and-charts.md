# Plan 0012: shadcn data, chat and charts

- **Status:** Done
- **Date:** 2026-09-29
- **Accepted proposal:** Port the data, chat and chart components of the shadcn registry (table, data table, chart) and a chat view modelled on shadcn's AI chat blocks as native screen nodes; pre-accepted under the standing autonomy for shadcn-faithful components
- **Decision records:** ADR-0051, ADR-0052, ADR-0056, ADR-0064, ADR-0065, ADR-0066

## Goal

`/rpscreen data` opens a screen on the dev stack that shows:
- a table with caption, header, body and footer rows, whose columns line up across rows and
  whose cells hold any component
- a data table with sortable columns (ascending, descending, off by header click), a text filter,
  pages of rows with "Zurück" and "Weiter", and row selection by checkbox with a select-all box
- charts of all six families: area, bar (vertical, horizontal, stacked), line (linear, curved,
  step, with dots), pie and donut, radar, and radial, each with a grid and axes where they
  apply, a tooltip on hover with the dot, line or dashed indicator, and an optional legend
- a chat view with own and other messages (avatar, name, time), a composer built from the input
  group, and new messages appended by the server while the screen is open

Every chart colours its series with the five chart tokens of the screen's theme (ADR-0064), in
the dark and light variants of every theme. The data table's view is one JSON input value with
optional change events that the server validates and can set by patch (ADR-0065). The chat view
stays at its newest message while it was at the bottom when the server appends one (ADR-0066).
`./gradlew build` passes, and every new function, class and public property has a KDoc comment.

## Out of scope

- Server-side sorting, filtering or paging of data that is not all in the screen.
- Column resizing, reordering, hiding and pinning, and row expansion in data tables.
- Chart animations, zoom, brushing and clickable chart elements; charts only show data.
- Charts from arbitrary colours, and more than five distinguishable series.
- Chat features beyond the view: typing indicators, read receipts, reactions and attachments.
- A dedicated composer node; the composer is composed from the input group.
- The component DSL, the GUI DSL and the storybook.

## Steps

### Step 1: Add chart tokens

**Does:** Adds `chart1` to `chart5` to the theme tokens of every theme variant, using shadcn's
default chart palette for the default theme's dark and light variants.

**Ends in:** Every resolved theme has five chart colours.

**Verified by:** `./gradlew build` passes, with a test that every theme variant has five chart
colours that differ from each other and from its background.

**Pushes:** no

### Step 2: Add table

**Does:**
- **New nodes:** `table` with `table_caption`, `table_header`, `table_body`, `table_footer`,
  `table_row`, `table_head` and `table_cell` (align).
- **Behaviour:** columns get the width of their widest cell, growing to fill the table; header
  and footer rows are muted; rows have a border below and highlight on hover; the caption sits
  below the table, muted.
- **Server and API:** elements, builders, mapper cases and tree cases.

**Ends in:** Tables exist in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for column widths across rows, alignment,
and a round trip.

**Pushes:** no

### Step 3: Add data table

**Does:**
- **New nodes:** `data_table` (page size, selectable, filter placeholder, filter column) with
  `data_table_column` (key, header, sortable, align) and `data_table_row` (id, selectable) whose
  `data_table_cell` children carry a sort key and any content.
- **Behaviour:**
  - A header click cycles its column through ascending, descending and off; the arrow shows the
    direction.
  - The filter keeps rows whose filter column's sort key contains the text, ignoring case.
  - Pages show the page size of rows, with "Zurück", "Weiter" and "Seite x von y".
  - A checkbox per row and a select-all box in the header select rows; the footer says how
    many are selected.
- **State:** the view is one JSON input value with optional change events and a patch.
- **Server and API:** elements, builders, mapper cases and the input rule (a sortable column,
  a page that exists for the filter, known and selectable rows).

**Ends in:** Data tables exist in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for sorting both ways, filtering, paging,
selection, the value format and its validation.

**Pushes:** no

### Step 4: Add chart foundation with bar, line and area

**Does:**
- **New node:** `chart` (kind, categories, series with key, label, colour 1 to 5 and values,
  height, options: stacked, horizontal, curve, dots, grid, x and y axes, legend, tooltip and its
  indicator).
- **Behaviour:** the mod draws the grid, the axes with their labels, and bar, line and area
  series; hovering a category shows a tooltip with its label and every series' value; the
  legend lists the series with their colour.
- **Server and API:** element, builder with series and options, and mapper case.

**Ends in:** Bar, line and area charts exist in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for value scaling, stacking, horizontal
bars, category hit testing and the round trip.

**Pushes:** no

### Step 5: Add pie, radar and radial charts

**Does:** Adds the pie (with donut and labels), radar and radial kinds to the chart node,
with tooltips and legends like the other kinds.

**Ends in:** All six chart families exist.

**Verified by:** `./gradlew build` passes, with tests for slice angles, radar points, radial
arcs and hit testing.

**Pushes:** no

### Step 6: Add chat view

**Does:**
- **New nodes:** `chat_view` with `chat_message` (own, avatar player id or texture, fallback,
  name, time) holding any content.
- **Behaviour:** own messages sit at the right in a primary bubble, others at the left in a
  muted bubble with their avatar; the view scrolls, starts at the newest message and stays
  there when a message is appended while it was at the bottom.
- **Server and API:** elements, builders, mapper and tree cases, and appending messages by
  insert patch.

**Ends in:** The chat view exists in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for bubble sides, sticking to the bottom,
keeping the position when scrolled up, and the insert patch.

**Pushes:** no

### Step 7: Add the data demo page

**Does:** Adds `/rpscreen data`. It shows every component of this plan; data table changes and
chat messages report in German in chat, and sending a chat message appends it and a reply by
patch.

**Ends in:** One screen exercises the whole data, chat and chart group.

**Verified by:** `./gradlew build` passes and the page opens on the dev stack.

**Pushes:** no

### Step 8: Verification

**Does:** Checks the whole goal on the dev stack, then runs a whole-branch review and fixes its
findings.

**Ends in:** Every statement of the goal has been observed, and the review findings are fixed or
listed.

**Verified by:**
- `./gradlew build` passes, and every new declaration has a KDoc comment.
- Screenshots saved to `screenshots/0012/` of every component in the dark default theme and in
  one light theme, and of a chart tooltip.
- Live checks: sorting, filtering, paging and selecting in the data table report through change
  events; a sent chat message is appended and the view stays at the bottom.
- The review's findings are fixed with tests.

**Pushes:** no

## Push points

none

## Risk

Step 4 is the most likely to go wrong. The mod has no vector drawing: lines, areas and curves
have to be drawn from the GUI's rectangle fills, one pixel column at a time, and must stay crisp
at every GUI scale. If lines or areas cannot be drawn acceptably with fills, the agent stops and
reports instead of switching to a new rendering approach on its own.

## If reality contradicts this plan

The agent stops and asks. It does not silently rewrite this plan to match what actually
happened, and it does not continue past a step whose stated end state was not reached.
