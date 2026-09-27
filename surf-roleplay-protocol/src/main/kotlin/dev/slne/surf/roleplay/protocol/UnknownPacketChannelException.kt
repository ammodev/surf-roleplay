package dev.slne.surf.roleplay.protocol

/**
 * Thrown when a payload arrives on a channel that no [PacketType] in [Packets] uses.
 *
 * @property channel the channel id of the payload
 */
class UnknownPacketChannelException(val channel: String) :
    IllegalArgumentException("No packet type uses channel '$channel'")
