package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.protocol.screen.CommandEmptyNode
import dev.slne.surf.roleplay.protocol.screen.CommandGroupNode
import dev.slne.surf.roleplay.protocol.screen.CommandInputNode
import dev.slne.surf.roleplay.protocol.screen.CommandItemNode
import dev.slne.surf.roleplay.protocol.screen.CommandListNode
import dev.slne.surf.roleplay.protocol.screen.CommandNode
import dev.slne.surf.roleplay.protocol.screen.CommandSeparatorNode
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the command menu in the mod.
 */
class CommandWidgetsTest {

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
     * The queries reported as searches, in order.
     */
    private val searches = mutableListOf<String>()

    /**
     * A context that records actions and searches and tracks the focus.
     */
    private val context = object : UiContext {
        override var focusedWidget: Widget? = null
        override fun focus(widget: Widget?) {
            focusedWidget = widget
        }
        override fun requestLayout() = Unit
        override fun actionTriggered(widget: Widget, submitsInput: Boolean) {
            actions += widget.id
        }
        override fun searchChanged(widget: Widget, query: String) {
            searches += query
        }
        override var clipboard: String = ""
    }

    /**
     * Creates a command menu with two groups, a separator and an empty text.
     *
     * @param notifySearch whether the command reports its searches
     * @return the command widget
     */
    private fun command(notifySearch: Boolean = false): CommandWidget = WidgetFactory.create(
        CommandNode(
            "command",
            notifySearch = notifySearch,
            children = listOf(
                CommandInputNode("input", placeholder = "Suchen"),
                CommandListNode(
                    "list",
                    children = listOf(
                        CommandEmptyNode("empty", text = "Nichts gefunden"),
                        CommandGroupNode(
                            "suggestions",
                            heading = "Vorschläge",
                            children = listOf(CommandItemNode("calendar", text = "Kalender"), CommandItemNode("search", text = "Suche", keywords = listOf("finden"))),
                        ),
                        CommandSeparatorNode("sep"),
                        CommandGroupNode("settings", heading = "Einstellungen", children = listOf(CommandItemNode("profile", text = "Profil"), CommandItemNode("off", text = "Aus", enabled = false))),
                    ),
                ),
            ),
        ),
    ) as CommandWidget

    /**
     * Types a text into the command input.
     *
     * @param command the command
     * @param text the text
     */
    private fun type(command: CommandWidget, text: String) {
        val input = WidgetTree.find(command, "input")!!
        context.focus(input)
        text.forEach { input.charTyped(context, CharacterEvent(it.code)) }
    }

    /**
     * Returns whether a widget of the command is hidden.
     *
     * @param command the command
     * @param id the id of the widget
     * @return whether it is hidden
     */
    private fun hidden(command: CommandWidget, id: String) = WidgetTree.find(command, id)!!.hidden

    /**
     * Verifies the matching of queries against texts and keywords.
     */
    @Test
    fun `filter matches texts and keywords`() {
        assertTrue(CommandFilter.matches("", "Kalender", emptyList()))
        assertTrue(CommandFilter.matches("kal", "Kalender", emptyList()))
        assertTrue(CommandFilter.matches("FIN", "Suche", listOf("finden")))
        assertTrue(CommandFilter.matches("pro fil", "Profil", emptyList()))
        assertFalse(CommandFilter.matches("xyz", "Kalender", emptyList()))
    }

    /**
     * Verifies that typing hides items that do not match, groups without matches and the
     * separator, and that the empty text shows only when nothing matches.
     */
    @Test
    fun `typing filters the items`() {
        val command = command()
        assertTrue(hidden(command, "empty"))

        type(command, "kal")
        assertFalse(hidden(command, "calendar"))
        assertTrue(hidden(command, "search"))
        assertTrue(hidden(command, "settings"))
        assertTrue(hidden(command, "sep"))
        assertTrue(hidden(command, "empty"))

        type(command, "xx")
        assertFalse(hidden(command, "empty"))
        assertTrue(hidden(command, "suggestions"))
    }

    /**
     * Verifies that hidden items take no space.
     */
    @Test
    fun `hidden items take no space`() {
        val command = command()
        val full = FlexLayout.measure(command.createLayout(measurer)).height

        type(command, "kal")

        assertTrue(FlexLayout.measure(command.createLayout(measurer)).height < full)
    }

    /**
     * Verifies that Up and Down in the input move the highlight through the shown enabled items,
     * and Enter fires the highlighted one.
     */
    @Test
    fun `keys move the highlight and choose`() {
        val command = command()
        val input = WidgetTree.find(command, "input")!!
        context.focus(input)
        assertEquals("calendar", command.highlighted?.id)

        input.keyPressed(context, KeyEvent(GLFW.GLFW_KEY_DOWN, 0, 0))
        input.keyPressed(context, KeyEvent(GLFW.GLFW_KEY_DOWN, 0, 0))
        input.keyPressed(context, KeyEvent(GLFW.GLFW_KEY_DOWN, 0, 0))
        assertEquals("profile", command.highlighted?.id)
        input.keyPressed(context, KeyEvent(GLFW.GLFW_KEY_UP, 0, 0))
        assertEquals("search", command.highlighted?.id)
        input.keyPressed(context, KeyEvent(GLFW.GLFW_KEY_ENTER, 0, 0))

        assertEquals(listOf("search"), actions)
    }

    /**
     * Verifies that the highlight moves to the first match when the highlighted item is filtered
     * away, and that nothing is highlighted without matches.
     */
    @Test
    fun `highlight follows the filter`() {
        val command = command()

        type(command, "pro")
        assertEquals("profile", command.highlighted?.id)
        type(command, "zz")
        assertNull(command.highlighted)
    }

    /**
     * Verifies that a command that reports its searches leaves the filtering to the server, and
     * that its input is not an input of the screen.
     */
    @Test
    fun `server search skips client filtering`() {
        val command = command(notifySearch = true)

        type(command, "kal")

        assertFalse(hidden(command, "search"))
        assertEquals(listOf("k", "ka", "kal"), searches)
        assertNull(WidgetTree.find(command, "input")!!.inputValue)
    }
}
