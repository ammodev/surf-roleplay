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
     * Verifies that the handshake packets use the expected channels and directions.
     */
    @Test
    fun `handshake packets use their channels and directions`() {
        assertEquals("roleplay:hello", Packets.HELLO.channel)
        assertEquals(PacketDirection.SERVERBOUND, Packets.HELLO.direction)
        assertEquals("roleplay:handshake_result", Packets.HANDSHAKE_RESULT.channel)
        assertEquals(PacketDirection.CLIENTBOUND, Packets.HANDSHAKE_RESULT.direction)
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
