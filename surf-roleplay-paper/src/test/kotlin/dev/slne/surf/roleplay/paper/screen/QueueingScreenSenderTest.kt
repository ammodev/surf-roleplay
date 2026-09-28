package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.screen.ScreenClose
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [QueueingScreenSender].
 */
class QueueingScreenSenderTest {

    /**
     * The packets that reached the underlying sender, in order.
     */
    private val delivered = mutableListOf<Packet>()

    /**
     * An underlying sender that records packets.
     */
    private val target = object : ScreenPacketSender {
        /**
         * Records a packet.
         *
         * @param type the packet type
         * @param packet the packet
         */
        override fun <P : Packet> send(type: PacketType<P>, packet: P) {
            delivered += packet
        }
    }

    /**
     * Verifies that packets sent before the client is ready are held and then delivered in order.
     */
    @Test
    fun `packets wait until the client is ready`() {
        val sender = QueueingScreenSender(target, ready = false)

        sender.send(Packets.SCREEN_CLOSE, ScreenClose(1))
        sender.send(Packets.SCREEN_CLOSE, ScreenClose(2))
        assertTrue(delivered.isEmpty())

        sender.markReady()
        sender.send(Packets.SCREEN_CLOSE, ScreenClose(3))

        assertEquals(listOf<Packet>(ScreenClose(1), ScreenClose(2), ScreenClose(3)), delivered)
    }

    /**
     * Verifies that a sender that starts ready delivers at once, and that marking it ready again
     * repeats nothing.
     */
    @Test
    fun `ready senders deliver at once`() {
        val sender = QueueingScreenSender(target, ready = true)

        sender.send(Packets.SCREEN_CLOSE, ScreenClose(1))
        sender.markReady()

        assertEquals(listOf<Packet>(ScreenClose(1)), delivered)
    }
}
