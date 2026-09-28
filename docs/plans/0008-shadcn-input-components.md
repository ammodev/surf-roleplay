# Plan 0008: shadcn input components

- **Status:** In progress
- **Date:** 2026-09-28
- **Accepted proposal:** Port the 17 input components of the shadcn registry (button, button-group, input-group, textarea, switch, radio-group, slider, toggle, toggle-group, input-otp, native-select, select, combobox, calendar, field, form, label) as native screen nodes with change events, pre-accepted under the standing autonomy for shadcn-faithful components
- **Decision records:** ADR-0051, ADR-0052, ADR-0053, ADR-0054

## Goal

`/rpscreen inputs` opens a screen on the dev stack that shows every input component of the shadcn
registry in every variant, size and state:
- button, with the variants default, destructive, outline, secondary, ghost and link, and the
  sizes default, xs, sm, lg, icon, icon-xs, icon-sm and icon-lg
- button group, horizontal and vertical, with text and separator
- input group, with addons at inline-start, inline-end, block-start and block-end, group buttons,
  group text and a group textarea
- input, of type text, password and email
- textarea
- switch, in sizes default and sm
- radio group, vertical and horizontal
- slider, with one and with two thumbs, horizontal and vertical
- toggle, with the variants default and outline in the sizes sm, default and lg
- toggle group, single and multiple
- input OTP, with groups and a separator
- native select, and select with groups, labels, separators, a placeholder and the sizes sm and
  default
- combobox, single and with multiple chips, with client-side filtering and an empty text
- calendar, in single, multiple and range mode, in German with Monday first, with a month and
  year dropdown
- field, with set, legend, group, label, title, description, separator and error, in vertical and
  horizontal orientation
- form, whose submit returns per-field errors from the server
- label, whose `for` target focuses its input

Every component works with the mouse and with the keyboard (ADR-0047). Every value is validated by
the server before a handler sees it (ADR-0042). Inputs show their invalid state only after the
player changed them or a submit failed. Toggles fire actions, and any input can opt into change
events, which the server validates before running its change handler (ADR-0052). The server-side
input rules live in one table per element kind, instead of being spread over the validator, the
screen tree and the mapper. `./gradlew build` passes, and every new function, class and public
property has a KDoc comment.

## Out of scope

- The display, overlay, navigation and data component groups (plans 0009 to 0012).
- Text wrapping in labels (plan 0009).
- Server-side fuzzy search for comboboxes; only the optional search event is added.
- Time-of-day inputs and time zones (ADR-0053).
- The component DSL, the GUI DSL and the storybook.

## Steps

### Step 1: Add change events and a per-element input rule table

**Does:**
- **Protocol:** adds the serverbound packet `screen_input_change(sessionId, widgetId, value)` and a
  `notifyChange` flag on every input node.
- **Paper:**
  - moves the per-kind logic for current value, enabled state, constraint check and value update
    into one `InputRules` table keyed by element class; the validator and the screen tree use it
  - validates change events against the table, stores the value, runs the element's `onChange`
    handler, and applies the rate limit
- **API:** gives input elements an optional `onChange` handler.
- **Mod:** sends change events for inputs with `notifyChange`, and marks inputs as touched, so
  that the invalid state shows only after a change or a failed submit.

**Ends in:** Any input can report changes, and the input rules of every existing kind live in one
place.

**Verified by:** `./gradlew build` passes, with tests for the change packet round trip, change
validation (unknown session, not top, unknown input, constraint violation, accepted), the rule
table for every existing input kind, and the touched state in the mod.

**Pushes:** no

### Step 2: Add button variants, button groups, toggles and toggle groups

**Does:**
- **Button:** gains `variant` and `size`, drawn with shadcn's token mapping. Icon sizes draw the
  icon only.
- **New nodes:** `button_group` (orientation) with `button_group_text` and
  `button_group_separator`, `toggle` (pressed, variant, size, text, icon), and `toggle_group`
  (single or multiple, items, selected, variant, size, spacing).
- **Behaviour:** toggles fire an action with their new state. Toggle groups are inputs whose value
  is the selected item values joined by commas.
- **Server and API:** each node gets its API element, DSL builder, mapper case and input rule.

**Ends in:** Every variant and size of button, button group, toggle and toggle group exists in the
protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with round-trip, mapping, input-rule and widget
keyboard tests for each new node.

**Pushes:** no

### Step 3: Add text components

**Does:**
- **Input:** gains `inputType` (`TEXT`, `PASSWORD`, `EMAIL`). Passwords are drawn masked, and email
  values are checked for an address shape on the server.
- **New nodes:**
  - `textarea` (rows, max length, required), with multi-line editing and vertical scrolling
  - `input_group`, with `input_group_addon` (align), `input_group_button`, `input_group_text` and
    a group textarea
  - `input_otp` (length, groups, digits or alphanumeric), with slot-by-slot entry and paste
