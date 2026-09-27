# Plan 0004: Protocol module, mod skeleton and handshake

- **Status:** Done
- **Date:** 2026-09-27
- **Accepted proposal:** Build the shared protocol module, packet registries on Paper and Fabric, the Fabric mod skeleton, and a configuration-phase version handshake with a mod whitelist
- **Decision records:** ADR-0012, ADR-0013, ADR-0027, ADR-0028, ADR-0029, ADR-0030

## Goal

The build contains two new top-level modules.

- **`surf-roleplay-protocol`** is pure Kotlin/JVM with no Minecraft dependency. It
  contains:
  - `PROTOCOL_VERSION`
  - a `Packet` marker
  - a packet type list that maps every packet class to its `roleplay:<name>`
    channel and its direction
  - a ProtoBuf codec
  - the packets `ClientHello(protocolVersion, modVersion, loadedMods)` and
    `HandshakeResult(accepted, reason)`
- **`surf-roleplay-fabric`** is a Fabric mod for Minecraft 26.2 with the mod id
  `surf-roleplay`. It requires fabric-api and fabric-language-kotlin, and it
  registers every packet type from the shared list.

The Paper plugin registers the same packet types. It holds every joining
connection in the configuration phase until a `ClientHello` arrives, and then:

| Case | Result |
| --- | --- |
| Correct protocol version and only whitelisted mods | The player continues into the world |
| No hello within the configured timeout | Disconnected with "Bitte starte das Spiel über den Roleplay-Launcher." |
| Protocol version mismatch | Disconnected with "Update über den Launcher" |
| Mods outside the whitelist | Disconnected with a message that lists them |

The whitelist is a list of mod ids in the Paper plugin config. The ids of
Minecraft, Java, the Fabric loader, fabric-api, fabric-language-kotlin and
`surf-roleplay` are always allowed.

`docs/protocol-versioning.md` states when `PROTOCOL_VERSION` is bumped.
`gradlew build` passes, and every new function, class and public property has a
KDoc comment.

## Out of scope

- The settings screen with rebindable keybinds (client-mod task 5).
- Hiding F3 coordinates and the Tab list, and disabling vanilla crafting (client-mod tasks 6 and 7).
- The screen framework for server-driven screens (client-mod task 8).
- A mod jar hash check.
- Storing the whitelist in the microservice or syncing it from the dashboard.
- The launcher and any release or CDN pipeline for the mod.
- Any gameplay packet beyond the two handshake packets.
- Automated tests against a running Minecraft client or server. The join checks are manual.

## Steps

### Step 1: Spike configuration-phase payloads on Paper 26.2

**Does:** Establishes how the Paper plugin can do two things with the Paper
version this build resolves:
- send and receive custom payloads for a connection in the configuration phase
- hold that connection open while it waits

It starts from the public API (`AsyncPlayerConnectionConfigureEvent`,
`PlayerConfigurationConnection`). If the public API cannot do both, it looks at a
server-internals hook through paperweight userdev or a Netty handler. The
findings go into a short note in the chat. No production code is committed in
this step.

**Ends in:** A named mechanism, with the classes and methods it uses, that can:
- receive a `roleplay:hello` payload during configuration
- send a payload back
- keep the connection in configuration until a decision is made

It also confirms whether the Paper module already has server internals on its
compile classpath.

**Verified by:** A throwaway compile of a minimal listener against the resolved
Paper artifacts, plus the class and method names quoted from the resolved jar.

**Pushes:** no

### Step 2: Create the protocol module

**Does:** Adds `surf-roleplay-protocol` to `settings.gradle.kts` with the Kotlin
JVM and serialization plugins. It depends on kotlinx-serialization-protobuf at a
version compatible with what fabric-language-kotlin ships for 26.2, and adds:
- `PROTOCOL_VERSION`
- `Packet`
- `PacketDirection`
- `PacketType`
- the `Packets` list
- `ProtocolCodec`
- `ClientHello`, `ModInfo`, `HandshakeResult`, with `@ProtoNumber` on every property

**Ends in:** A module that builds, with tests that cover:
- the round-trip encoding of both packets
- unique channel ids in the `roleplay` namespace
- rejection of an unknown channel on decode

**Verified by:** `./gradlew :surf-roleplay-protocol:test` passes.

**Pushes:** no

### Step 3: Register the packet types on Paper

**Does:** The Paper plugin depends on the protocol module and bundles it. It
registers the incoming and outgoing channel of every `PacketType` in `Packets`,
using the mechanism from step 1 for configuration-phase packets. It adds a
dispatcher that decodes incoming payloads and routes them to typed handlers.

**Ends in:** A Paper plugin jar that contains the protocol classes and a registry
that registers every channel in `Packets` at startup.

