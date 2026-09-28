package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for the theme, presentation and icon fields of the screen protocol.
 */
class ScreenFoundationProtocolTest {

    /**
     * Encodes and decodes a screen open.
     *
     * @param packet the packet
     * @return the decoded packet
     */
    private fun roundTrip(packet: ScreenOpen): ScreenOpen =
        assertIs<ScreenOpen>(ProtocolCodec.decode(Packets.SCREEN_OPEN.channel, ProtocolCodec.encode(Packets.SCREEN_OPEN, packet)))

    /**
     * Verifies that theme, variant, presentation and sheet edge survive a round trip.
     */
    @Test
    fun `theme and presentation round-trip`() {
        val packet = ScreenOpen(
            sessionId = 1,
            title = "{}",
            body = WidgetScreenBody(LabelNode("a")),
            theme = "police",
            variant = ThemeVariant.LIGHT,
            presentation = Presentation.SHEET,
            sheetEdge = SheetEdge.LEFT,
        )

        assertEquals(packet, roundTrip(packet))
    }

    /**
     * Verifies that an open without the new fields decodes to the default theme, the dark
     * variant, full-screen presentation and a right sheet edge.
     */
    @Test
    fun `missing fields decode to their defaults`() {
        val decoded = roundTrip(ScreenOpen(sessionId = 1, title = "{}", body = WidgetScreenBody(LabelNode("a"))))

        assertEquals("default", decoded.theme)
        assertEquals(ThemeVariant.DARK, decoded.variant)
        assertEquals(Presentation.SCREEN, decoded.presentation)
        assertEquals(SheetEdge.RIGHT, decoded.sheetEdge)
    }

    /**
     * Verifies that icons on buttons, labels and text inputs and the icon node survive a round
     * trip.
     */
    @Test
    fun `icons round-trip`() {
        val root = ColumnNode(
            "root",
            children = listOf(
                ButtonNode("b", icon = "trash-2"),
                LabelNode("l", icon = "info"),
                TextInputNode("t", icon = "search"),
                IconNode("i", icon = "shield", size = 24, color = IconColor.PRIMARY),
            ),
        )

        val decoded = roundTrip(ScreenOpen(sessionId = 1, title = "{}", body = WidgetScreenBody(root)))

        assertEquals(root, assertIs<WidgetScreenBody>(decoded.body).root)
    }

    /**
     * Verifies that widgets without an icon decode with no icon, and the icon node with its
     * defaults.
     */
    @Test
    fun `icons default to none`() {
        val root = ColumnNode("root", children = listOf(ButtonNode("b"), IconNode("i", icon = "x")))

        val decoded = assertIs<WidgetScreenBody>(roundTrip(ScreenOpen(sessionId = 1, title = "{}", body = WidgetScreenBody(root))).body).root

        val children = assertIs<ColumnNode>(decoded).children
        assertEquals(null, assertIs<ButtonNode>(children[0]).icon)
        assertEquals(16, assertIs<IconNode>(children[1]).size)
        assertEquals(IconColor.FOREGROUND, assertIs<IconNode>(children[1]).color)
    }
}
