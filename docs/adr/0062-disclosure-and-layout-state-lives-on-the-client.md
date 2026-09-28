# ADR-0062: Disclosure and layout state lives on the client

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

The navigation and layout group of the shadcn registry (ADR-0051) holds components whose state
changes with every click or drag: which tab is selected, which accordion items and collapsibles
are open, which carousel slide is shown, whether a sidebar is expanded, and how wide the panels
of a resizable group are. In the browser this state lives in the page. A round trip per tab
switch or per pixel of a drag would make them feel broken, and the server usually only needs to
know the state when the player submits.

Overlays already keep their open state on the client as an input value (ADR-0057), and inputs
can send change events (ADR-0052).

## Decision

Tabs, accordions, collapsibles, carousels, sidebars and resizable panel groups change their state
in the mod without asking the server. Each one's state is an input value of the screen:

- tabs: the value of the selected tab
- accordion: the values of the open items, comma separated
- collapsible and sidebar: `true` or `false`
- carousel: the index of the shown slide
- resizable panel group: the panel sizes in percent, comma separated

The value is submitted with every action like any other input value and validated on the server.
Each component can opt into change events that report the new value, and the server can set the
value with a patch. Content that is not shown, such as an inactive tab or a closed accordion
item, stays in the tree, so patches reach it and its inputs submit.

## Alternatives considered

### The server decides every change

Its advantage: the server always knows what the player sees, and one screen state lives in one
place.

It was rejected because every tab switch, slide or drag would wait for a round trip, and a drag
would send a packet per frame.

### The state is not reported at all

Its advantage: less data per submit and no validation rules for layout state.

It was rejected because screens often need to know the selected tab or the open section, and a
server patch could not know what it is replacing.

## Consequences

### What this gives us

These components react at once, like their shadcn originals, and the server still learns their
state with the next action or through change events.

### What this costs

- Every such component adds an input value to every submit of its screen.
- Hidden content still travels with the screen and counts towards its size.
- The server needs a value rule for each component: known tab values, known item values, slide
  indexes in range, and panel sizes inside their limits that sum to 100.

### Follow-on work

The nodes, widgets and elements of the group, their input rules on the server, and patches that
set their values.

### What this forecloses

Pages that the server loads lazily when a tab is selected, unless the screen asks for change
events and patches the content in.
