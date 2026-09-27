package dev.slne.surf.roleplay.paper.handshake

import dev.slne.surf.roleplay.protocol.PROTOCOL_VERSION
import dev.slne.surf.roleplay.protocol.packets.ClientHello
import dev.slne.surf.roleplay.protocol.packets.ModInfo
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [HandshakeEvaluator].
 */
class HandshakeEvaluatorTest {

    /**
     * The evaluator under test, which additionally allows Sodium.
     */
    private val evaluator = HandshakeEvaluator(allowedMods = setOf("sodium"))

    /**
     * Creates a hello with the server's protocol version and the given loaded mod ids.
     *
     * @param modIds the ids of the loaded mods
     * @return the hello
     */
    private fun helloWith(vararg modIds: String) =
        ClientHello(PROTOCOL_VERSION, "26.3.0", modIds.map { ModInfo(it, "1.0.0") })

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

    /**
     * Verifies that a client running only the mods that are always allowed is accepted.
     */
    @Test
    fun `always allowed mods are accepted`() {
        val hello = helloWith(*HandshakeEvaluator.ALWAYS_ALLOWED_MODS.toTypedArray())

        assertEquals(HandshakeOutcome.Accepted, evaluator.evaluate(hello))
    }

    /**
     * Verifies that a mod on the configured whitelist is accepted.
     */
    @Test
    fun `configured mod is accepted`() {
        assertEquals(HandshakeOutcome.Accepted, evaluator.evaluate(helloWith("surf-roleplay", "sodium")))
    }

    /**
     * Verifies that mods outside the whitelist are rejected and listed sorted by id.
     */
    @Test
    fun `mods outside the whitelist are rejected sorted by id`() {
        val hello = helloWith("surf-roleplay", "xaerominimap", "sodium", "freecam")

        assertEquals(HandshakeOutcome.ForbiddenMods(listOf("freecam", "xaerominimap")), evaluator.evaluate(hello))
    }

    /**
     * Verifies that a protocol mismatch is reported even when forbidden mods are loaded too.
     */
    @Test
    fun `protocol mismatch takes precedence over forbidden mods`() {
        val hello = ClientHello(PROTOCOL_VERSION + 1, "26.3.0", listOf(ModInfo("freecam", "1.0.0")))

        assertEquals(
            HandshakeOutcome.ProtocolMismatch(clientVersion = PROTOCOL_VERSION + 1, serverVersion = PROTOCOL_VERSION),
            evaluator.evaluate(hello),
        )
    }
}