**Verified by:** `./gradlew :surf-roleplay-paper:build` passes. A local Paper 26.2
server started with the plugin logs the registered `roleplay:*` channels.

**Pushes:** no

### Step 4: Create the Fabric mod module

**Does:** Adds `surf-roleplay-fabric` to `settings.gradle.kts` with Fabric Loom
for Minecraft 26.2. It has:
- a `fabric.mod.json` with the id `surf-roleplay`, requiring `fabric-api` and
  `fabric-language-kotlin`
- a Kotlin client entrypoint
- a dependency on the protocol module, which is bundled into the mod jar

Kotlin and kotlinx come from fabric-language-kotlin and are not bundled.

**Ends in:** The mod jar builds in the same Gradle build as the paperweight
modules, and a Fabric 26.2 dev client starts with the mod loaded.

**Verified by:** `./gradlew build` passes for the whole project. The Loom client
run task starts the game, and the log shows `surf-roleplay` loaded.

**Pushes:** no

### Step 5: Register the packet types on Fabric

**Does:** Adds a generic payload bridge that turns each `PacketType` into a Fabric
payload type and codec. It registers every packet type in the configuration and
play phases, in its direction, and adds a client-side dispatcher.

**Ends in:** Every channel in `Packets` is registered with Fabric's payload
registry when the mod initialises.

**Verified by:** `./gradlew :surf-roleplay-fabric:build` passes. The dev client log
lists the registered `roleplay:*` channels at startup.

**Pushes:** no

### Step 6: Implement the version handshake

**Does:**
- **Fabric:** when the server registers `roleplay:hello` during configuration, the
  mod sends a `ClientHello` with `PROTOCOL_VERSION`, its mod version and its
  loaded mods.
- **Paper:** holds every configuring connection until a hello arrives or the
  configured timeout expires, then compares the protocol version and answers with
  a `HandshakeResult`.
- **Disconnects:** the German "missing mod" and "version mismatch" messages go
  through the existing German message style.
- **Config:** the timeout is added to the Paper plugin config.

**Ends in:** A modded client joins a local Paper server. A vanilla client and a
mod built with a different `PROTOCOL_VERSION` are disconnected in configuration,
before spawning, with the right German messages.

**Verified by:** Manual joins against a local Paper 26.2 server:
- with the mod
- with a vanilla 26.2 client
- with a mod built with `PROTOCOL_VERSION + 1`

Each outcome is observed.

**Pushes:** no

### Step 7: Enforce the mod whitelist

**Does:** Adds an `allowedMods` list to the Paper plugin config. The always-allowed
ids are Minecraft, Java, the Fabric loader, fabric-api, fabric-language-kotlin
and `surf-roleplay`. The handshake rejects any loaded mod outside the union of the
two, with a German message that lists the rejected ids. The check is a pure
function, with unit tests.

**Ends in:** The whitelist check is part of the handshake and covered by tests.

**Verified by:** `./gradlew :surf-roleplay-paper:test` passes. A manual join with
an extra non-whitelisted mod in the dev client is disconnected with the listing
message. A join after adding that mod id to the config succeeds.

**Pushes:** no

### Step 8: Document the protocol version rules

**Does:** Writes `docs/protocol-versioning.md`. It states which changes bump
`PROTOCOL_VERSION` and which ProtoBuf changes are compatible and do not:
- new packets
- removed packets
- renamed channels
- new, removed or renumbered fields
- changed field types

It also states how a bump is released.

**Ends in:** The document exists and matches the codec and the channel naming
rule in the code.

**Verified by:** Reading the document against `ProtocolCodec` and `Packets`.

**Pushes:** no

### Step 9: Verification

**Does:** Checks the whole goal end to end.

**Ends in:** Every statement in the goal has been observed to hold.

**Verified by:**
- `./gradlew build` passes from a clean state.
- Every new declaration has a KDoc comment with no ADR, plan or history reference.
- On a local Paper 26.2 server:
  - the modded client joins
  - the vanilla client is kicked with the launcher message
  - the wrong-version mod is kicked with the update message
  - a non-whitelisted mod is kicked with the listing message
  - a client that sends no hello is kicked after the timeout
- The same modded join and vanilla kick are repeated through a local Velocity
  proxy in front of the Paper server.

**Pushes:** no

## Push points

none

## Risk

Step 1 is the most likely to go wrong. Paper's public API does not appear to
expose custom payloads during the configuration phase. The fallback hooks into
server internals, and on Folia-based Canvas that may behave differently from
Paper. If no working mechanism exists, the agent stops before step 3:
- It sets the `protocol` system to Blocked and adds a roadmap question.
- It asks the human whether to move the handshake to the play phase. That would
  supersede ADR-0029.

## If reality contradicts this plan

The agent stops and asks. It does not silently rewrite this plan to match what
actually happened, and it does not continue past a step whose stated end state was
not reached.
