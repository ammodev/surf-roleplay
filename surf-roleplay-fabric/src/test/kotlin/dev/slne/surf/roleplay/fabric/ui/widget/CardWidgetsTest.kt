package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.AlertNode
import dev.slne.surf.roleplay.protocol.screen.AlertVariant
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.CardActionNode
import dev.slne.surf.roleplay.protocol.screen.CardContentNode
import dev.slne.surf.roleplay.protocol.screen.CardFooterNode
import dev.slne.surf.roleplay.protocol.screen.CardHeaderNode
import dev.slne.surf.roleplay.protocol.screen.CardNode
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TextKind
import dev.slne.surf.roleplay.protocol.screen.TextNode
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for alerts and cards in the mod.
 */
class CardWidgetsTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
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
     * Verifies that an alert with an icon places its texts beside the icon, and one without an
     * icon at its padding.
     */
    @Test
    fun `alert places texts beside its icon`() {
        val withIcon = laidOut(AlertNode("a", icon = "info", children = listOf(TextNode("t", kind = TextKind.ALERT_TITLE, text = "T"))), Rect(0, 0, 200, 40))
        val without = laidOut(AlertNode("b", children = listOf(TextNode("u", kind = TextKind.ALERT_TITLE, text = "T"))), Rect(0, 0, 200, 40))

        assertEquals(AlertWidget.PADDING_X + AlertWidget.ICON_SPACE, WidgetTree.find(withIcon, "t")!!.bounds.x)
        assertEquals(AlertWidget.PADDING_X, WidgetTree.find(without, "u")!!.bounds.x)
        assertEquals(200 - 2 * AlertWidget.PADDING_X - AlertWidget.ICON_SPACE, WidgetTree.find(withIcon, "t")!!.bounds.width)
    }

    /**
     * Verifies that a destructive alert draws its title and description in the destructive
     * colour, and a default alert its description muted.
     */
    @Test
    fun `alert variants tint their texts`() {
        val tokens = Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)

        assertEquals(tokens.destructive, AlertWidget.textColor(tokens, AlertVariant.DESTRUCTIVE, TextKind.ALERT_TITLE))
        assertEquals(tokens.cardForeground, AlertWidget.textColor(tokens, AlertVariant.DEFAULT, TextKind.ALERT_TITLE))
        assertEquals(tokens.mutedForeground, AlertWidget.textColor(tokens, AlertVariant.DEFAULT, TextKind.ALERT_DESCRIPTION))
    }

    /**
     * Verifies that a card header places its action at the top right, beside its title and
     * description.
     */
    @Test
    fun `card header places its action at the top right`() {
        val card = laidOut(
            CardNode(
                "card",
                width = Sizing.fixed(200),
                children = listOf(
                    CardHeaderNode(
                        "header",
                        children = listOf(
                            TextNode("title", kind = TextKind.CARD_TITLE, text = "Title"),
                            TextNode("description", kind = TextKind.CARD_DESCRIPTION, text = "Description"),
                            CardActionNode("action", children = listOf(ButtonNode("more", text = "More"))),
                        ),
                    ),
                    CardContentNode("content", children = listOf(TextNode("body", text = "Body"))),
                    CardFooterNode("footer", children = listOf(ButtonNode("save", text = "Save"))),
                ),
            ),
            Rect(0, 0, 200, 200),
        )

        val action = WidgetTree.find(card, "action")!!.bounds
        val title = WidgetTree.find(card, "title")!!.bounds
        val description = WidgetTree.find(card, "description")!!.bounds
        assertEquals(200 - CardWidget.PADDING_X, action.right)
        assertEquals(CardWidget.PADDING_Y, action.y)
        assertEquals(CardWidget.PADDING_X, title.x)
        assertEquals(CardWidget.PADDING_Y, title.y)
        assertEquals(title.x, description.x)
        assertEquals(action.x - CardHeaderWidget.ACTION_GAP - title.x, title.width)
        assertEquals(CardWidget.PADDING_X, WidgetTree.find(card, "body")!!.bounds.x)
        assertEquals(200 - 2 * CardWidget.PADDING_X, WidgetTree.find(card, "body")!!.bounds.width)
    }

    /**
     * Verifies that a card header without an action gives its texts the full width.
     */
    @Test
    fun `card header without action gives texts the full width`() {
        val card = laidOut(
            CardNode("card", width = Sizing.fixed(200), children = listOf(CardHeaderNode("header", children = listOf(TextNode("title", kind = TextKind.CARD_TITLE, text = "Title"))))),
            Rect(0, 0, 200, 200),
        )

        assertEquals(200 - 2 * CardWidget.PADDING_X, WidgetTree.find(card, "title")!!.bounds.width)
    }
}
