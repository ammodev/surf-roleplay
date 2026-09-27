package dev.slne.surf.roleplay.paper.handshake

import dev.slne.surf.roleplay.protocol.PROTOCOL_VERSION
import dev.slne.surf.roleplay.protocol.packets.ClientHello
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [HandshakeEvaluator].
 */
class HandshakeEvaluatorTest {

    /**
     * The evaluator under test.
     */
    private val evaluator = HandshakeEvaluator()

    /**
     * Verifies that a missing hello rejects the client as not running the mod.
     */
    @Test
    fun `missing hello is rejected as missing mod`() {
        assertEquals(HandshakeOutcome.MissingMod, evaluator.evaluate(null))
    }

    /**
     * Verifies that a hello with the server's protocol version is accepted.
     */
    @Test
    fun `matching protocol version is accepted`() {
        val hello = ClientHello(PROTOCOL_VERSION, "26.3.0")

        assertEquals(HandshakeOutcome.Accepted, evaluator.evaluate(hello))
    }

    /**
     * Verifies that a hello with a newer protocol version is rejected with both versions.
     */
    @Test
    fun `newer protocol version is rejected as mismatch`() {
        val hello = ClientHello(PROTOCOL_VERSION + 1, "26.3.0")

        assertEquals(
            HandshakeOutcome.ProtocolMismatch(clientVersion = PROTOCOL_VERSION + 1, serverVersion = PROTOCOL_VERSION),
            evaluator.evaluate(hello),
        )
    }

    /**
     * Verifies that a hello with an older protocol version is rejected with both versions.
     */
    @Test
    fun `older protocol version is rejected as mismatch`() {
        val hello = ClientHello(PROTOCOL_VERSION - 1, "26.2.0")

        assertEquals(
            HandshakeOutcome.ProtocolMismatch(clientVersion = PROTOCOL_VERSION - 1, serverVersion = PROTOCOL_VERSION),
            evaluator.evaluate(hello),
        )
    }
}
