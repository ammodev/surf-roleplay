# ADR-0061: Content of an unreachable overlay rejects input

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

Overlays are screen nodes whose content the client shows after a click on a trigger (ADR-0057).
The server validates actions and changed values against the screen it holds (ADR-0037). That
check looks at the widget itself: whether it exists and whether it is enabled. It does not look
at how the widget is reached.

A server that disables the trigger of a dropdown menu, for example for players without a
permission, expects the items inside the menu to be out of reach. A normal client cannot open
the menu. A modified client can still send an action for an item inside it, and the server
accepts it, because the item itself is enabled.

## Decision

The server rejects an action for a widget inside the content of an unreachable overlay, and a
changed value for an input inside it. It also rejects a player opening an overlay whose triggers
are all unusable.

An overlay is reachable when it is not itself inside the content of an unreachable overlay, and
at least one of these holds:

- the server holds it as open, for example because the server opened it;
- one of its triggers contains an enabled widget that opens it, such as an enabled button, and
  every element between that widget and the overlay is enabled;
- it is a context menu or a hover card, which open on a right click or on hover over their area
  rather than through a trigger widget.

Closing an overlay is always accepted.

## Alternatives considered

### Document that a disabled trigger does not protect the content

Its advantage: no extra check on the server and no rule to keep in step with the client. Servers
that need protection disable the items themselves.

It was rejected because it is easy to get wrong. Disabling the button that opens a menu looks
like it protects the menu, and a mistake would be exploitable by any modified client.

## Consequences

### What this gives us

Disabling or hiding a trigger protects everything in its overlay, including nested overlays,
against modified clients.

### What this costs

- One walk up the server-held tree for every action and changed value.
- The server needs to know which children of a host are triggers and which is the content, for
  every overlay element.

### Follow-on work

The reachability check in the action validator, and tests for disabled and nested
triggers.

### What this forecloses

A screen cannot rely on a disabled context-menu area or hover-card trigger to protect its
content; it has to disable the items themselves. A player who closed an overlay that the server
opened cannot open it again once its triggers are disabled.
