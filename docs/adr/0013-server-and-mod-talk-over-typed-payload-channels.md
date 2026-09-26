# ADR-0013: Server and mod talk over typed payload channels

- **Status:** Accepted
- **Date:** 2026-09-26
- **Supersedes:** none
- **Superseded by:** none

## Context

The required client mod (ADR-0012) renders all UI and vehicles, so the server and
the mod exchange a large and growing set of messages: screen contents, form
submissions, vehicle inputs and corrections, HUD state, and radio and dispatch
updates. Both sides are written in Kotlin, and mod and server versions can differ
during an update rollout.

## Decision

The server and the mod communicate through custom payload channels in the
`roleplay` namespace. Every packet is a Kotlin class serialized with
kotlinx.serialization. It is defined in a shared protocol module that both the
Paper plugin and the mod depend on. On join the mod sends its protocol version,
and a mismatch disconnects the player with an update notice.

## Alternatives considered

### An existing networking library

Its advantage: packet registration, versioning and serialization come ready-made,
so there is less code to own.

It was rejected because no library in use covers both a Fabric client and a Paper
server with shared Kotlin types. Adapting one would add a dependency and still
leave the need for a shared module.

### Hand-written binary codecs per packet

Its advantage: full control over the byte layout and the smallest possible
packets.

It was rejected because every packet would need a writer and a reader kept in
sync by hand, which does not scale to the number of packets this gamemode needs.

## Consequences

### What this gives us

One definition per packet, compile-time checked on both sides, and an explicit
compatibility check at join.

### What this costs

Serialization overhead compared to hand-tuned codecs, and every protocol change
requires a coordinated mod and server release.

### Follow-on work

The protocol module, a packet registry on both sides, the handshake, and rules for
bumping the protocol version.

### What this forecloses

Clients on a different protocol version cannot connect at all. Moving to another
transport later means migrating every packet.
