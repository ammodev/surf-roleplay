# ADR-0066: The chat view is a native node the server appends to

- **Status:** Accepted
- **Date:** 2026-09-29
- **Supersedes:** none
- **Superseded by:** none

## Context

Roleplay screens need conversations: phone messengers, radio logs and chats with NPCs. shadcn
has no chat primitive in its component list, but its AI chat blocks show the familiar form:
bubbles for one's own messages and for the others', each with avatar, name and time, a list that
stays at its newest message, and a composer below. The data, chat and charts group (ADR-0051)
includes such a view.

A conversation grows while the screen is open. Open screens change through patches by widget
id (ADR-0036), and an insert patch already appends a node to a container.

## Decision

A chat view is a native node that shows its messages in a scrolling column. A chat message is a
native node with a side (own or other), an avatar (a player head or a resource-pack texture, as
for avatars in ADR-0056), a name, a time text and content of any components. The server adds a
message with the existing insert patch; the view stays scrolled to the newest message while it
was at the bottom, and keeps the player's position while they read further up. The composer is
not a node of its own; screens build it from the input group and a button.

## Alternatives considered

### A composite of existing nodes

Its advantage: no new nodes; a column in a scroll area with cards and avatars could look close
enough.

It was rejected because the mod could not know which container is a conversation, so it could
not keep it at the newest message, and bubbles aligned by side would need special layout rules
anyway.

### A dedicated packet that streams messages

Its advantage: messages could arrive without the server holding them in the screen tree, which
suits very long conversations.

It was rejected because patches already deliver nodes to open screens, and a second path would
duplicate validation, ids and ordering.

## Consequences

### What this gives us

Conversations look and behave like their shadcn models, grow while the screen is open, and use
the same patch and id rules as every other component.

### What this costs

- Every message stays in the server-held screen tree and in the mod, so long conversations grow
  both; screens must remove old messages themselves.
- The view needs scroll handling of its own to stick to the newest message.

### Follow-on work

The chat view and chat message nodes, widgets and elements, the insert handling that keeps the
view at the bottom, and a demo that composes a composer.

### What this forecloses

Messages that bypass the screen tree. Streaming very long histories would need a new decision.
