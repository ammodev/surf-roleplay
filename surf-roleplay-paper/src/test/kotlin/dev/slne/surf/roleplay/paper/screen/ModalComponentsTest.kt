package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogSize
import dev.slne.surf.roleplay.api.client.common.screen.OverlaySide
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialog
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialogAction
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialogCancel
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialogContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialogMedia
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Button
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Dialog
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogClose
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogDescription
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogFooter
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DialogTitle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Drawer
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DrawerContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Screen
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Sheet
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetContent
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetFooter
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SheetTitle
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.screen.AlertDialogContentNode
import dev.slne.surf.roleplay.protocol.screen.AlertDialogMediaNode
import dev.slne.surf.roleplay.protocol.screen.AlertDialogNode
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.DialogCloseNode
import dev.slne.surf.roleplay.protocol.screen.DialogContentNode
import dev.slne.surf.roleplay.protocol.screen.DialogFooterNode
import dev.slne.surf.roleplay.protocol.screen.DialogHeaderNode
import dev.slne.surf.roleplay.protocol.screen.DialogNode
import dev.slne.surf.roleplay.protocol.screen.DrawerContentNode
import dev.slne.surf.roleplay.protocol.screen.DrawerNode
import dev.slne.surf.roleplay.protocol.screen.ScreenInputChange
import dev.slne.surf.roleplay.protocol.screen.ScreenOpen
import dev.slne.surf.roleplay.protocol.screen.ScreenWidgetAction
import dev.slne.surf.roleplay.protocol.screen.SheetContentNode
import dev.slne.surf.roleplay.protocol.screen.SheetFooterNode
import dev.slne.surf.roleplay.protocol.screen.SheetHeaderNode
import dev.slne.surf.roleplay.protocol.screen.SheetNode
import dev.slne.surf.roleplay.protocol.screen.TextNode
import dev.slne.surf.roleplay.protocol.screen.WidgetScreenBody
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import dev.slne.surf.roleplay.protocol.screen.AlertDialogSize as NodeAlertDialogSize
import dev.slne.surf.roleplay.protocol.screen.OverlaySide as NodeOverlaySide
import dev.slne.surf.roleplay.protocol.screen.TextKind as NodeTextKind

/**
 * Tests for dialogs, alert dialogs, sheets and drawers in the API and on Paper.
 */
class ModalComponentsTest {

    /**
     * The packets sent to the client.
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
     * The clicks the dialogs reported.
     */
    private val clicks = mutableListOf<String>()

    /**
     * Opens a screen with a dialog, an alert dialog, a sheet and a drawer.
     *
     * @return the session id
     */
    private fun open(): Int = state.open(
        Screen(Component.text("Dialoge")) {
            Column(id = "root") {
                Dialog(onChange = { clicks += "dialog=${it.value}" }, id = "dialog") {
                    Button(Component.text("Öffnen"), submitsInput = false, id = "open_dialog")
                    DialogContent(id = "dialog_content") {
                        DialogHeader(id = "dialog_header") {
                            DialogTitle(Component.text("Profil bearbeiten"), id = "dialog_title")
                            DialogDescription(Component.text("Ändere dein Profil."), id = "dialog_description")
                        }
                        DialogFooter(id = "dialog_footer") {
                            DialogClose(id = "dialog_close") { Button(Component.text("Abbrechen"), submitsInput = false, id = "cancel") }
                        }
                    }
                }
                AlertDialog(id = "alert") {
                    Button(Component.text("Löschen"), submitsInput = false, id = "open_alert")
                    AlertDialogContent(AlertDialogSize.SM, id = "alert_content") {
                        DialogHeader(id = "alert_header") { AlertDialogMedia("trash", id = "alert_media") }
                        DialogFooter(id = "alert_footer") {
                            AlertDialogCancel(Component.text("Abbrechen"), id = "alert_cancel")
                            AlertDialogAction(Component.text("Löschen"), id = "alert_ok") { clicks += it.buttonId }
                        }
                    }
                }
                Sheet(id = "sheet") {
                    Button(Component.text("Sheet"), submitsInput = false, id = "open_sheet")
                    SheetContent(OverlaySide.LEFT, id = "sheet_content") {
                        SheetHeader(id = "sheet_header") { SheetTitle(Component.text("Filter"), id = "sheet_title") }
                        SheetFooter(id = "sheet_footer") { Button(Component.text("Anwenden"), id = "apply") }
                    }
                }
                Drawer(id = "drawer") {
                    Button(Component.text("Drawer"), submitsInput = false, id = "open_drawer")
                    DrawerContent(OverlaySide.TOP, id = "drawer_content") {}
                }
            }
        },
        null,
    ).sessionId

    /**
     * Verifies that every modal part maps to its node with its settings and default widths.
     */
    @Test
    fun `modals map to their nodes`() {
        open()

        val root = assertIs<ColumnNode>(assertIs<WidgetScreenBody>((sent.last() as ScreenOpen).body).root)
        val dialog = assertIs<DialogNode>(root.children[0])
        val content = assertIs<DialogContentNode>(dialog.children[1])
        assertEquals(256, content.width.value)
        assertEquals(NodeTextKind.DIALOG_TITLE, assertIs<TextNode>(assertIs<DialogHeaderNode>(content.children[0]).children[0]).kind)
        assertIs<DialogCloseNode>(assertIs<DialogFooterNode>(content.children[1]).children[0])
        val alert = assertIs<AlertDialogContentNode>(assertIs<AlertDialogNode>(root.children[1]).children[1])
        assertEquals(NodeAlertDialogSize.SM, alert.size)
        assertEquals(160, alert.width.value)
        assertIs<AlertDialogMediaNode>(assertIs<DialogHeaderNode>(alert.children[0]).children[0])
        val cancel = assertIs<DialogCloseNode>(assertIs<DialogFooterNode>(alert.children[1]).children[0])
        assertIs<ButtonNode>(cancel.children.single())
        val sheet = assertIs<SheetContentNode>(assertIs<SheetNode>(root.children[2]).children[1])
        assertEquals(NodeOverlaySide.LEFT, sheet.side)
        assertIs<SheetHeaderNode>(sheet.children[0])
        assertIs<SheetFooterNode>(sheet.children[1])
        assertEquals(NodeOverlaySide.TOP, assertIs<DrawerContentNode>(assertIs<DrawerNode>(root.children[3]).children[1]).direction)
    }

    /**
     * Verifies that a dialog reports its open state and that the alert dialog action runs its
     * handler.
     */
    @Test
    fun `dialogs report and act`() {
        val session = open()

        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleInputChange(ScreenInputChange(session, "dialog", "true")))
        assertIs<PlayerScreenState.Outcome.Accepted>(state.handleWidgetAction(ScreenWidgetAction(session, "alert_ok")))
        assertEquals(listOf("dialog=true", "alert_ok"), clicks)
    }
}
