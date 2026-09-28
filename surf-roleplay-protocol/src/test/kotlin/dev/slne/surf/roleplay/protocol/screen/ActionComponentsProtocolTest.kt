package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for button variants, button groups, toggles and toggle groups on the wire.
 */
class ActionComponentsProtocolTest {

    /**
     * Encodes and decodes a tree inside a screen open.
     *
     * @param root the tree
     * @return the decoded tree
     */
    private fun roundTrip(root: ScreenNode): ScreenNode {
        val open = ScreenOpen(sessionId = 1, title = "{}", body = WidgetScreenBody(root))
        val decoded = ProtocolCodec.decode(Packets.SCREEN_OPEN.channel, ProtocolCodec.encode(Packets.SCREEN_OPEN, open)) as ScreenOpen
        return (decoded.body as WidgetScreenBody).root
    }

    /**
     * Verifies that every button variant and size survives a round trip.
     */
    @Test
    fun `button variants and sizes round-trip`() {
        val buttons = ButtonVariant.entries.flatMap { variant ->
            ButtonSize.entries.map { size -> ButtonNode("b_${variant}_$size", variant = variant, size = size) }
        }
        val root = ColumnNode("root", children = buttons)

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that a vertical button group with text and a separator survives a round trip.
     */
    @Test
    fun `button groups round-trip`() {
        val root = ButtonGroupNode(
            "group",
            orientation = Orientation.VERTICAL,
            children = listOf(ButtonGroupTextNode("t", text = "\"Text\"", icon = "info"), ButtonNode("a"), ButtonGroupSeparatorNode("s"), ButtonNode("b")),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that toggles and toggle groups survive a round trip with all their settings.
     */
    @Test
    fun `toggles and toggle groups round-trip`() {
        val root = ColumnNode(
            "root",
            children = listOf(
                ToggleNode("bold", text = "\"B\"", icon = "bold", pressed = true, variant = ToggleVariant.OUTLINE, size = ToggleSize.LG, enabled = false),
                ToggleGroupNode(
                    "align",
                    items = listOf(ToggleGroupItem("left", icon = "align-left"), ToggleGroupItem("right", text = "\"R\"", enabled = false)),
                    multiple = true,
                    selected = listOf("left"),
                    variant = ToggleVariant.OUTLINE,
                    size = ToggleSize.SM,
                    spacing = 2,
                    orientation = Orientation.VERTICAL,
                    required = true,
                    notifyChange = true,
                ),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies the defaults of the new fields and nodes.
     */
    @Test
    fun `defaults are shadcn's defaults`() {
        val button = ButtonNode("b")
        val toggle = ToggleNode("t")
        val group = ToggleGroupNode("g")

        assertEquals(ButtonVariant.DEFAULT, button.variant)
        assertEquals(ButtonSize.DEFAULT, button.size)
        assertEquals(ToggleVariant.DEFAULT, toggle.variant)
        assertEquals(ToggleSize.DEFAULT, toggle.size)
        assertEquals(false, group.multiple)
        assertEquals(0, group.spacing)
        assertEquals(Orientation.HORIZONTAL, ButtonGroupNode("x").orientation)
    }
}
