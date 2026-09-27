package dev.slne.surf.roleplay.protocol

import dev.slne.surf.roleplay.protocol.packets.ClientHello
import dev.slne.surf.roleplay.protocol.packets.ModInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for [ProtocolCodec].
 */
class ProtocolCodecTest {

    /**
     * Verifies that a client hello with loaded mods survives encoding and decoding unchanged.
     */
    @Test
    fun `client hello round-trips`() {
        val hello = ClientHello(
            protocolVersion = PROTOCOL_VERSION,
            modVersion = "26.3.0",
            loadedMods = listOf(ModInfo("fabric-api", "0.161.0+26.2"), ModInfo("sodium", "0.7.0")),
        )

        val bytes = ProtocolCodec.encode(Packets.HELLO, hello)

        assertEquals(hello, ProtocolCodec.decode(Packets.HELLO.channel, bytes))
    }

    /**
     * Verifies that a client hello without loaded mods decodes with an empty mod list.
     */
    @Test
    fun `client hello without mods decodes to empty mod list`() {
        val bytes = ProtocolCodec.encode(Packets.HELLO, ClientHello(PROTOCOL_VERSION, "26.3.0"))

        assertEquals(emptyList(), (ProtocolCodec.decode(Packets.HELLO.channel, bytes) as ClientHello).loadedMods)
    }

    /**
     * Verifies that decoding a payload from a channel that no packet type uses fails.
     */
    @Test
    fun `decoding an unknown channel fails`() {
        assertFailsWith<UnknownPacketChannelException> {
            ProtocolCodec.decode("roleplay:does_not_exist", byteArrayOf())
        }
    }
}
