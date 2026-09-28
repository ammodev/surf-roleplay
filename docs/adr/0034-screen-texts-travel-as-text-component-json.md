# ADR-0034: Screen texts travel as text component JSON

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** none

## Context

Screens sent by the server contain player-facing text: titles, labels, button captions and
list rows. The server builds text with Adventure components, and the mod renders vanilla
Minecraft components. The packet body is ProtoBuf (ADR-0027), which has no text type of its own.

## Decision

Every player-facing text in a screen packet is a string field that holds the text component in
its JSON form. The server serializes Adventure components with the JSON component serializer.
The mod parses the JSON into a vanilla component with Minecraft's component codec.

## Alternatives considered

### MiniMessage source strings

Its advantage: compact on the wire and easy to read in logs and debugging tools.

It was rejected because the mod would need a MiniMessage parser, which is either a new runtime
dependency in the mod or a hand-written subset.

### An own styled-text message

Its advantage: compact, fully typed in ProtoBuf, and independent of Minecraft's text format.

It was rejected because it supports only the styles it defines, and the server would need a
converter from Adventure components.

## Consequences

### What this gives us

The full styling of Minecraft text, including colors, decorations, fonts and translatable keys,
with the conversion handled by existing serializers on both sides.

### What this costs

- JSON text is several times larger than a compact encoding.
- The JSON text format changes with Minecraft versions, so server and mod must stay on
  compatible Minecraft versions.
- Malformed JSON is only detected when the mod parses it.

### Follow-on work

A conversion helper on each side and a fallback in the mod for text that fails to parse.

### What this forecloses

Changing the text format later changes every text field in every screen packet and needs a
protocol version bump.
