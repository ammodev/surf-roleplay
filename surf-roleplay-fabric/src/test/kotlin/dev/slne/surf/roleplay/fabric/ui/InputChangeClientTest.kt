package dev.slne.surf.roleplay.fabric.ui

import dev.slne.surf.roleplay.fabric.ui.widget.ButtonWidget
import dev.slne.surf.roleplay.fabric.ui.widget.CheckboxWidget
import dev.slne.surf.roleplay.fabric.ui.widget.DropdownWidget
import dev.slne.surf.roleplay.fabric.ui.widget.TextInputWidget
import dev.slne.surf.roleplay.fabric.ui.widget.UiContext
import dev.slne.surf.roleplay.fabric.ui.widget.Widget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetFactory
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetTree
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.CheckboxNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import net.minecraft.client.input.CharacterEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for change reporting, the touched state and [ChangeDebouncer].
 */
class InputChangeClientTest {

    /**
     * The widgets reported as changed, in order.
     */
    private val reported = mutableListOf<String>()

    /**
     * A context that records change reports.
     */
    private val context = object : UiContext {
        override val focusedWidget: Widget? = null
        override fun focus(widget: Widget?) = Unit
        override fun openDropdown(dropdown: DropdownWidget) = Unit
        override fun closeDropdown() = Unit
        override fun requestLayout() = Unit
        override fun actionTriggered(widget: Widget, submitsInput: Boolean) = Unit
        override fun valueChanged(widget: Widget, immediate: Boolean) {
            reported += "${widget.id}:$immediate"
        }
        override var clipboard: String = ""
    }

    /**
     * A tree with a watched text input, an unwatched text input and a watched checkbox.
     */
    private val root = WidgetFactory.create(
        ColumnNode(
            "root",
            children = listOf(
                TextInputNode("watched", notifyChange = true, required = true),
                TextInputNode("plain", required = true),
                CheckboxNode("box", notifyChange = true),
                ButtonNode("submit"),
            ),
        ),
    )

    /**
     * Verifies that typing into a watched input reports a debounced change, that unwatched inputs
     * report nothing, and that a checkbox reports at once.
     */
    @Test
    fun `watched inputs report changes`() {
        (WidgetTree.find(root, "watched") as TextInputWidget).charTyped(context, CharacterEvent('a'.code))
        (WidgetTree.find(root, "plain") as TextInputWidget).charTyped(context, CharacterEvent('b'.code))
        (WidgetTree.find(root, "box") as CheckboxWidget).toggle(context)

        assertEquals(listOf("watched:false", "box:true"), reported)
    }

    /**
     * Verifies that inputs are untouched until they change, and that a required input shows as
     * invalid only once touched.
     */
    @Test
    fun `invalid state shows only after a change`() {
        val plain = WidgetTree.find(root, "plain") as TextInputWidget

        assertFalse(plain.touched)
        assertFalse(plain.showsInvalid)
        plain.charTyped(context, CharacterEvent('x'.code))
        plain.edit.backspace()
        plain.touched = true

        assertTrue(plain.showsInvalid)
    }

    /**
     * Verifies that marking a tree as submitted touches every input.
     */
    @Test
    fun `submitting touches every input`() {
        WidgetTree.touchAll(root)

        assertTrue((WidgetTree.find(root, "plain") as TextInputWidget).showsInvalid)
    }

    /**
     * Verifies that the debouncer releases a widget only after it stayed unchanged long enough,
     * and releases every widget at once when flushed.
     */
    @Test
    fun `debouncer waits for a pause`() {
        var now = 0L
        val debouncer = ChangeDebouncer(250) { now }

        debouncer.changed("a")
        now = 200
        assertEquals(emptyList(), debouncer.due())
        debouncer.changed("a")
        now = 400
        assertEquals(emptyList(), debouncer.due())
        now = 450
        assertEquals(listOf("a"), debouncer.due())
        assertEquals(emptyList(), debouncer.due())
        debouncer.changed("b")
        assertEquals(listOf("b"), debouncer.flush())
    }
}
