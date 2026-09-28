package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.EmptyContentNode
import dev.slne.surf.roleplay.protocol.screen.EmptyHeaderNode
import dev.slne.surf.roleplay.protocol.screen.EmptyMediaNode
import dev.slne.surf.roleplay.protocol.screen.EmptyMediaVariant
import dev.slne.surf.roleplay.protocol.screen.EmptyNode
import dev.slne.surf.roleplay.protocol.screen.ItemActionsNode
import dev.slne.surf.roleplay.protocol.screen.ItemContentNode
import dev.slne.surf.roleplay.protocol.screen.ItemFooterNode
import dev.slne.surf.roleplay.protocol.screen.ItemGroupNode
import dev.slne.surf.roleplay.protocol.screen.ItemHeaderNode
import dev.slne.surf.roleplay.protocol.screen.ItemMediaNode
import dev.slne.surf.roleplay.protocol.screen.ItemMediaVariant
import dev.slne.surf.roleplay.protocol.screen.ItemNode
import dev.slne.surf.roleplay.protocol.screen.ItemSize
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.TextKind
import dev.slne.surf.roleplay.protocol.screen.TextNode
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for empty states and items in the mod.
 */
class ItemWidgetsTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * The widgets whose actions were triggered, in order.
     */
    private val actions = mutableListOf<String>()

    /**
     * A context that records actions.
     */
    private val context = object : UiContext {
        override var focusedWidget: Widget? = null
        override fun focus(widget: Widget?) {
            focusedWidget = widget
        }
        override fun requestLayout() = Unit
        override fun actionTriggered(widget: Widget, submitsInput: Boolean) {
            actions += "${widget.id}:$submitsInput"
        }
        override var clipboard: String = ""
    }

    /**
     * Creates the widget of a node and lays it out in an area.
     *
     * @param node the node
     * @param area the area
     * @return the widget
     */
    private fun laidOut(node: ScreenNode, area: Rect): Widget {
        val widget = WidgetFactory.create(node)
        FlexLayout.layout(widget.createLayout(measurer), area)
        widget.applyLayout()
        return widget
    }

    /**
     * Creates an item with media, content, actions, a header and a footer.
     *
     * @param clickable whether the item fires an action
     * @return the item node
     */
    private fun item(clickable: Boolean = false) = ItemNode(
        "item",
        clickable = clickable,
        children = listOf(
            ItemHeaderNode("header", children = listOf(TextNode("left", text = "L"), TextNode("right", text = "R"))),
            ItemMediaNode("media", variant = ItemMediaVariant.ICON, icon = "user"),
            ItemContentNode("content", children = listOf(TextNode("title", kind = TextKind.ITEM_TITLE, text = "Title"))),
            ItemActionsNode("actions", children = listOf(ButtonNode("open", text = "Open"))),
            ItemFooterNode("footer", children = listOf(TextNode("note", text = "Note"))),
        ),
    )

    /**
     * Verifies that an empty state centers its parts, to within the pixel lost when centering
     * inside an odd width.
     */
    @Test
    fun `empty centers its parts`() {
        val empty = laidOut(
            EmptyNode(
                "empty",
                outline = true,
                children = listOf(
                    EmptyHeaderNode("header", children = listOf(EmptyMediaNode("media", variant = EmptyMediaVariant.ICON, icon = "folder"), TextNode("title", kind = TextKind.EMPTY_TITLE, text = "Leer"))),
                    EmptyContentNode("content", children = listOf(ButtonNode("create", text = "Neu"))),
                ),
            ),
            Rect(0, 0, 200, 150),
        )

        val media = WidgetTree.find(empty, "media")!!.bounds
        val button = WidgetTree.find(empty, "create")!!.bounds
        assertTrue(abs(media.x + media.width / 2 - 100) <= 1, "media centre")
        assertTrue(abs(button.x + button.width / 2 - 100) <= 1, "button centre")
        assertEquals(EmptyMediaWidget.ICON_BOX, media.width)
    }

    /**
     * Verifies that an item puts its header above and its footer below the row of media, content
     * and actions, and that the content grows to push the actions to the end.
     */
    @Test
    fun `item places header, row and footer`() {
        val item = laidOut(item(), Rect(0, 0, 200, 100))
        val padding = ItemWidget.padding(ItemSize.DEFAULT)

        val header = WidgetTree.find(item, "header")!!.bounds
        val media = WidgetTree.find(item, "media")!!.bounds
        val actions = WidgetTree.find(item, "actions")!!.bounds
        val footer = WidgetTree.find(item, "footer")!!.bounds
        val right = WidgetTree.find(item, "right")!!.bounds
        assertEquals(padding.top, header.y)
        assertTrue(media.y >= header.bottom)
        assertTrue(footer.y >= media.bottom)
        assertEquals(200 - padding.right, actions.right)
        assertEquals(200 - padding.right, right.right)
        assertEquals(ItemMediaWidget.ICON_BOX, media.width)
    }

    /**
     * Verifies that a clickable item fires an action on a click and on Enter, without submitting
     * input, and that a click on a button inside it fires only the button.
     */
    @Test
    fun `clickable item fires actions`() {
        val item = laidOut(item(clickable = true), Rect(0, 0, 200, 100))
        val title = WidgetTree.find(item, "title")!!.bounds
        val button = WidgetTree.find(item, "open")!!.bounds

        assertTrue(item.focusable)
        item.mouseClicked(context, title.x + 1.0, title.y + 1.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)
        item.keyPressed(context, KeyEvent(GLFW.GLFW_KEY_ENTER, 0, 0))
        item.mouseClicked(context, button.x + 1.0, button.y + 1.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)

        assertEquals(listOf("item:false", "item:false", "open:true"), actions)
    }

    /**
     * Verifies that an item that is not clickable fires nothing and cannot take the focus.
     */
    @Test
    fun `plain item fires nothing`() {
        val item = laidOut(item(), Rect(0, 0, 200, 100))
        val title = WidgetTree.find(item, "title")!!.bounds

        assertFalse(item.focusable)
        item.mouseClicked(context, title.x + 1.0, title.y + 1.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)

        assertEquals(emptyList(), actions)
    }

    /**
     * Verifies that an item group stretches its items across its width.
     */
    @Test
    fun `group stretches its items`() {
        val group = laidOut(ItemGroupNode("group", children = listOf(item().copy(id = "a", children = emptyList()), item().copy(id = "b", size = ItemSize.SM, children = emptyList()))), Rect(0, 0, 200, 100))

        assertEquals(200, WidgetTree.find(group, "a")!!.bounds.width)
        assertEquals(200, WidgetTree.find(group, "b")!!.bounds.width)
    }

    /**
     * Verifies that an item measured at a width is as tall as its laid-out content, with a
     * description that wraps beside an action.
     */
    @Test
    fun `measured item fits its wrapped description`() {
        val node = ItemNode(
            "item",
            children = listOf(
                ItemContentNode("content", children = listOf(TextNode("description", kind = TextKind.ITEM_DESCRIPTION, text = "aa bb cc dd ee ff gg hh ii jj"))),
                ItemActionsNode("actions", children = listOf(ButtonNode("open", text = "Open"))),
            ),
        )
        val widget = WidgetFactory.create(node)
        val layout = widget.createLayout(measurer)
        val measured = FlexLayout.measure(layout, 120)
        FlexLayout.layout(layout, Rect(0, 0, 120, measured.height))
        widget.applyLayout()

        val description = WidgetTree.find(widget, "description")!!.bounds
        assertEquals(measured.height - ItemWidget.padding(ItemSize.DEFAULT).bottom, description.bottom)
    }
}
