package dev.slne.surf.roleplay.protocol

import dev.slne.surf.roleplay.protocol.packets.ClientHello
import dev.slne.surf.roleplay.protocol.packets.HandshakeResult
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
     * Verifies that a rejecting handshake result with a reason survives encoding and decoding.
     */
    @Test
    fun `handshake result round-trips`() {
        val result = HandshakeResult(accepted = false, reason = "Update über den Launcher")

        val bytes = ProtocolCodec.encode(Packets.HANDSHAKE_RESULT, result)

        assertEquals(result, ProtocolCodec.decode(Packets.HANDSHAKE_RESULT.channel, bytes))
    }

    /**
     * Verifies that an accepting handshake result without a reason decodes with a null reason.
     */
    @Test
    fun `handshake result without reason decodes to null reason`() {
        val bytes = ProtocolCodec.encode(Packets.HANDSHAKE_RESULT, HandshakeResult(accepted = true))

        assertEquals(HandshakeResult(accepted = true, reason = null), ProtocolCodec.decode(Packets.HANDSHAKE_RESULT.channel, bytes))
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