- **Label:** gains a `forId` that focuses the named input when the label is clicked.
- **Server and API:** each node gets its API element, DSL builder, mapper case and input rule.

**Ends in:** All text entry components exist in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for multi-line editing, OTP entry and paste,
masking, and the server rules for length, required, email shape and OTP pattern.

**Pushes:** no

### Step 4: Add switch, radio group and slider

**Does:**
- **New nodes:**
  - `switch` (checked, size), toggled by click and Space
  - `radio_group` (options, selected, orientation, required), with arrow keys moving the
    selection
  - `slider` (min, max, step, one or two values, orientation), with dragging, clicking the track
    and arrow keys
- **Checkbox:** restyled to shadcn's checkbox.
- **Server and API:** each node gets its API element, DSL builder, mapper case and input rule. The
  slider's rule checks the range and step.

**Ends in:** The choice and range inputs exist in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for the slider's value math (step snapping,
range order, keyboard steps), radio keyboard movement, and the server rules.

**Pushes:** no

### Step 5: Replace dropdown with select, native select and combobox

**Does:**
- **Rename:** `dropdown` becomes `select`, which is allowed because nothing is released. It gains
  groups, group labels, separators, a placeholder and a size.
- **New nodes:**
  - `native_select`: grouped options and a size, with a simpler trigger
  - `combobox`: single or multiple selection with chips, a search placeholder and an empty text,
    filtered on the client (ADR-0054), with an optional search change event
- **Server and API:** each node gets its API element, DSL builder, mapper case and input rule.

**Ends in:** All selection components exist, and no `dropdown` remains.

**Verified by:** `./gradlew build` passes, with tests for combobox filtering, chip add and remove,
keyboard navigation in the lists, and the server rules for unknown options and multiple values.

**Pushes:** no

### Step 6: Add the calendar

**Does:**
- **Node:** `calendar`, with:
  - mode (`SINGLE`, `MULTIPLE` or `RANGE`) and the selected dates
  - the displayed month
  - min and max dates, and disabled dates
  - whether outside days are shown
  - caption layout (`LABEL` or `DROPDOWN`)
- **Mod:** draws the calendar in German with Monday first and supports mouse and arrow-key
  navigation.
- **Wire format:** values use ISO dates (ADR-0053).
- **Server and API:** the node gets its API element, DSL builder, mapper case and input rule.

**Ends in:** The calendar exists in the protocol, the mod and the API.

**Verified by:** `./gradlew build` passes, with tests for the German month grid (first weekday,
leap years), range selection order, min and max limits, and the server's date parsing.

**Pushes:** no

### Step 7: Add field and form

**Does:**
- **Field nodes:**
  - containers: `field_set`, `field_group`, `field` (orientation) and `field_content`
  - separator: `field_separator`
  - text: `field_legend`, `field_label`, `field_title`, `field_description` and `field_error`
- **Form node:** `form`, with a submit button id. Enter in one of its inputs clicks the submit
  button.
- **API:** `ScreenClick.fail(errors)` shows per-field errors and the invalid state through a
  patch.
- **Server:** the mapper handles every new node.

**Ends in:** Forms with labelled fields and server-side errors can be built.

**Verified by:** `./gradlew build` passes, with tests for Enter-to-submit, error patches, and
mapping.

**Pushes:** no

### Step 8: Add the inputs demo page

**Does:** Adds `/rpscreen inputs`, which shows every component of this plan in every variant, size
and state, grouped into sections. Its actions and change events report in German in chat.

**Ends in:** One screen exercises the whole input group.

**Verified by:** `./gradlew build` passes and the page opens on the dev stack.

**Pushes:** no

### Step 9: Verification

**Does:** Checks the whole goal on the dev stack, then runs a fresh whole-plan review and fixes its
findings.

**Ends in:** Every statement of the goal has been observed, and the review findings are fixed or
listed.

**Verified by:**
- `./gradlew build` passes, and every new declaration has a KDoc comment.
- Screenshots of the demo page in the default dark theme and in one light theme, saved to
  `screenshots/0008/`.
- Every component operated live by mouse and by keyboard, with chat output checked.
- A form submit with errors shows them under the fields.
- A change event and a toggle action reach the server.
- A reviewer's findings are fixed with tests.

**Pushes:** no

## Push points

none

## Risk

Step 1 is the most likely to go wrong. Moving the input logic of every existing kind into one
table touches the validator, the screen tree and the mapper at once, and a slip there silently
weakens validation (ADR-0042). The existing validation and tree tests must pass unchanged before
any new component is added. If they cannot pass without changing what they assert, the agent
stops and reports instead of adjusting the tests.

## If reality contradicts this plan

The agent stops and asks. It does not silently rewrite this plan to match what actually
happened, and it does not continue past a step whose stated end state was not reached.
