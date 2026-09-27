package dev.slne.surf.roleplay.paper.protocol

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketDirection
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import io.papermc.paper.connection.PlayerConnection
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.util.concurrent.ConcurrentHashMap

private val log = logger()

/**
 * A handler for one kind of packet received from a client.
 *
 * @param P the packet class
 */
fun interface PacketHandler<P : Packet> {
    /**
     * Handles a packet received from a client.
     *
     * @param connection the connection the packet arrived on, in the configuration or play phase
     * @param packet the decoded packet
     */
    fun handle(connection: PlayerConnection, packet: P)
}

/**
 * Decodes serverbound roleplay payloads and routes each packet to the handler registered for
 * its packet type.
 *
 * Payloads on a channel without a handler, and payloads that cannot be decoded, are logged and
 * dropped.
 */
class PaperPacketDispatcher : PluginMessageListener {

    /**
     * The registered handlers keyed by the channel of their packet type.
     */
    private val handlers = ConcurrentHashMap<String, PacketHandler<*>>()

    /**
     * Registers the handler for a serverbound packet type, replacing any earlier handler.
     *
     * @param type the packet type to handle
     * @param handler the handler that receives every decoded packet of [type]
     * @throws IllegalArgumentException if [type] is not serverbound
     */
    fun <P : Packet> on(type: PacketType<P>, handler: PacketHandler<P>) {
        require(type.direction == PacketDirection.SERVERBOUND) { "$type is not serverbound" }
        handlers[type.channel] = handler
    }

    /**
     * Ignores the joined-player variant of an incoming payload, because every payload is also
     * delivered to the connection variant.
     *
     * @param channel the channel the payload arrived on
     * @param player the player who sent it
     * @param message the payload bytes
     */
    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) = Unit

    /**
     * Decodes a payload received from a configuring or joined connection and passes the packet
     * to the handler registered for its channel.
     *
     * @param channel the channel the payload arrived on
     * @param connection the connection that sent it
     * @param message the payload bytes
     */
    override fun onPluginMessageReceived(channel: String, connection: PlayerConnection, message: ByteArray) {
        val type = Packets.byChannel(channel) ?: return
        val handler = handlers[channel]
        if (handler == null) {
            log.atFine().log("Dropped %s from %s: no handler", channel, connection.address)
            return
        }

        val packet = try {
            ProtocolCodec.decode(type, message)
        } catch (exception: Exception) {
            log.atWarning().withCause(exception).log(
                "Dropped undecodable %s from %s", channel, connection.address
            )
            return
        }

        dispatch(handler, connection, packet)
    }

    /**
     * Passes a decoded packet to its handler.
     *
     * @param handler the handler registered for the packet's channel
     * @param connection the connection the packet arrived on
     * @param packet the decoded packet, whose type matches the handler's
     */
    @Suppress("UNCHECKED_CAST")
    private fun <P : Packet> dispatch(handler: PacketHandler<P>, connection: PlayerConnection, packet: Packet) {
        handler.handle(connection, packet as P)
    }
}
