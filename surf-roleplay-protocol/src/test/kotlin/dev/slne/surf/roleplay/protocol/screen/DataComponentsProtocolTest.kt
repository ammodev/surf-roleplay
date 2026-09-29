package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the data, chat and chart components on the wire.
 */
class DataComponentsProtocolTest {

    /**
     * Sends a tree through a screen open and returns the decoded tree.
     *
     * @param root the root of the tree
     * @return the decoded root
     */
    private fun roundTrip(root: ScreenNode): ScreenNode {
        val open = ScreenOpen(sessionId = 1, title = "{}", body = WidgetScreenBody(root))
        val decoded = ProtocolCodec.decode(Packets.SCREEN_OPEN.channel, ProtocolCodec.encode(Packets.SCREEN_OPEN, open)) as ScreenOpen
        return (decoded.body as WidgetScreenBody).root
    }

    /**
     * Verifies that a table with every part survives a round trip.
     */
    @Test
    fun `tables round-trip`() {
        val root = TableNode(
            "table",
            width = Sizing.grow(),
            children = listOf(
                TableCaptionNode("caption", text = "\"Einsätze\""),
                TableSectionNode(
                    "header",
                    section = TableSection.HEADER,
                    children = listOf(TableRowNode("head_row", children = listOf(TableCellNode("head", head = true, children = listOf(LabelNode("h")))))),
                ),
                TableSectionNode(
                    "body",
                    children = listOf(TableRowNode("row", selected = true, children = listOf(TableCellNode("cell", align = Align.END, children = listOf(LabelNode("c")))))),
                ),
                TableSectionNode("footer", section = TableSection.FOOTER),
            ),
        )

        assertEquals(root, roundTrip(root))
    }
}
