package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.screen.ScreenPatcher
import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.ChatMessageNode
import dev.slne.surf.roleplay.protocol.screen.ChatViewNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.InsertNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TextKind
import dev.slne.surf.roleplay.protocol.screen.TextNode
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for chat views in the mod.
 */
class ChatWidgetsTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * A listener that ignores everything.
     */
    private val listener = object : ScreenPanelListener {
        override fun actionTriggered(panel: ScreenPanel, widget: Widget) = Unit
        override fun closeRequested(panel: ScreenPanel) = Unit
    }

    /**
     * Creates a message node with a text.
     *
     * @param id the id of the message
     * @param own whether the message is the player's own
     * @return the node
     */
    private fun message(id: String, own: Boolean) = ChatMessageNode(
        id,
        own = own,
        fallback = "LS",
        name = "\"Leitstelle\"",
        time = "\"12:30\"",
        children = listOf(TextNode("${id}_text", kind = TextKind.P, text = "\"Hallo $id\"")),
    )

    /**
     * Creates a laid-out panel with a chat view 200 wide and 80 tall holding messages.
     *
     * @param count the number of messages
     * @return the panel
     */
    private fun panel(count: Int): ScreenPanel {
        val view = ChatViewNode("chat", width = Sizing.fixed(200), height = Sizing.fixed(80), children = (0 until count).map { message("m$it", own = it % 2 == 1) })
        return ScreenPanel("T", WidgetFactory.create(ColumnNode("root", children = listOf(view))), true, listener, PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)))
            .also { it.layoutIfNeeded(measurer, 500, 400) }
    }

    /**
     * Returns the widget with an id.
     *
     * @param panel the panel
     * @param id the id
     * @return the widget
     */
    private fun widget(panel: ScreenPanel, id: String): Widget = WidgetTree.find(panel.root, id)!!

    /**
     * Appends a message by patch and lays the panel out again.
     *
     * @param panel the panel
     * @param id the id of the message
     */
    private fun append(panel: ScreenPanel, id: String) {
        ScreenPatcher.apply(panel.root, listOf(InsertNode("chat", Int.MAX_VALUE, message(id, own = false))))
        panel.requestLayout()
        panel.layoutIfNeeded(measurer, 500, 400)
    }

    /**
     * Verifies that messages of others have an avatar at the left and their bubble after it,
     * while the player's own messages have no avatar and their bubble at the right.
     */
    @Test
    fun `bubbles sit on their side`() {
        val panel = panel(2)
        val view = widget(panel, "chat").bounds

        assertIs<AvatarWidget>(widget(panel, "m0:avatar"))
        assertEquals(view.x + ChatViewWidget.PADDING, widget(panel, "m0:avatar").bounds.x)
        assertTrue(widget(panel, "m0:bubble").bounds.x > widget(panel, "m0:avatar").bounds.right)
        assertEquals(null, WidgetTree.find(panel.root, "m1:avatar"))
        assertEquals(view.right - ChatViewWidget.PADDING, widget(panel, "m1:bubble").bounds.right)
        assertTrue(assertIs<ChatBubbleWidget>(widget(panel, "m1:bubble")).own)
    }

    /**
     * Verifies that a long conversation starts at its newest message and stays there when a
     * message is appended.
     */
    @Test
    fun `views stick to the newest message`() {
        val panel = panel(6)
        val view = assertIs<ChatViewWidget>(widget(panel, "chat"))

        assertTrue(view.maxScrollY > 0)
        assertEquals(view.maxScrollY, view.scrollY)
        val before = view.maxScrollY
        append(panel, "m6")
        assertTrue(view.maxScrollY > before)
        assertEquals(view.maxScrollY, view.scrollY)
        assertTrue(widget(panel, "m6").bounds.bottom <= view.bounds.bottom)
    }

    /**
     * Verifies that a view scrolled up keeps its position when a message is appended.
     */
    @Test
    fun `scrolled views keep their position`() {
        val panel = panel(6)
        val view = assertIs<ChatViewWidget>(widget(panel, "chat"))
        val b = view.bounds
        panel.mouseScrolled(b.x + b.width / 2.0, b.y + b.height / 2.0, 2.0)
        panel.layoutIfNeeded(measurer, 500, 400)
        val position = view.scrollY
        assertTrue(position < view.maxScrollY)

        append(panel, "m6")
        assertEquals(position, view.scrollY)
    }
}
