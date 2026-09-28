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

    /**
     * Verifies that dropdown menus, context menus and menubars with every part survive a round
     * trip.
     */
    @Test
    fun `menus round-trip`() {
        val content = MenuContentNode(
            "content",
            children = listOf(
                MenuLabelNode("label", text = "\"Konto\"", inset = true),
                MenuGroupNode("group", children = listOf(MenuItemNode("item", text = "\"Profil\"", icon = "user", shortcut = "\"P\"", destructive = true, enabled = false))),
                MenuSeparatorNode("sep"),
                MenuCheckboxItemNode("check", text = "\"Status\"", checked = true),
                MenuRadioGroupNode("radio", value = "a", children = listOf(MenuRadioItemNode("a", text = "\"A\"", value = "a"))),
                MenuSubNode("sub", open = true, notifyChange = true, children = listOf(MenuSubTriggerNode("sub_trigger", text = "\"Mehr\""), MenuContentNode("sub_content"))),
            ),
        )
        val root = ColumnNode(
            "root",
            children = listOf(
                DropdownMenuNode("menu", side = OverlaySide.TOP, align = Align.END, children = listOf(ButtonNode("trigger"), content)),
                ContextMenuNode("ctx", children = listOf(LabelNode("area"), MenuContentNode("ctx_content"))),
                MenubarNode("bar", children = listOf(MenubarMenuNode("file", children = listOf(MenubarTriggerNode("file_trigger", text = "\"Datei\""), MenuContentNode("file_content"))))),
            ),
        )

        assertEquals(root, roundTrip(root))
    }
}
