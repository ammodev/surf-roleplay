package dev.slne.surf.roleplay.paper.screen.debug

import dev.slne.surf.roleplay.api.client.common.screen.ScreenThemes
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.toast.Toast
import dev.slne.surf.roleplay.api.client.common.toast.ToastType
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
 * Tests for the overlays page of the screen debug command.
 */
class OverlaysDemoTest {

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
     * The toasts the page sent.
     */
    private val toasts = mutableListOf<Toast>()

    /**
     * The tasks the page scheduled.
     */
    private val later = mutableListOf<() -> Unit>()

    /**
     * The reports the page sent to chat.
     */
    private val reports = mutableListOf<Component>()

    /**
     * Opens the page.
     *
     * @return the session id
     */
    private fun open(): Int = state.open(
        OverlaysDemo.definition(OverlaysDemo.Hooks({ reports += it }, { toasts += it; it.id ?: "t" }, { _, task -> later += task }, { _, _ -> }), ScreenThemes.DEFAULT, ScreenVariant.DARK),
        null,
    ).sessionId

    /**
     * Verifies that the page builds with unique ids and maps to a screen open.
     */
    @Test
    fun `the overlays page builds and maps`() {
        open()

        assertIs<ScreenOpen>(sent.single())
    }

    /**
     * Verifies that a menu item reports in chat.
     */
    @Test
    fun `menu items report`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "menu_profile")))
        assertEquals(1, reports.size)
    }

    /**
     * Verifies that the toast buttons send toasts, and that the loading toast turns into a
     * success with the same id.
     */
    @Test
    fun `toast buttons send toasts`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "toast_success")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "toast_loading")))
        later.single()()

        assertEquals(listOf(ToastType.SUCCESS, ToastType.LOADING, ToastType.SUCCESS), toasts.map { it.type })
        assertEquals(toasts[1].id, toasts[2].id)
    }
}
