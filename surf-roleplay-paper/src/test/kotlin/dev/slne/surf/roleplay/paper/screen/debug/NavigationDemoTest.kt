package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.paper.screen.ActionRateLimiter
import dev.slne.surf.roleplay.paper.screen.PlayerScreenState
import dev.slne.surf.roleplay.paper.screen.ScreenPacketSender
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.ScreenInputChange
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for the navigation demo page.
 */
class NavigationDemoTest {

    /**
     * The packets sent to the client.
     */
    private val sent = mutableListOf<Packet>()

    /**
     * The state under test.
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
     * The reports the page sent to chat, as plain text.
     */
    private val reports = mutableListOf<String>()

    /**
     * Opens the page and returns its session id.
     *
     * @return the session id
     */
    private fun open(): Int {
        val hooks = NavigationDemo.Hooks(
            report = { reports += PlainTextComponentSerializer.plainText().serialize(it) },
            reopen = { _, _, _ -> },
        )
        val session = state.open(NavigationDemo.definition(hooks, ScreenThemes.DEFAULT, ScreenVariant.DARK, NavigationDemo.SidebarSetup()), null).sessionId
        assertIs<ScreenOpen>(sent.last())
        return session
    }

    /**
     * Verifies that breadcrumb, pagination, navigation menu and sidebar links reach the page and
     * are reported in chat.
     */
    @Test
    fun `links report in chat`() {
        val session = open()

        listOf("breadcrumb_home", "pagination_2", "navigation_docs", "sidebar_home", "direction_prev").forEach { id ->
            assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, id)))
        }
        assertEquals(
            listOf("breadcrumb_home geklickt", "pagination_2 geklickt", "navigation_docs geklickt", "sidebar_home geklickt", "direction_prev geklickt"),
            reports,
        )
    }

    /**
     * Verifies that tab and accordion changes are reported in chat.
     */
    @Test
    fun `tab and accordion changes report in chat`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChange(session, "tabs_default", "report")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChange(session, "accordion_single", "returns")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChange(session, "accordion_multiple", "a,b")))
        assertEquals(listOf("Tab tabs_default: report", "Accordion einzeln: returns", "Accordion mehrere: a,b"), reports)
    }
}
