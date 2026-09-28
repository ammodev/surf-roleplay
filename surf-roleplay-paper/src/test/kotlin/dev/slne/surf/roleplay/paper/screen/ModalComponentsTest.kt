package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogSize
import dev.slne.surf.roleplay.api.client.common.screen.OverlaySide
import dev.slne.surf.roleplay.api.client.common.screen.alertDialog
import dev.slne.surf.roleplay.api.client.common.screen.alertDialogAction
import dev.slne.surf.roleplay.api.client.common.screen.alertDialogCancel
import dev.slne.surf.roleplay.api.client.common.screen.alertDialogContent
import dev.slne.surf.roleplay.api.client.common.screen.alertDialogMedia
import dev.slne.surf.roleplay.api.client.common.screen.dialog
import dev.slne.surf.roleplay.api.client.common.screen.dialogClose
import dev.slne.surf.roleplay.api.client.common.screen.dialogContent
import dev.slne.surf.roleplay.api.client.common.screen.dialogDescription
import dev.slne.surf.roleplay.api.client.common.screen.dialogFooter
import dev.slne.surf.roleplay.api.client.common.screen.dialogHeader
import dev.slne.surf.roleplay.api.client.common.screen.dialogTitle
import dev.slne.surf.roleplay.api.client.common.screen.drawer
import dev.slne.surf.roleplay.api.client.common.screen.drawerContent
import dev.slne.surf.roleplay.api.client.common.screen.screen
import dev.slne.surf.roleplay.api.client.common.screen.sheet
import dev.slne.surf.roleplay.api.client.common.screen.sheetContent
import dev.slne.surf.roleplay.api.client.common.screen.sheetFooter
import dev.slne.surf.roleplay.api.client.common.screen.sheetHeader
import dev.slne.surf.roleplay.api.client.common.screen.sheetTitle
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
        screen(Component.text("Dialoge")) {
            column("root") {
                dialog("dialog", onChange = { clicks += "dialog=${it.value}" }) {
                    button("open_dialog", Component.text("Öffnen"), submitsInput = false)
                    dialogContent("dialog_content") {
                        dialogHeader("dialog_header") {
                            dialogTitle("dialog_title", Component.text("Profil bearbeiten"))
                            dialogDescription("dialog_description", Component.text("Ändere dein Profil."))
                        }
                        dialogFooter("dialog_footer") {
                            dialogClose("dialog_close") { button("cancel", Component.text("Abbrechen"), submitsInput = false) }
                        }
                    }
                }
                alertDialog("alert") {
                    button("open_alert", Component.text("Löschen"), submitsInput = false)
                    alertDialogContent("alert_content", AlertDialogSize.SM) {
                        dialogHeader("alert_header") { alertDialogMedia("alert_media", "trash") }
                        dialogFooter("alert_footer") {
                            alertDialogCancel("alert_cancel", Component.text("Abbrechen"))
                            alertDialogAction("alert_ok", Component.text("Löschen")) { clicks += it.buttonId }
                        }
                    }
                }
                sheet("sheet") {
                    button("open_sheet", Component.text("Sheet"), submitsInput = false)
                    sheetContent("sheet_content", OverlaySide.LEFT) {
                        sheetHeader("sheet_header") { sheetTitle("sheet_title", Component.text("Filter")) }
                        sheetFooter("sheet_footer") { button("apply", Component.text("Anwenden")) }
                    }
                }
                drawer("drawer") {
                    button("open_drawer", Component.text("Drawer"), submitsInput = false)
                    drawerContent("drawer_content", OverlaySide.TOP) {}
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
