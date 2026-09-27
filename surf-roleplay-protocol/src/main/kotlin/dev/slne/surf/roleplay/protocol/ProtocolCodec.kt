package dev.slne.surf.roleplay.protocol

import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * Encodes packets into payload bytes and decodes payload bytes back into packets, using the
 * ProtoBuf format.
 */
object ProtocolCodec {
    /**
     * The ProtoBuf format used for every packet body.
     */
    private val format: ProtoBuf = ProtoBuf

    /**
     * Encodes a packet into the bytes of its payload.
     *
     * @param type the packet type of [packet]
     * @param packet the packet to encode
     * @return the payload bytes
     */
    fun <P : Packet> encode(type: PacketType<P>, packet: P): ByteArray =
        format.encodeToByteArray(type.serializer, packet)

    /**
     * Decodes the payload bytes received on a channel into a packet.
     *
     * @param channel the channel id the payload arrived on
     * @param bytes the payload bytes
     * @return the decoded packet
     * @throws UnknownPacketChannelException if no packet type uses [channel]
     * @throws SerializationException if the bytes are not a valid packet of that type
     */
    fun decode(channel: String, bytes: ByteArray): Packet {
        val type = Packets.byChannel(channel) ?: throw UnknownPacketChannelException(channel)
        return decode(type, bytes)
    }

    /**
     * Decodes payload bytes into a packet of a known type.
     *
     * @param type the packet type the bytes encode
     * @param bytes the payload bytes
     * @return the decoded packet
     * @throws SerializationException if the bytes are not a valid packet of that type
     */
    fun <P : Packet> decode(type: PacketType<P>, bytes: ByteArray): P =
        format.decodeFromByteArray(type.serializer, bytes)
}
