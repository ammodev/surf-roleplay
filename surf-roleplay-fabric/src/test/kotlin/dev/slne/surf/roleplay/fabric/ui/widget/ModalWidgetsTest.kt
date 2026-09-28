package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.AlertDialogContentNode
import dev.slne.surf.roleplay.protocol.screen.AlertDialogNode
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.DialogCloseNode
import dev.slne.surf.roleplay.protocol.screen.DialogContentNode
import dev.slne.surf.roleplay.protocol.screen.DialogFooterNode
import dev.slne.surf.roleplay.protocol.screen.DialogHeaderNode
import dev.slne.surf.roleplay.protocol.screen.DialogNode
import dev.slne.surf.roleplay.protocol.screen.DrawerContentNode
import dev.slne.surf.roleplay.protocol.screen.DrawerNode
import dev.slne.surf.roleplay.protocol.screen.OverlaySide
import dev.slne.surf.roleplay.protocol.screen.RowNode
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.SheetContentNode
import dev.slne.surf.roleplay.protocol.screen.SheetFooterNode
import dev.slne.surf.roleplay.protocol.screen.SheetHeaderNode
import dev.slne.surf.roleplay.protocol.screen.SheetNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TextKind
import dev.slne.surf.roleplay.protocol.screen.TextNode
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for dialogs, alert dialogs, sheets and drawers in the mod.
 */
class ModalWidgetsTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * The widgets whose actions reached the listener, in order.
     */
    private val actions = mutableListOf<String>()

    /**
     * A listener that records actions.
     */
    private val listener = object : ScreenPanelListener {
        override fun actionTriggered(panel: ScreenPanel, widget: Widget) {
            actions += widget.id
        }
        override fun closeRequested(panel: ScreenPanel) = Unit
    }

    /**
     * Creates a laid-out panel around nodes placed in a row, in a 400 by 300 window.
     *
     * @param nodes the nodes
     * @return the panel
     */
    private fun panel(vararg nodes: ScreenNode): ScreenPanel =
        ScreenPanel("T", WidgetFactory.create(RowNode("root", gap = 20, children = nodes.toList())), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
            .also { it.layoutIfNeeded(measurer, 400, 300) }

    /**
     * Clicks the middle of a widget, laying out the open overlays first.
     *
     * @param panel the panel
     * @param id the id of the widget
     */
    private fun click(panel: ScreenPanel, id: String) {
        panel.overlayAreas()
        val b = WidgetTree.find(panel.root, id)!!.bounds
        panel.mouseClicked(b.x + b.width / 2.0, b.y + b.height / 2.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)
    }

    /**
     * Creates a dialog with a header, a footer with a close part, and a close button.
     *
     * @return the node
     */
    private fun dialog() = DialogNode(
        "dialog",
        children = listOf(
            ButtonNode("open", text = "Open"),
            DialogContentNode(
                "content",
                width = Sizing.fixed(200),
                children = listOf(
                    DialogHeaderNode("header", children = listOf(TextNode("title", kind = TextKind.DIALOG_TITLE, text = "Title"))),
                    DialogFooterNode(
                        "footer",
                        children = listOf(DialogCloseNode("close_part", children = listOf(ButtonNode("cancel", text = "Cancel"))), ButtonNode("save", text = "Save")),
                    ),
                ),
            ),
        ),
    )

    /**
     * Verifies that a dialog opens centered in the window, and that its footer places its
     * buttons at the end.
     */
    @Test
    fun `dialog is centered with footer buttons at the end`() {
        val panel = panel(dialog())
        click(panel, "open")

        val area = panel.overlayAreas().single()
        assertEquals(200, area.width)
        assertEquals((400 - 200) / 2, area.x)
        assertEquals((300 - area.height) / 2, area.y)
        assertEquals(area.right - DialogContentWidget.PADDING, WidgetTree.find(panel.root, "save")!!.bounds.right)
    }

    /**
     * Verifies that an action inside a close part fires and closes the dialog, and that an action
     * outside one keeps it open.
     */
    @Test
    fun `close parts close the dialog`() {
        val panel = panel(dialog())
        click(panel, "open")

        click(panel, "save")
        assertNotNull(panel.popover)
        click(panel, "cancel")

        assertNull(panel.popover)
        assertEquals(listOf("save", "cancel"), actions)
    }

    /**
     * Verifies that the close button at the top right closes the dialog without an action.
     */
    @Test
    fun `close button closes the dialog`() {
        val panel = panel(dialog())
        click(panel, "open")
        val content = WidgetTree.find(panel.root, "content") as DialogContentWidget
        val button = content.closeButton()

        panel.mouseClicked(button.x + 1.0, button.y + 1.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)

        assertNull(panel.popover)
        assertEquals(emptyList(), actions)
    }

    /**
     * Verifies that a click outside an alert dialog keeps it open, and Escape closes it.
     */
    @Test
    fun `alert dialogs ignore outside clicks`() {
        val panel = panel(AlertDialogNode("alert", children = listOf(ButtonNode("open", text = "Open"), AlertDialogContentNode("content", children = listOf(ButtonNode("ok", text = "OK"))))))
        click(panel, "open")

        panel.mouseClicked(2.0, 2.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)
        assertNotNull(panel.popover)
        panel.keyPressed(KeyEvent(GLFW.GLFW_KEY_ESCAPE, 0, 0))

        assertNull(panel.popover)
    }

    /**
     * Verifies that a right sheet is attached to the right edge over the whole window height, and
     * that its footer sits at its bottom.
     */
    @Test
    fun `sheet is attached to its edge`() {
        val panel = panel(
            SheetNode(
                "sheet",
                children = listOf(
                    ButtonNode("open", text = "Open"),
                    SheetContentNode(
                        "content",
                        side = OverlaySide.RIGHT,
                        children = listOf(SheetHeaderNode("header", children = listOf(TextNode("title", kind = TextKind.SHEET_TITLE, text = "T"))), SheetFooterNode("footer", children = listOf(ButtonNode("done", text = "Done")))),
                    ),
                ),
            ),
        )
        click(panel, "open")

        val area = panel.overlayAreas().single()
        assertEquals(400, area.right)
        assertEquals(0, area.y)
        assertEquals(300, area.height)
        assertEquals(SheetContentWidget.SIDE_WIDTH, area.width)
        assertEquals(300 - SheetFooterWidget.PADDING, WidgetTree.find(panel.root, "done")!!.bounds.bottom)
    }

    /**
     * Verifies that a bottom drawer spans the window width at its bottom edge.
     */
    @Test
    fun `drawer comes from the bottom`() {
        val panel = panel(DrawerNode("drawer", children = listOf(ButtonNode("open", text = "Open"), DrawerContentNode("content", children = listOf(ButtonNode("x", text = "X"))))))
        click(panel, "open")

        val area = panel.overlayAreas().single()
        assertEquals(0, area.x)
        assertEquals(400, area.width)
        assertEquals(300, area.bottom)
        assertTrue(area.height < 300)
    }
}
