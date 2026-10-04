package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ScreenInputChange
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import dev.slne.surf.roleplay.protocol.screen.ScreenInputChange as ScreenInputChangePacket

/**
 * Tests for input change events.
 */
class InputChangeTest {

    /**
     * The packets sent to the client.
     */
    private val sent = mutableListOf<Packet>()

    /**
     * The state under test, with a limit of three actions per second.
     */
    private val state = PlayerScreenState(
        UUID.randomUUID(),
        object : ScreenPacketSender {
            /**
             * Records a packet.
             *
             * @param type the packet type
             * @param packet the packet
             */
            override fun <P : Packet> send(type: PacketType<P>, packet: P) {
                sent += packet
            }
        },
        ActionRateLimiter(3),
    )

    /**
     * The changes the handler received.
     */
    private val changes = mutableListOf<ScreenInputChange>()

    /**
     * Opens a screen with a watched search input, an unwatched name input and a checkbox.
     *
     * @return the session id
     */
    private fun open(): Int = state.open(
        Screen(Component.text("Suche")) {
            Column(id = "root") {
                Input(maxLength = 10, onChange = { changes += it }, id = "search")
                Input(id = "name")
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that inputs with a change handler are marked for change events on the wire.
     */
    @Test
    fun `watched inputs notify their changes`() {
        open()

        val children = assertIs<ColumnNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root).children
        assertEquals(true, assertIs<TextInputNode>(children[0]).notifyChange)
        assertEquals(false, assertIs<TextInputNode>(children[1]).notifyChange)
    }

    /**
     * Verifies that a valid change runs the handler with the new value and stores it.
     */
    @Test
    fun `valid changes run the handler`() {
        val session = open()

        val outcome = state.handleInputChange(ScreenInputChangePacket(session, "search", "Max"))

        assertIs<PlayerScreenState.Outcome.Accepted>(outcome)
        assertEquals("Max", changes.single().value)
        assertEquals("search", changes.single().inputId)
        assertEquals("Max", changes.single().values.text("search"))
    }

    /**
     * Verifies that changes for unknown sessions, unknown or unwatched inputs and invalid values are
     * rejected without running the handler.
     */
    @Test
    fun `invalid changes are rejected`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChangePacket(99, "search", "x")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChangePacket(session, "missing", "x")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChangePacket(session, "name", "x")))
        assertIs<PlayerScreenState.Outcome.Rejected>(state.handleInputChange(ScreenInputChangePacket(session, "search", "viel zu lang")))
        assertTrue(changes.isEmpty())
    }

    /**
     * Verifies that change events count against the rate limit.
     */
    @Test
    fun `changes count against the rate limit`() {
        val session = open()

        val outcomes = List(4) { state.handleInputChange(ScreenInputChangePacket(session, "search", "a$it")) }

        assertEquals(3, outcomes.count { it is PlayerScreenState.Outcome.Accepted })
    }
}
