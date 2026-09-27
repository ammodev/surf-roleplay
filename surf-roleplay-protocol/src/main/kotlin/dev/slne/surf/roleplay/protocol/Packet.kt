package dev.slne.surf.roleplay.protocol

/**
 * A message of the roleplay protocol.
 *
 * Every implementation is a `@Serializable` class whose properties carry an explicit
 * `@ProtoNumber`, and is listed with its [PacketType] in [Packets].
 */
interface Packet
