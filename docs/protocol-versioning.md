# Protocol versioning

The server and the client mod share one protocol, defined in `surf-roleplay-protocol`. Its
version is `PROTOCOL_VERSION` in `ProtocolVersion.kt`. During the configuration phase the mod
sends its version in `ClientHello`. If the version differs from the server's, the server
disconnects the player with an update notice. Two builds with the same `PROTOCOL_VERSION` must
understand every packet the other sends.

## How packets are encoded

- Every packet type is listed in `Packets.all` with a `PacketType`: a snake_case name, a
  direction and the connection phases it may be sent in.
- Each packet type travels on its own payload channel `roleplay:<name>`.
- The payload body is the packet's kotlinx.serialization ProtoBuf encoding, with no extra
  framing.
- Every property of a packet class carries an explicit `@ProtoNumber`.
- A sealed type, such as a screen node or a patch operation, is encoded with its subclass's
  `@SerialName` and the subclass's own encoding. Every subclass carries an explicit `@SerialName`.
- Every enum constant carries an explicit `@ProtoNumber`.
- A typed screen's state and actions are ProtoBuf bytes inside screen packets. Their classes are
  defined with their `ScreenType` in the protocol module and follow the same rules as packets.
- Decoders skip fields whose number they do not know. A field that a reader expects and does not
  find takes its default value. If it has no default, decoding fails.

## Changes that bump `PROTOCOL_VERSION`

Bump the version for any of these:

- adding, removing or renaming a packet type, which changes the set of channels
- changing a packet type's direction or connection phases
- adding a field without a default value
- removing a field that has no default value
- changing a field's `@ProtoNumber`
- changing a field's type, or whether it is nullable or a list
- reusing a field number that was released with a different meaning
- changing what an existing field means, even if its type stays the same
- adding, removing or renaming a screen type, a screen node kind or a patch operation kind, or
  changing a `@SerialName` of a sealed subclass
- adding an enum constant, or changing an enum constant's `@ProtoNumber`
- updating the pinned Lucide release if it renames or removes an icon, because icon names in
  screen packets refer to that release

## Changes that keep the version

- adding a field with a new, never-used `@ProtoNumber` and a default value
- removing a field that has a default value. Its number is retired and never reused.
- renaming a Kotlin property or class without changing its `@ProtoNumber`, type, `@SerialName` or
  packet name
- changing code that does not affect the bytes on the wire, such as handlers, validation or logging

If in doubt, bump. A needless bump forces an update. A missing bump lets incompatible builds talk
to each other.

## Before the first release

Until a protocol version has been released to players, `PROTOCOL_VERSION` stays at 1 and
incompatible changes do not bump it. Development builds of the mod and the server must then be
built from the same commit. The rules above apply from the first release on.

## Releasing a bump

1. Increment `PROTOCOL_VERSION` by one in the same commit as the incompatible change.
2. Release the Paper plugin and the client mod from the same commit.
3. Publish the new mod through the launcher before the server update goes live. Players with the
   old mod are then disconnected with the update notice until the launcher updates them.
