package dev.slne.surf.roleplay.protocol.screen

import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the overlay components on the wire.
 */
class OverlayComponentsProtocolTest {

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
     * Verifies that popovers, hover cards and tooltips survive a round trip.
     */
    @Test
    fun `popovers, hover cards and tooltips round-trip`() {
        val root = ColumnNode(
            "root",
            children = listOf(
                PopoverNode(
                    "popover",
                    open = true,
                    side = OverlaySide.RIGHT,
                    align = Align.START,
                    notifyChange = true,
                    children = listOf(
                        ButtonNode("trigger"),
                        PopoverContentNode(
                            "content",
                            children = listOf(PopoverHeaderNode("header", children = listOf(TextNode("title", kind = TextKind.POPOVER_TITLE), TextNode("description", kind = TextKind.POPOVER_DESCRIPTION)))),
                        ),
                    ),
                ),
                HoverCardNode("card", openDelay = 500, closeDelay = 100, children = listOf(LabelNode("name"), HoverCardContentNode("card_content"))),
                TooltipNode("tip", text = "\"Hinweis\"", side = OverlaySide.LEFT, children = listOf(ButtonNode("tip_trigger"))),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that the set-open patch survives a round trip.
     */
    @Test
    fun `set open patches round-trip`() {
        val patch = ScreenPatch(sessionId = 1, operations = listOf(SetOpen("popover", true), SetOpen("card", false)))

        assertEquals(patch, ProtocolCodec.decode(Packets.SCREEN_PATCH.channel, ProtocolCodec.encode(Packets.SCREEN_PATCH, patch)))
    }
}
