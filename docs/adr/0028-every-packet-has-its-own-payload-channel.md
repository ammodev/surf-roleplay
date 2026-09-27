# ADR-0028: Every packet has its own payload channel

- **Status:** Accepted
- **Date:** 2026-09-27
- **Supersedes:** none
- **Superseded by:** none

## Context

Packets travel over custom payload channels in the `roleplay` namespace
(ADR-0013). The mapping of packets to channels is still open: a channel can carry
one packet type, or several packet types behind a project-defined packet id.
Fabric models custom payloads as one payload type per channel id, and Paper
registers plugin channels by name.

## Decision

Every packet type has its own channel, named `roleplay:<packet_name>` in
snake_case. The protocol module holds the list of packet types with their channel
id and direction, and both the Paper plugin and the mod register every channel
from that list.

## Alternatives considered

### One multiplexed channel

Its advantage: a single channel registration on both sides, with the packet id
table fully under the project's control inside the protocol module.

It was rejected because it hides all traffic behind one opaque channel and works
against Fabric's one-payload-type-per-id model, so the mod would need its own
dispatch layer on top of Fabric's.

### One multiplexed channel per domain

Its advantage: traffic can be split and inspected by subsystem (core, UI, vehicle)
while keeping the number of registrations small.

It was rejected because it still needs a packet id layer inside each channel and
leaves open, for every new packet, which domain it belongs to.

## Consequences

### What this gives us

Fabric's payload registry and Paper's channel registry do the dispatching, and
each packet is visible by name in network debugging tools.

### What this costs

- Many channel registrations on both sides, which grows with the packet count.
- The client announces a long list of channels to the server on connect.

### Follow-on work

A generic payload bridge on both sides that registers every packet type from the
shared list, so no packet needs hand-written registration code.

### What this forecloses

Changing to multiplexed channels later renames every channel and needs a protocol
version bump.
