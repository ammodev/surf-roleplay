package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType

/**
 * A [ScreenPacketSender] that holds packets until the player's client is ready to receive screen
 * packets, and then delivers them in order.
 *
 * @property target the sender that delivers packets to the client
 * @param ready whether the client can already receive screen packets
 */
class QueueingScreenSender(private val target: ScreenPacketSender, ready: Boolean) : ScreenPacketSender {

    /**
     * The packets held until the client is ready, oldest first, or `null` once it is ready.
     */
    private var queue: MutableList<() -> Unit>? = if (ready) null else mutableListOf()

    /**
     * Delivers a packet, or holds it until the client is ready.
     *
     * @param type the packet type
     * @param packet the packet
     */
    override fun <P : Packet> send(type: PacketType<P>, packet: P) {
        val pending = queue
        if (pending == null) target.send(type, packet) else pending += { target.send(type, packet) }
    }

    /**
     * Marks the client as ready and delivers the held packets in order. Does nothing if the
     * client is already ready.
     */
    fun markReady() {
        val pending = queue ?: return
        queue = null
        pending.forEach { it() }
    }
}
