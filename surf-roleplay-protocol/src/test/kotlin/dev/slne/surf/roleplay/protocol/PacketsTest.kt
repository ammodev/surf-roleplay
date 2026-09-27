package dev.slne.surf.roleplay.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for [Packets] and [PacketType].
 */
class PacketsTest {

    /**
     * Verifies that no two packet types share a channel.
     */
    @Test
    fun `channels are unique`() {
        val channels = Packets.all.map { it.channel }

        assertEquals(channels.size, channels.toSet().size)
    }

    /**
     * Verifies that every channel is in the roleplay namespace and uses a snake_case name.
     */
    @Test
    fun `channels are snake_case in the roleplay namespace`() {
        for (type in Packets.all) {
            assertTrue(Regex("roleplay:[a-z0-9_]+").matches(type.channel), type.channel)
        }
    }

    /**
     * Verifies that every packet type can be looked up by its own channel.
     */
    @Test
    fun `byChannel resolves every packet type`() {
        for (type in Packets.all) {
            assertSame(type, Packets.byChannel(type.channel))
        }
    }

    /**
     * Verifies that the hello uses its channel and is sent by the client in the configuration phase.
     */
    @Test
    fun `hello uses its channel, direction and phase`() {
        assertEquals("roleplay:hello", Packets.HELLO.channel)
        assertEquals(PacketDirection.SERVERBOUND, Packets.HELLO.direction)
        assertEquals(setOf(ConnectionPhase.CONFIGURATION), Packets.HELLO.phases)
    }

    /**
     * Verifies that a packet type name outside snake_case is rejected.
     */
    @Test
    fun `packet type rejects names that are not snake_case`() {
        assertFailsWith<IllegalArgumentException> {
            PacketType(
                "Hello",
                PacketDirection.SERVERBOUND,
                setOf(ConnectionPhase.CONFIGURATION),
                dev.slne.surf.roleplay.protocol.packets.ClientHello.serializer(),
            )
        }
    }
}
