package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for switches, radio groups and sliders on the wire, and for the slider value arithmetic.
 */
class ChoiceComponentsProtocolTest {

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
     * Verifies that every choice component survives a round trip with all its settings.
     */
    @Test
    fun `choice components round-trip`() {
        val root = ColumnNode(
            "root",
            children = listOf(
                SwitchNode("s", checked = true, size = SwitchSize.SM, enabled = false, notifyChange = true),
                RadioGroupNode(
                    "r",
                    options = listOf(RadioOption("a", "\"A\""), RadioOption("b", enabled = false)),
                    selected = "a",
                    orientation = Orientation.HORIZONTAL,
                    required = true,
                    notifyChange = true,
                ),
                SliderNode("v", values = listOf(20.0, 80.5), min = -10.0, max = 90.5, step = 0.5, orientation = Orientation.VERTICAL, notifyChange = true),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that values snap to the closest step within the range, without binary noise.
     */
    @Test
    fun `slider values snap to steps`() {
        assertEquals(0.3, SliderValues.snap(0.29, 0.0, 1.0, 0.1))
        assertEquals(10.0, SliderValues.snap(12.0, 0.0, 100.0, 10.0))
        assertEquals(100.0, SliderValues.snap(250.0, 0.0, 100.0, 10.0))
        assertEquals(-5.0, SliderValues.snap(-7.0, -5.0, 5.0, 1.0))
        assertEquals(7.0, SliderValues.snap(6.0, 1.0, 10.0, 3.0))
    }

    /**
     * Verifies the step check, the decimal form and parsing.
     */
    @Test
    fun `slider values format and parse`() {
        assertTrue(SliderValues.isSelectable(0.3, 0.0, 1.0, 0.1))
        assertFalse(SliderValues.isSelectable(0.35, 0.0, 1.0, 0.1))
        assertFalse(SliderValues.isSelectable(1.1, 0.0, 1.0, 0.1))
        assertEquals("25", SliderValues.format(25.0))
        assertEquals("0.3", SliderValues.format(0.1 + 0.2))
        assertEquals("0", SliderValues.format(-0.0))
        assertEquals(listOf(20.0, 80.5), SliderValues.parse("20,80.5"))
        assertNull(SliderValues.parse("20,x"))
        assertNull(SliderValues.parse("NaN"))
    }
}
