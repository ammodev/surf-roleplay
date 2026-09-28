package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.protocol.screen.ComboboxNode
import dev.slne.surf.roleplay.protocol.screen.NativeSelectNode
import dev.slne.surf.roleplay.protocol.screen.SelectGroup
import dev.slne.surf.roleplay.protocol.screen.SelectNode
import dev.slne.surf.roleplay.protocol.screen.SelectOption
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Tests for option lists, selects, native selects and comboboxes in the mod.
 */
class SelectWidgetsTest {

    /**
     * Two groups: fruit with a heading and a disabled pear, and vegetables without heading.
     */
    private val groups = listOf(
        SelectGroup("\"Obst\"", listOf(SelectOption("apple", "\"Apfel\""), SelectOption("pear", "\"Birne\"", enabled = false), SelectOption("plum", "\"Pflaume\""))),
        SelectGroup(options = listOf(SelectOption("carrot", "\"Möhre\""), SelectOption("pea", "\"Erbse\""))),
    )

    /**
     * The changes and searches reported, in order.
     */
    private val reports = mutableListOf<String>()

    /**
     * A context that tracks the focus and the open popover, and records reports.
     */
    private val context = object : UiContext {
        override var focusedWidget: Widget? = null
        override var popover: Popover? = null
        override fun focus(widget: Widget?) {
            focusedWidget = widget
        }
        override fun openPopover(popover: Popover) {
            this.popover = popover
        }
        override fun closePopover() {
            popover = null
        }
        override fun requestLayout() = Unit
        override fun actionTriggered(widget: Widget, submitsInput: Boolean) = Unit
        override fun valueChanged(widget: Widget, immediate: Boolean) {
            reports += "change:${widget.inputValue}"
        }
        override fun searchChanged(widget: Widget, query: String) {
            reports += "search:$query"
        }
        override var clipboard: String = ""
    }

    /**
     * Returns a key event for a key without modifiers.
     *
     * @param key the GLFW key code
     * @return the event
     */
    private fun key(key: Int) = KeyEvent(key, 0, 0)

    /**
     * Types a text into a widget character by character.
     *
     * @param widget the widget
     * @param text the text
     */
    private fun type(widget: Widget, text: String) = text.forEach { widget.charTyped(context, CharacterEvent(it.code)) }

    /**
     * Verifies that plain text is taken from component JSON, including children.
     */
    @Test
    fun `plain text reads component json`() {
        assertEquals("Apfelbaum", PlainText.of("""{"text":"Apfel","extra":[{"text":"baum"}]}"""))
        assertEquals("Birne", PlainText.of("\"Birne\""))
        assertEquals("", PlainText.of(""))
    }

    /**
     * Verifies that a query filters options by label ignoring case, keeping the headings of
     * matching groups and separators between them.
     */
    @Test
    fun `option lists filter by label`() {
        val list = OptionList(groups)
        assertEquals(7, list.rows.size)

        list.query = "r"
        assertEquals(
            listOf(OptionList.Heading("\"Obst\""), OptionList.Item(groups[0].options[1]), OptionList.Separator, OptionList.Item(groups[1].options[0]), OptionList.Item(groups[1].options[1])),
            list.rows,
        )
        list.query = "MÖH"
        assertEquals(listOf(OptionList.Item(groups[1].options[0])), list.rows)
        list.query = "xyz"
        assertEquals(emptyList(), list.rows)
        assertNull(list.highlightedOption)
    }

    /**
     * Verifies that the highlight skips headings, separators and disabled options and stops at
     * the ends.
     */
    @Test
    fun `option list highlight skips what cannot be chosen`() {
        val list = OptionList(groups)

        assertEquals("apple", list.highlightedOption?.value)
        list.moveHighlight(1)
        assertEquals("plum", list.highlightedOption?.value)
        list.moveHighlight(1)
        assertEquals("carrot", list.highlightedOption?.value)
        list.highlightEnd(last = true)
        list.moveHighlight(1)
        assertEquals("pea", list.highlightedOption?.value)
        list.moveHighlight(-1)
        list.moveHighlight(-1)
        list.moveHighlight(-1)
        list.moveHighlight(-1)
        assertEquals("apple", list.highlightedOption?.value)
    }

