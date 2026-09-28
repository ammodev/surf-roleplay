# ADR-0041: The mod is active only after the roleplay server welcomes it

- **Status:** Accepted
- **Date:** 2026-09-28
- **Supersedes:** ADR-0040
- **Superseded by:** none

## Context

ADR-0040 made the mod send its hello only to servers that announce the hello channel in the
configuration phase. On Paper 26.2 behind Velocity, no channel registration happens in the
configuration phase in either direction. The mod therefore never learns that the server
receives `roleplay:hello`, although the server does receive and handle it. Gating the hello on
the announcement would stop the handshake on the roleplay server itself.

## Decision

The mod sends its hello at the start of every configuration phase, to every server. Servers
that do not know the channel ignore it. All other roleplay behaviour in the mod, including
screens, UI changes and keybinds, is active only after the roleplay server sends the
play-phase `welcome` packet, and is reset when the player disconnects.

## Alternatives considered

### Announce the hello channel through Paper internals

Its advantage: the mod could keep sending its hello only to servers that announce the channel,
so no other server ever sees the mod list.

It was rejected because it needs a second hook into server internals next to the handshake's,
must also survive Velocity in the configuration phase, and can break on every Minecraft or
Velocity update.

### An address allowlist in the mod

Its advantage: detection that works before any packet is exchanged and does not depend on
channel announcements.

It was rejected because server addresses change between production, test and development
setups, and every change would need a mod release or a config edit on every client.

### A two-stage hello

Its advantage: the hello sent everywhere would carry only versions, while the mod list would
go only to the roleplay server in the play phase.

It was rejected because the whitelist check would move to the play phase, so clients with
forbidden mods would enter the world before being disconnected.

## Consequences

### What this gives us

A handshake that works on the roleplay server without any further internals hook, and a mod
that behaves as a plain client on other servers.

### What this costs

- Every server the player joins can read the mod's version and the player's loaded mods.
- Every roleplay feature in the mod must check that the roleplay server is active.
- One more clientbound packet on every join.

### Follow-on work

The `welcome` packet, and a roleplay-active state in the mod that later features consult.

### What this forecloses

Hiding the mod list from other servers, unless a later decision adds an announcement hook or a
two-stage hello.
