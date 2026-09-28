package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the display components on the wire.
 */
class DisplayComponentsProtocolTest {

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
     * Verifies that texts of every kind, lists, separators, keys and badges survive a round trip.
     */
    @Test
    fun `texts, separators, keys and badges round-trip`() {
        val root = ColumnNode(
            "root",
            children = TextKind.entries.map { TextNode("text_${it.name}", kind = it, text = "\"Text\"", maxLines = 2, align = Align.CENTER) } + listOf(
                TextListNode("list", items = listOf("\"Eins\"", "\"Zwei\""), ordered = true),
                SeparatorNode("separator", orientation = Orientation.VERTICAL),
                KbdGroupNode("keys", children = listOf(KbdNode("ctrl", text = "\"Strg\""), KbdNode("cmd", icon = "command"))),
            ) + BadgeVariant.entries.map { BadgeNode("badge_${it.name}", text = "\"Neu\"", icon = "check", variant = it) },
        )

        assertEquals(root, roundTrip(root))
    }
}
