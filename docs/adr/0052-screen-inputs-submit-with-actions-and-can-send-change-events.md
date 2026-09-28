# ADR-0052: Screen inputs submit with actions and can send change events

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

The screen framework gains the shadcn input components: switches, radio groups, sliders, toggle
groups, selects, comboboxes, calendars, text areas and one-time code fields (ADR-0051). Some
screens only need their values when the player submits, while others must react at once, for
example a live search or a filter that updates a list. Other components, such as toggles and menu
items, are actions in themselves.

## Decision

Input components hold values that travel to the server with the next widget action, as text and
number inputs already do (ADR-0042). Every input can additionally opt into change events. The mod
then sends a `screen_input_change` packet with the input's new value as soon as the player changes
it. The server validates that value against the input's constraints, stores it in its copy of the
screen, and runs the input's change handler. Change events count against the same per-player rate
limit as actions.

Toggles fire a widget action when pressed, carrying their new state. Tabs, accordions and
collapsibles change only on the client, and can opt into change events in the same way.

## Alternatives considered

### Every change is sent immediately

Its advantage: the server always knows every value, and no screen needs a submit button.

It was rejected because most forms need their values only on submit, and sending every keystroke
and slider step multiplies traffic and server handlers.

### Values only on submit

Its advantage: one path for all values and the least traffic.

It was rejected because live searches, filters and dependent fields would need a button press for
every update.

## Consequences

### What this gives us

Forms stay cheap, and screens that need to react at once can do so per input.

### What this costs

- One more serverbound packet, with its own validation path.
- Feature code must choose per input whether it needs change events.

### Follow-on work

The change event packet, its validation and handlers on the server, and a change event option on
every input element.

### What this forecloses

Nothing permanent: inputs without change events behave as before.
