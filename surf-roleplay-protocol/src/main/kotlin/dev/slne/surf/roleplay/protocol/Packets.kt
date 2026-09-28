package dev.slne.surf.roleplay.protocol

import dev.slne.surf.roleplay.protocol.packets.ClientHello
import dev.slne.surf.roleplay.protocol.packets.Welcome

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
     * The welcome the server sends once the player is in the world, which marks the server as the
     * roleplay server for the client mod.
     */
    val WELCOME: PacketType<Welcome> = PacketType(
        "welcome",
        PacketDirection.CLIENTBOUND,
        setOf(ConnectionPhase.PLAY),
        Welcome.serializer(),
    )

    /**
     * Every packet type of the protocol.
     */
    val all: List<PacketType<*>> = listOf(HELLO, WELCOME)

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
