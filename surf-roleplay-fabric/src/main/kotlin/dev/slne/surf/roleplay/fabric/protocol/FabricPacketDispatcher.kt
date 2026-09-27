package dev.slne.surf.roleplay.fabric.protocol

import dev.slne.surf.roleplay.fabric.RoleplayClient
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketDirection
import dev.slne.surf.roleplay.protocol.PacketType
import java.util.concurrent.ConcurrentHashMap

/**
 * Routes clientbound packets received from the server to the handler registered for their
 * packet type.
 *
 * Packets without a handler are logged and dropped.
 */
object FabricPacketDispatcher {

    /**
     * The registered handlers keyed by the channel of their packet type.
     */
    private val handlers = ConcurrentHashMap<String, (Packet) -> Unit>()

    /**
     * Registers the handler for a clientbound packet type, replacing any earlier handler.
     *
     * @param type the packet type to handle
     * @param handler the handler that receives every packet of [type]
     * @throws IllegalArgumentException if [type] is not clientbound
     */
    @Suppress("UNCHECKED_CAST")
    fun <P : Packet> on(type: PacketType<P>, handler: (P) -> Unit) {
        require(type.direction == PacketDirection.CLIENTBOUND) { "$type is not clientbound" }
        handlers[type.channel] = handler as (Packet) -> Unit
    }

    /**
     * Passes a received payload's packet to the handler registered for its packet type.
     *
     * @param payload the received payload
     */
    fun dispatch(payload: RoleplayPayload<*>) {
        val handler = handlers[payload.packetType.channel]
        if (handler == null) {
            RoleplayClient.log.debug("Dropped {}: no handler", payload.packetType.channel)
            return
        }
        handler(payload.packet)
    }
}
