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

    /**
     * Verifies that a command menu with every part survives a round trip.
     */
    @Test
    fun `commands round-trip`() {
        val root = CommandNode(
            "command",
            notifySearch = true,
            children = listOf(
                CommandInputNode("input", placeholder = "\"Suchen\""),
                CommandListNode(
                    "list",
                    children = listOf(
                        CommandEmptyNode("empty", text = "\"Nichts\""),
                        CommandGroupNode("group", heading = "\"Vorschläge\"", children = listOf(CommandItemNode("item", text = "\"Kalender\"", icon = "calendar", shortcut = "\"K\"", keywords = listOf("termin"), enabled = false))),
                        CommandSeparatorNode("sep"),
                    ),
                ),
            ),
        )

        assertEquals(root, roundTrip(root))
    }

    /**
     * Verifies that dialogs, alert dialogs, sheets and drawers with every part survive a round
     * trip.
     */
    @Test
    fun `modals round-trip`() {
        val header = DialogHeaderNode("header", children = listOf(TextNode("title", kind = TextKind.DIALOG_TITLE), TextNode("description", kind = TextKind.DIALOG_DESCRIPTION)))
        val root = ColumnNode(
            "root",
            children = listOf(
                DialogNode(
                    "dialog",
                    open = true,
                    notifyChange = true,
                    children = listOf(
                        ButtonNode("open"),
                        DialogContentNode("content", showCloseButton = false, children = listOf(header, DialogFooterNode("footer", children = listOf(DialogCloseNode("close", children = listOf(ButtonNode("cancel"))))))),
                    ),
                ),
                AlertDialogNode(
                    "alert",
                    children = listOf(ButtonNode("open_alert"), AlertDialogContentNode("alert_content", size = AlertDialogSize.SM, children = listOf(AlertDialogMediaNode("media", icon = "trash")))),
                ),
                SheetNode(
                    "sheet",
                    children = listOf(
                        ButtonNode("open_sheet"),
                        SheetContentNode("sheet_content", side = OverlaySide.LEFT, children = listOf(SheetHeaderNode("sheet_header", children = listOf(TextNode("sheet_title", kind = TextKind.SHEET_TITLE))), SheetFooterNode("sheet_footer"))),
                    ),
                ),
                DrawerNode("drawer", children = listOf(ButtonNode("open_drawer"), DrawerContentNode("drawer_content", direction = OverlaySide.TOP))),
            ),
        )

        assertEquals(root, roundTrip(root))
    }
}
