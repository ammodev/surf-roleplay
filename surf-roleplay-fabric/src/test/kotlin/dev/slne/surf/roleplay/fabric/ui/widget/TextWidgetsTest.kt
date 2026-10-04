package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.protocol.screen.CheckboxNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.InputGroupAddonNode
import dev.slne.surf.roleplay.protocol.screen.InputGroupAlign
import dev.slne.surf.roleplay.protocol.screen.InputGroupNode
import dev.slne.surf.roleplay.protocol.screen.InputGroupTextNode
import dev.slne.surf.roleplay.protocol.screen.InputOtpNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.OtpPattern
import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import dev.slne.surf.roleplay.protocol.screen.TextInputType
import dev.slne.surf.roleplay.protocol.screen.TextareaNode
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for password and email fields, textareas, one-time code inputs, input groups and label
 * targets in the mod.
 */
class TextWidgetsTest {

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }

    /**
     * The tree the context resolves label targets in.
     */
    private var tree: Widget? = null

    /**
     * A context that tracks the focus and resolves widgets in [tree].
     */
    private val context = object : UiContext {
        override var focusedWidget: Widget? = null
        override fun focus(widget: Widget?) {
            focusedWidget = widget
        }
        override fun requestLayout() = Unit
        override fun actionTriggered(widget: Widget, submitsInput: Boolean) = Unit
        override fun widget(id: String): Widget? = tree?.let { WidgetTree.find(it, id) }
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
     * Verifies that a password field draws one mask character per character and keeps its value.
     */
    @Test
    fun `passwords are masked`() {
        val field = WidgetFactory.create(TextInputNode("p", value = "geheim", inputType = TextInputType.PASSWORD)) as TextInputWidget

        assertEquals("••••••", field.shownText)
        assertEquals("geheim", field.inputValue)
    }

    /**
     * Verifies that an email field shows an invalid address as invalid once touched.
     */
    @Test
    fun `email fields check the address shape`() {
        val field = WidgetFactory.create(TextInputNode("m", inputType = TextInputType.EMAIL)) as TextInputWidget
        field.touched = true

        assertFalse(field.showsInvalid)
        field.applyValue("max")
        assertTrue(field.showsInvalid)
        field.applyValue("max@example.de")
        assertFalse(field.showsInvalid)
    }

    /**
     * Verifies that text wraps at word boundaries, breaks long words and keeps line breaks.
     */
    @Test
    fun `text wraps into lines`() {
        val lines = TextLines.wrap("aa bb cc\n\nddddddd", 25, measurer::plainWidth)

        assertEquals(
            listOf(TextLines.Line(0, 5), TextLines.Line(6, 8), TextLines.Line(9, 9), TextLines.Line(10, 15), TextLines.Line(15, 17)),
            lines,
        )
    }

    /**
     * Verifies that Enter inserts a line break, the arrow keys move between lines, and pasted
     * text keeps its line breaks.
     */
    @Test
    fun `textareas edit several lines`() {
        val area = WidgetFactory.create(TextareaNode("a", value = "abc")) as TextareaWidget
        area.bounds = Rect(0, 0, 100, 40)
        area.layoutLines(measurer)

        area.keyPressed(context, key(GLFW.GLFW_KEY_ENTER))
        area.charTyped(context, CharacterEvent('d'.code))
        assertEquals("abc\nd", area.inputValue)
        area.layoutLines(measurer)
        area.keyPressed(context, key(GLFW.GLFW_KEY_UP))
        assertEquals(1, area.edit.cursor)
        area.keyPressed(context, key(GLFW.GLFW_KEY_DOWN))
        assertEquals(5, area.edit.cursor)
        area.paste(context, "x\r\ny")
        assertEquals("abc\ndx\ny", area.inputValue)
        assertTrue(area.touched)
    }

    /**
     * Verifies that a drag after a cut, with no layout in between, neither throws nor leaves the
     * cursor outside the text.
     */
    @Test
    fun `textareas drag safely after an edit without a layout`() {
        val area = WidgetFactory.create(TextareaNode("a", value = "aaaa bbbb cccc dddd eeee ffff")) as TextareaWidget
        area.bounds = Rect(0, 0, 60, 40)
        area.layoutLines(measurer)
        area.mouseClicked(context, 10.0, 12.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)
        area.mouseDragged(context, 50.0, 30.0)
        area.keyPressed(context, TestKeys.key(GLFW.GLFW_KEY_X, shortcut = true))

        area.mouseDragged(context, 50.0, 30.0)

        assertTrue(area.edit.cursor in 0..area.inputValue.length)
    }

    /**
     * Verifies that End after an edit in the same frame moves to the end of the line the edited
     * text has.
     */
    @Test
    fun `textareas find the drawn line after an edit without a layout`() {
        val area = WidgetFactory.create(TextareaNode("a", value = "abc")) as TextareaWidget
        area.bounds = Rect(0, 0, 100, 40)
        area.layoutLines(measurer)
        area.keyPressed(context, key(GLFW.GLFW_KEY_ENTER))
        area.charTyped(context, CharacterEvent('d'.code))
        area.edit.moveCursorTo(4, extend = false)

        area.keyPressed(context, key(GLFW.GLFW_KEY_END))

        assertEquals(5, area.edit.cursor)
    }

    /**
     * Verifies that a textarea keeps to its maximum length.
     */
    @Test
    fun `textareas keep their maximum length`() {
        val area = WidgetFactory.create(TextareaNode("a", maxLength = 3)) as TextareaWidget

        area.paste(context, "abcdef")

        assertEquals("abc", area.inputValue)
    }

    /**
     * Verifies that a one-time code fills slot by slot, refuses characters outside its pattern and
     * extra characters, and that paste replaces the code with its accepted characters.
     */
    @Test
    fun `one-time codes fill slot by slot`() {
        val otp = WidgetFactory.create(InputOtpNode("o", length = 4, groups = listOf(2, 2))) as InputOtpWidget

        "1a23".forEach { otp.charTyped(context, CharacterEvent(it.code)) }
        assertEquals("123", otp.inputValue)
        assertEquals(3, otp.activeSlot)
        otp.charTyped(context, CharacterEvent('4'.code))
        otp.charTyped(context, CharacterEvent('5'.code))
        assertEquals("1234", otp.inputValue)
        otp.keyPressed(context, key(GLFW.GLFW_KEY_BACKSPACE))
        assertEquals("123", otp.inputValue)
        otp.paste(context, " 98-7 6 5")
        assertEquals("9876", otp.inputValue)

        val letters = WidgetFactory.create(InputOtpNode("l", length = 3, pattern = OtpPattern.ALPHANUMERIC)) as InputOtpWidget
        letters.paste(context, "a-B1")
        assertEquals("aB1", letters.inputValue)
    }

    /**
     * Verifies that a one-time code is valid only when empty and optional or complete.
     */
    @Test
    fun `one-time codes are valid only when complete`() {
        val otp = WidgetFactory.create(InputOtpNode("o", length = 2, required = true)) as InputOtpWidget
        otp.touched = true

        assertTrue(otp.showsInvalid)
        otp.paste(context, "1")
        assertTrue(otp.showsInvalid)
        otp.paste(context, "12")
        assertFalse(otp.showsInvalid)
    }

    /**
     * Verifies that an input group places its addons around the control, embeds the control and
     * frames its focus with the group.
     */
    @Test
    fun `input groups embed their control`() {
        val group = WidgetFactory.create(
            InputGroupNode(
                "g",
                children = listOf(
                    InputGroupAddonNode("top", align = InputGroupAlign.BLOCK_START, children = listOf(InputGroupTextNode("t", text = "\"T\""))),
                    InputGroupAddonNode("start", children = listOf(InputGroupTextNode("s", text = "\"@\""))),
                    TextInputNode("q"),
                    InputGroupAddonNode("end", align = InputGroupAlign.INLINE_END),
                ),
            ),
        ) as InputGroupWidget
        group.createLayout(measurer)
        group.applyLayout()

        val control = WidgetTree.find(group, "q")!!
        assertSame(control, group.control)
        assertTrue(control.embedded)
        assertSame(group, control.focusFrame)
        assertEquals(listOf("top", "g#row"), group.children.map { it.id })
        assertEquals(listOf("start", "q", "end"), group.children[1].children.map { it.id })
    }

    /**
     * Verifies that clicking a label focuses its target, and checks a target checkbox.
     */
    @Test
    fun `labels focus their target`() {
        val root = WidgetFactory.create(
            ColumnNode(
                "root",
                children = listOf(LabelNode("l", text = "\"Name\"", forId = "name"), TextInputNode("name"), LabelNode("cl", text = "\"AGB\"", forId = "box"), CheckboxNode("box")),
            ),
        )
        tree = root
        val label = WidgetTree.find(root, "l")!!
        label.bounds = Rect(0, 0, 20, 9)
        val checkLabel = WidgetTree.find(root, "cl")!!
        checkLabel.bounds = Rect(0, 20, 20, 9)

        assertTrue(label.mouseClicked(context, 5.0, 5.0, GLFW.GLFW_MOUSE_BUTTON_LEFT))
        assertSame(WidgetTree.find(root, "name"), context.focusedWidget)
        checkLabel.mouseClicked(context, 5.0, 25.0, GLFW.GLFW_MOUSE_BUTTON_LEFT)
        assertEquals("true", WidgetTree.find(root, "box")!!.inputValue)
    }
}
