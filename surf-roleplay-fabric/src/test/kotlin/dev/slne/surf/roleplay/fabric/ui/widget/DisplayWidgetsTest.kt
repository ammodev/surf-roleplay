package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.screen.ScreenPatcher
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.text.TextBlock
import dev.slne.surf.roleplay.protocol.screen.BadgeNode
import dev.slne.surf.roleplay.protocol.screen.BadgeVariant
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.KbdGroupNode
import dev.slne.surf.roleplay.protocol.screen.KbdNode
import dev.slne.surf.roleplay.protocol.screen.Orientation
import dev.slne.surf.roleplay.protocol.screen.SeparatorNode
import dev.slne.surf.roleplay.protocol.screen.SetText
import dev.slne.surf.roleplay.protocol.screen.TextKind
import dev.slne.surf.roleplay.protocol.screen.TextListNode
import dev.slne.surf.roleplay.protocol.screen.TextNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for the display widgets in the mod.
 */
class DisplayWidgetsTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * Measures a widget at a width.
     *
     * @param widget the widget
     * @param maxWidth the width available to it
     * @return its preferred size
     */
    private fun measure(widget: Widget, maxWidth: Int = FlexLayout.UNBOUNDED): Size = FlexLayout.measure(widget.createLayout(measurer), maxWidth)

    /**
     * Verifies that a paragraph wraps to the width it gets.
     */
    @Test
    fun `paragraph wraps`() {
        val text = WidgetFactory.create(TextNode("p", text = "aa bb cc dd ee"))

        assertEquals(Size(25, TextBlock.height(measurer, 3)), measure(text, 25))
    }

    /**
     * Verifies that a heading is drawn with a scaled font and wraps at the scaled width.
     */
    @Test
    fun `heading is scaled`() {
        val heading = WidgetFactory.create(TextNode("h1", kind = TextKind.H1, text = "\"aa bb\""))

        assertEquals(Size(50, 18), measure(heading, 100))
        assertEquals(Size(20, 2 * 9 * 2 + 2 * TextBlock.LINE_GAP), measure(heading, 25))
    }

    /**
     * Verifies that an item description shows at most two lines by default, and that a node's
     * line limit overrides it.
     */
    @Test
    fun `line limits apply`() {
        val description = WidgetFactory.create(TextNode("d", kind = TextKind.ITEM_DESCRIPTION, text = "aa bb cc dd ee"))
        val limited = WidgetFactory.create(TextNode("l", text = "aa bb cc dd ee", maxLines = 1))

        assertEquals(TextBlock.height(measurer, 2), measure(description, 25).height)
        assertEquals(TextBlock.height(measurer, 1), measure(limited, 25).height)
    }

    /**
     * Verifies that a list indents its items and wraps them.
     */
    @Test
    fun `list indents and wraps items`() {
        val list = WidgetFactory.create(TextListNode("list", items = listOf("aa bb cc", "dd")))

        val size = measure(list, TextListWidget.INDENT + 25)

        assertEquals(TextListWidget.INDENT + 25, size.width)
        assertEquals(TextBlock.height(measurer, 2) + TextListWidget.ITEM_GAP + TextBlock.height(measurer, 1), size.height)
    }

    /**
     * Verifies that a separator is one pixel thick across its orientation.
     */
    @Test
    fun `separators are one pixel thick`() {
        assertEquals(1, measure(WidgetFactory.create(SeparatorNode("h"))).height)
        assertEquals(1, measure(WidgetFactory.create(SeparatorNode("v", orientation = Orientation.VERTICAL))).width)
    }

    /**
     * Verifies that badges and keys keep their width in a narrow container.
     */
    @Test
    fun `badges and keys never wrap`() {
        val badge = WidgetFactory.create(BadgeNode("badge", text = "aa bb cc", variant = BadgeVariant.OUTLINE))
        val key = WidgetFactory.create(KbdNode("key", text = "Strg"))

        assertEquals(measure(badge), measure(badge, 10))
        assertEquals(measure(key), measure(key, 10))
        assertEquals(BadgeWidget.HEIGHT, measure(badge).height)
        assertEquals(KbdWidget.HEIGHT, measure(key).height)
    }

    /**
     * Verifies that a single-character key is at least square.
     */
    @Test
    fun `short key is square`() {
        val key = WidgetFactory.create(KbdNode("key", text = "K"))

        assertTrue(measure(key).width >= KbdWidget.HEIGHT)
    }

    /**
     * Verifies that texts, badges and keys take new texts from patches, and that a key group is a
     * container of keys.
     */
    @Test
    fun `texts can be patched`() {
        val root = WidgetFactory.create(
            ColumnNode(
                "root",
                children = listOf(
                    TextNode("text", text = "a"),
                    BadgeNode("badge", text = "b"),
                    KbdGroupNode("keys", children = listOf(KbdNode("key", text = "c"))),
                ),
            ),
        )

        ScreenPatcher.apply(root, listOf(SetText("text", "xx"), SetText("badge", "yy"), SetText("key", "zz")))

        assertEquals("xx", (WidgetTree.find(root, "text") as TextWidget).text)
        assertEquals("yy", (WidgetTree.find(root, "badge") as BadgeWidget).text)
        assertEquals("zz", (WidgetTree.find(root, "key") as KbdWidget).text)
        assertIs<ContainerWidget>(WidgetTree.find(root, "keys"))
    }
}
