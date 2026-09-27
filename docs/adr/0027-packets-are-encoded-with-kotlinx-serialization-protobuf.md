# ADR-0027: Packets are encoded with kotlinx.serialization ProtoBuf

- **Status:** Accepted
- **Date:** 2026-09-27
- **Supersedes:** none
- **Superseded by:** none

## Context

Server and mod exchange kotlinx.serialization packets (ADR-0013), but
kotlinx.serialization supports several formats. The packet set will include
high-rate traffic such as vehicle inputs and corrections (ADR-0021), and mod and
server versions can differ briefly during an update rollout. The wire format has
to be chosen before the first packet is defined, because every packet carries it.

## Decision

Every packet body is encoded with the kotlinx.serialization ProtoBuf format.
Every property of a packet class carries an explicit `@ProtoNumber`. A field
number is never reused for a different meaning once released.

## Alternatives considered

### CBOR

Its advantage: compact binary without field numbering, so packet classes need no
extra annotations.

It was rejected because it encodes field names into every message, which makes
packets noticeably larger than ProtoBuf on high-rate channels.

### JSON

Its advantage: human-readable payloads that are trivial to log and debug, with the
most mature kotlinx.serialization support.

It was rejected because it is the largest and slowest to encode of the three,
which matters for vehicle and HUD traffic at 150+ players.

## Consequences

### What this gives us

Small packets, and optional fields can be added with new numbers without breaking
readers that do not know them yet.

### What this costs

- The kotlinx ProtoBuf format is marked experimental and needs an opt-in.
- Every packet property needs a hand-assigned `@ProtoNumber`, and reviewers must
  check that numbers are not reused.
- Payloads are not readable without decoding them.

### Follow-on work

Protocol version bump rules that state which ProtoBuf changes are compatible, and
a debug tool that decodes packets for logging.

### What this forecloses

Switching formats later changes every packet on the wire and needs a protocol
version bump with a coordinated mod and server release.