    /**
     * Verifies that a select opens its list on Down, and that the list chooses with the keyboard
     * and closes.
     */
    @Test
    fun `selects choose with the keyboard`() {
        val select = WidgetFactory.create(SelectNode("s", groups = groups, notifyChange = true)) as SelectWidget

        select.keyPressed(context, key(GLFW.GLFW_KEY_DOWN))
        val popover = assertIs<SelectPopover>(context.popover)
        popover.keyPressed(context, key(GLFW.GLFW_KEY_DOWN))
        popover.keyPressed(context, key(GLFW.GLFW_KEY_ENTER))

        assertEquals("plum", select.inputValue)
        assertNull(context.popover)
        assertEquals(listOf("change:plum"), reports)
    }

    /**
     * Verifies that a native select changes its selection with the arrow keys without opening
     * its list.
     */
    @Test
    fun `native selects step through options`() {
        val native = WidgetFactory.create(NativeSelectNode("n", groups = groups, selected = "apple")) as NativeSelectWidget

        native.keyPressed(context, key(GLFW.GLFW_KEY_DOWN))
        assertEquals("plum", native.inputValue)
        native.keyPressed(context, key(GLFW.GLFW_KEY_UP))
        assertEquals("apple", native.inputValue)
        assertNull(context.popover)
    }

    /**
     * Verifies that typing into a combobox filters and opens its list, and that Enter chooses the
     * highlighted option and closes a single combobox. A combobox without search events reports
     * no query.
     */
    @Test
    fun `comboboxes filter and choose`() {
        val combobox = WidgetFactory.create(ComboboxNode("c", groups = groups, notifyChange = true)) as ComboboxWidget

        type(combobox, "rb")
        assertSame(combobox, context.popover?.owner)
        assertEquals("pea", combobox.list.highlightedOption?.value)
        combobox.keyPressed(context, key(GLFW.GLFW_KEY_ENTER))

        assertEquals("pea", combobox.inputValue)
        assertNull(context.popover)
        assertEquals("", combobox.edit.text)
        assertEquals(listOf("change:pea"), reports)
    }

    /**
     * Verifies that a combobox with search events reports its query and leaves the filtering to
     * the server.
     */
    @Test
    fun `searching comboboxes report instead of filtering`() {
        val combobox = WidgetFactory.create(ComboboxNode("c", groups = groups, notifySearch = true)) as ComboboxWidget

        type(combobox, "xyz")

        assertEquals(listOf("search:x", "search:xy", "search:xyz"), reports)
        assertEquals(5, combobox.list.rows.count { it is OptionList.Item })
    }

    /**
     * Verifies that a multiple combobox toggles options with its list open, and that Backspace in
     * an empty field removes the last chip.
     */
    @Test
    fun `multiple comboboxes keep chips`() {
        val combobox = WidgetFactory.create(ComboboxNode("c", groups = groups, multiple = true, selected = listOf("carrot"))) as ComboboxWidget

        combobox.open(context)
        combobox.choose("apple", context)
        combobox.choose("plum", context)
        combobox.choose("apple", context)
        assertEquals("carrot,plum", combobox.inputValue)
        assertSame(combobox, context.popover?.owner)
        combobox.choose("pear", context)
        assertEquals("carrot,plum", combobox.inputValue)
        combobox.keyPressed(context, key(GLFW.GLFW_KEY_BACKSPACE))
        assertEquals("carrot", combobox.inputValue)
    }

    /**
     * Verifies that replacing a combobox's options keeps selected options that are missing from
     * the new groups.
     */
    @Test
    fun `replacing options keeps the selection`() {
        val combobox = WidgetFactory.create(ComboboxNode("c", groups = groups, multiple = true, selected = listOf("carrot"))) as ComboboxWidget

        combobox.replaceOptions(listOf(SelectGroup(options = listOf(SelectOption("kiwi", "\"Kiwi\"")))))

        assertEquals(listOf("kiwi", "carrot"), combobox.list.options.map { it.value })
        assertEquals("carrot", combobox.inputValue)
    }
}
