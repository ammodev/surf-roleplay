package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ScreenPresentation
import dev.slne.surf.roleplay.api.client.common.screen.ScreenVariant
import dev.slne.surf.roleplay.api.client.common.screen.SheetSide
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.Presentation
import dev.slne.surf.roleplay.protocol.screen.ScreenClosed
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.SheetEdge
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for presentations, themes and [ConfirmDialog].
 */
class ConfirmDialogTest {

    /**
     * The packets sent to the client, in order.
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
     * The outcomes the confirmation reported, in order.
     */
    private val outcomes = mutableListOf<String>()

    /**
     * Opens a parent screen in the police theme.
     *
     * @return the parent's session id
     */
    private fun openParent(): Int = state.open(
        Screen(Component.text("Akte"), theme = "police", variant = ScreenVariant.LIGHT) {
            Label(Component.empty(), id = "a")
        },
        null,
    ).sessionId

    /**
     * Opens a confirmation over a parent.
     *
     * @param parent the parent session
     * @return the dialog's session id
     */
    private fun openConfirm(parent: Int): Int = ConfirmDialog.open(
        state,
        parent,
        title = Component.text("Löschen?"),
        text = Component.text("Die Akte wird gelöscht."),
        confirmLabel = Component.text("Löschen"),
        cancelLabel = Component.text("Abbrechen"),
        destructive = true,
        onConfirm = { outcomes += "confirm" },
        onCancel = { outcomes += "cancel" },
    ).sessionId

    /**
     * Verifies that a screen open carries its theme, variant, presentation and sheet side.
     */
    @Test
    fun `opens carry theme and presentation`() {
        state.open(
            Screen(Component.text("x"), theme = "sar", variant = ScreenVariant.LIGHT) {
                Label(Component.empty(), id = "l")
            },
            null,
            ScreenPresentation.SHEET,
            SheetSide.LEFT,
        )

        val open = sent.last() as ScreenOpen
        assertEquals("sar", open.theme)
        assertEquals(ThemeVariant.LIGHT, open.variant)
        assertEquals(Presentation.SHEET, open.presentation)
        assertEquals(SheetEdge.LEFT, open.sheetEdge)
    }

    /**
     * Verifies that a confirmation opens as a dialog over its parent in the parent's theme.
     */
    @Test
    fun `confirmation opens as a dialog in the parent's theme`() {
        val parent = openParent()

        openConfirm(parent)

        val open = sent.last() as ScreenOpen
        assertEquals(Presentation.DIALOG, open.presentation)
        assertEquals(parent, open.parentSessionId)
        assertEquals("police", open.theme)
        assertEquals(ThemeVariant.LIGHT, open.variant)
    }

    /**
     * Verifies that confirming runs only the confirm handler and closes the dialog.
     */
    @Test
    fun `confirm runs only the confirm handler`() {
        val dialog = openConfirm(openParent())

        state.handleWidgetAction(ScreenWidgetAction(dialog, ConfirmDialog.CONFIRM_ID))

        assertEquals(listOf("confirm"), outcomes)
        assertEquals(1, state.openScreens.size)
    }

    /**
     * Verifies that cancelling runs only the cancel handler.
     */
    @Test
    fun `cancel runs only the cancel handler`() {
        val dialog = openConfirm(openParent())

        state.handleWidgetAction(ScreenWidgetAction(dialog, ConfirmDialog.CANCEL_ID))

        assertEquals(listOf("cancel"), outcomes)
    }

    /**
     * Verifies that closing the dialog with Escape or the backdrop counts as cancel.
     */
    @Test
    fun `closing the dialog counts as cancel`() {
        val dialog = openConfirm(openParent())

        state.handleClosed(ScreenClosed(dialog))

        assertEquals(listOf("cancel"), outcomes)
    }

    /**
     * Verifies that closing the parent closes the dialog and counts as cancel.
     */
    @Test
    fun `closing the parent counts as cancel`() {
        val parent = openParent()
        openConfirm(parent)

        state.close(parent, notifyClient = true)

        assertEquals(listOf("cancel"), outcomes)
        assertTrue(state.openScreens.isEmpty())
    }

    /**
     * Verifies that a screen without its own theme inherits its parent's theme and variant, while
     * one with a theme keeps it.
     */
    @Test
    fun `screens without a theme inherit the parent's theme`() {
        val parent = openParent()

        state.open(Screen(Component.text("Kind")) { Label(Component.empty(), id = "c") }, parent, ScreenPresentation.SHEET)
        val inherited = sent.last() as ScreenOpen
        state.open(Screen(Component.text("Eigen"), theme = "sar") { Label(Component.empty(), id = "o") }, parent, ScreenPresentation.DIALOG)
        val own = sent.last() as ScreenOpen

        assertEquals("police", inherited.theme)
        assertEquals(ThemeVariant.LIGHT, inherited.variant)
        assertEquals("sar", own.theme)
    }

    /**
     * Verifies that a root screen without a theme uses the default theme in dark.
     */
    @Test
    fun `root screens without a theme use the default theme`() {
        state.open(Screen(Component.text("Wurzel")) { Label(Component.empty(), id = "r") }, null)

        val open = sent.last() as ScreenOpen
        assertEquals("default", open.theme)
        assertEquals(ThemeVariant.DARK, open.variant)
    }
}
