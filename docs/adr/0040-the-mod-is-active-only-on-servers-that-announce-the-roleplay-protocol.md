# ADR-0040: The mod is active only on servers that announce the roleplay protocol

- **Status:** Superseded by ADR-0041
- **Date:** 2026-09-28
- **Supersedes:** none
- **Superseded by:** ADR-0041

## Context

Players install the roleplay mod and may also join other servers with the same client. The mod
used to send its hello, which lists every loaded mod, to every server it joined. It will also
change vanilla UI, register keybinds and open screens, none of which should happen on other
servers.

## Decision

The mod behaves as a plain client unless the server identifies itself as the roleplay server:
- In the configuration phase, the mod sends its hello only if the server announced that it
  receives the hello channel.
- In the play phase, the roleplay server sends a `welcome` packet after join. All roleplay
  behaviour in the mod, including screens, UI changes and keybinds, is active only after the
  `welcome` arrives, and is reset when the player disconnects.

## Alternatives considered

### Keep sending the hello everywhere

Its advantage: no detection logic, and a server that does not know the channel simply ignores
it.

It was rejected because it reveals the player's mod list to every server, and it does nothing
to keep roleplay UI changes off other servers.

### An address allowlist in the mod

Its advantage: detection that does not depend on how the server announces channels, and that
works before any packet is exchanged.

It was rejected because server addresses change between production, test and development
setups, and every change would need a mod release or a config edit on every client.

## Consequences

### What this gives us

Other servers see a plain client and receive no mod list, and players keep vanilla behaviour
there.

### What this costs

- Every roleplay feature in the mod must check that the roleplay server is active.
- The hello depends on Paper, and Velocity in front of it, announcing configuration-phase
  channels, which has to be verified and can break with an update.
- One more clientbound packet on every join.

### Follow-on work

The announcement check before sending the hello, the `welcome` packet, and a roleplay-active
state in the mod that later features consult.

### What this forecloses

Using roleplay mod features on servers that do not speak the roleplay protocol.
