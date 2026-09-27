# ADR-0030: The client mod depends on Fabric Language Kotlin

- **Status:** Accepted
- **Date:** 2026-09-27
- **Supersedes:** none
- **Superseded by:** none

## Context

The client mod is written in Kotlin and uses kotlinx.serialization through the
shared protocol module (ADR-0013). A Fabric mod written in Kotlin needs the
Kotlin standard library and kotlinx libraries at runtime. They can come from the
Fabric Language Kotlin mod or be bundled into the mod jar.

## Decision

The client mod declares Fabric API and Fabric Language Kotlin as required runtime
dependencies. It uses the Kotlin stdlib, coroutines and kotlinx.serialization that
Fabric Language Kotlin provides, and does not bundle them. The launcher ships both
dependencies with the mod.

## Alternatives considered

### Bundle Kotlin into the mod jar

Its advantage: the mod has no runtime dependency besides Fabric API, and the
Kotlin and kotlinx.serialization versions are exactly the ones the project
compiles against.

It was rejected because it makes the mod jar much larger and can clash with other
Kotlin mods that load their own copy of the stdlib.

## Consequences

### What this gives us

A small mod jar and one shared Kotlin runtime for every Kotlin mod the player
runs.

### What this costs

- The Kotlin and kotlinx.serialization versions available at runtime are the ones
  Fabric Language Kotlin ships, so the protocol module must compile against
  versions it provides.
- A Minecraft update can wait on a Fabric Language Kotlin release.

### Follow-on work

The launcher manifest includes Fabric API and Fabric Language Kotlin, and both
are implicitly on the mod whitelist.

### What this forecloses

Using Kotlin or kotlinx versions newer than Fabric Language Kotlin ships, unless
the mod starts bundling them.
