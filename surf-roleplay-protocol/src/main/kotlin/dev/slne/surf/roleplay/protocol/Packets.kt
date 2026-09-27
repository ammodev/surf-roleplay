package dev.slne.surf.roleplay.protocol

import dev.slne.surf.roleplay.protocol.packets.ClientHello
import dev.slne.surf.roleplay.protocol.packets.HandshakeResult

/**
 * The registry of every [PacketType] of the roleplay protocol.
 *
 * Both the server and the client mod register exactly the packet types in [all].
 */
object Packets {
    /**
     * The hello the client mod sends in the configuration phase to start the handshake.
     */
    val HELLO: PacketType<ClientHello> = PacketType(
        "hello",
        PacketDirection.SERVERBOUND,
        setOf(ConnectionPhase.CONFIGURATION),
        ClientHello.serializer(),
    )

    /**
     * The server's answer to a [ClientHello].
     */
    val HANDSHAKE_RESULT: PacketType<HandshakeResult> = PacketType(
        "handshake_result",
        PacketDirection.CLIENTBOUND,
        setOf(ConnectionPhase.CONFIGURATION),
        HandshakeResult.serializer(),
    )

    /**
     * Every packet type of the protocol.
     */
    val all: List<PacketType<*>> = listOf(HELLO, HANDSHAKE_RESULT)

    /**
     * The packet types keyed by their channel id.
     */
    private val typesByChannel: Map<String, PacketType<*>> = all.associateBy { it.channel }

    init {
        check(typesByChannel.size == all.size) { "Two packet types share a channel" }
    }

    /**
     * Looks up the packet type that travels on a channel.
     *
     * @param channel the payload channel id, such as `roleplay:hello`
     * @return the packet type, or `null` if no packet type uses the channel
     */
    fun byChannel(channel: String): PacketType<*>? = typesByChannel[channel]
}
