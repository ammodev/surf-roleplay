# ADR-0029: The mod handshake runs in the configuration phase

- **Status:** Accepted
- **Date:** 2026-09-27
- **Supersedes:** none
- **Superseded by:** none

## Context

The server refuses clients that do not complete the mod handshake (ADR-0012), and
a protocol version mismatch disconnects the player (ADR-0013). A Minecraft
connection passes through a configuration phase before the player enters the
world. The handshake can run there, or after the player has joined the world.

## Decision

The mod handshake runs in the configuration phase, before the player enters the
world. The server holds the connection until the mod's hello packet arrives or a
configurable timeout expires. It then either lets the player continue or
disconnects them with a German reason: missing mod, protocol mismatch, or
forbidden mods.

## Alternatives considered

### Play phase with a timeout

Its advantage: it uses only well-documented play-phase plugin messaging on both
sides, and it is simpler to build and to route through Velocity.

It was rejected because vanilla and outdated clients would briefly enter the world
and would have to be frozen and hidden until they are kicked, and every join
listener would have to know about players that are not yet verified.

## Consequences

### What this gives us

Only verified clients ever spawn in the world, so no gameplay code has to handle
unverified players.

### What this costs

- Paper's public API does not expose custom payloads in the configuration phase,
  so the Paper side hooks into server internals. That hook can break on any
  Minecraft update.
- Behind Velocity, configuration-phase payloads must pass the proxy, which needs
  its own testing.
- A slow client delays its own login by up to the timeout.

### Follow-on work

The configuration-phase payload hook on Paper, the configuration-phase sender on
Fabric, and a Velocity test setup.

### What this forecloses

Moving the handshake to the play phase later means adding an unverified-player
state to join handling.
