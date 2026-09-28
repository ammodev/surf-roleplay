package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.paper.screen.ActionRateLimiter
import dev.slne.surf.roleplay.paper.screen.PlayerScreenState
import dev.slne.surf.roleplay.paper.screen.ScreenPacketSender
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for the display page of the screen debug command.
 */
class DisplayDemoTest {

    /**
     * The packets sent to the client.
     */
    private val sent = mutableListOf<Packet>()

    /**
     * The state the page is opened in.
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
        ActionRateLimiter(100),
    )

    /**
     * Verifies that the page builds with unique ids and maps to a screen open.
     */
    @Test
    fun `the display page builds and maps`() {
        state.open(DisplayDemo.definition(UUID.randomUUID(), { }, { _, _ -> }, ScreenThemes.DEFAULT, ScreenVariant.DARK), null)

        assertIs<ScreenOpen>(sent.single())
    }

    /**
     * Verifies that a click on the clickable item is reported in chat.
     */
    @Test
    fun `item clicks are reported`() {
        val reports = mutableListOf<Component>()
        val session = state.open(DisplayDemo.definition(UUID.randomUUID(), { reports += it }, { _, _ -> }, ScreenThemes.DEFAULT, ScreenVariant.DARK), null).sessionId

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "item_click")))
        assertEquals(1, reports.size)
    }
}
