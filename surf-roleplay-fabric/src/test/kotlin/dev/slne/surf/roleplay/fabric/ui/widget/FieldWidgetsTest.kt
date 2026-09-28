package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.screen.ScreenPatcher
import dev.slne.surf.roleplay.fabric.ui.PanelStyle
import dev.slne.surf.roleplay.fabric.ui.ScreenPanel
import dev.slne.surf.roleplay.fabric.ui.ScreenPanelListener
import dev.slne.surf.roleplay.fabric.ui.theme.Themes
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.FieldNode
import dev.slne.surf.roleplay.protocol.screen.FieldTextKind
import dev.slne.surf.roleplay.protocol.screen.FieldTextNode
import dev.slne.surf.roleplay.protocol.screen.FormNode
import dev.slne.surf.roleplay.protocol.screen.SetInvalid
import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import dev.slne.surf.roleplay.protocol.screen.TextareaNode
import dev.slne.surf.roleplay.protocol.screen.ThemeVariant
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for forms, fields and server-side invalid marks in the mod.
 */
class FieldWidgetsTest {

    /**
     * The ids of the widgets whose actions were triggered, in order.
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
     * Creates a panel showing a form with a field around a text input, a textarea and the submit
     * button.
     *
     * @return the panel
     */
    private fun panel(): ScreenPanel = ScreenPanel(
        "{}",
        WidgetFactory.create(
            FormNode(
                "form",
                submitId = "send",
                children = listOf(
                    FieldNode(
                        "name_field",
                        children = listOf(FieldTextNode("label", text = "\"Name\"", forId = "name"), TextInputNode("name"), FieldTextNode("error", kind = FieldTextKind.ERROR)),
                    ),
                    TextareaNode("bio"),
                    ButtonNode("send"),
                ),
            ),
        ),
        true,
        listener,
        PanelStyle(Themes.resolve(Themes.DEFAULT, ThemeVariant.DARK)),
    )

    /**
     * Returns a key event for a key without modifiers.
     *
     * @param key the GLFW key code
     * @return the event
     */
    private fun key(key: Int) = KeyEvent(key, 0, 0)

    /**
     * Verifies that Enter in a single-line input of a form clicks the submit button, while Enter
     * in a textarea inserts a line break.
     */
    @Test
    fun `enter submits the form`() {
        val panel = panel()

        panel.focus(WidgetTree.find(panel.root, "name"))
        assertTrue(panel.keyPressed(key(GLFW.GLFW_KEY_ENTER)))
        assertEquals(listOf("send"), actions)

        panel.focus(WidgetTree.find(panel.root, "bio"))
        panel.keyPressed(key(GLFW.GLFW_KEY_ENTER))
        assertEquals(listOf("send"), actions)
        assertEquals("\n", WidgetTree.find(panel.root, "bio")!!.inputValue)
    }

    /**
     * Verifies that a server mark makes a field and its input invalid until the player changes
     * the input, and that an empty error text takes no space.
     */
    @Test
    fun `server marks show until the input changes`() {
        val panel = panel()
        val root = panel.root
        ScreenPatcher.apply(root, listOf(SetInvalid("name", true), SetInvalid("name_field", true)))
        val field = WidgetTree.find(root, "name_field") as FieldWidget
        val name = WidgetTree.find(root, "name") as TextInputWidget

        assertTrue(field.invalid)
        assertTrue(name.showsInvalid)
        name.charTyped(panel, CharacterEvent('a'.code))
        assertFalse(name.showsInvalid)
        ScreenPatcher.apply(root, listOf(SetInvalid("name_field", false)))
        assertFalse(field.invalid)
        assertEquals(0, WidgetTree.find(root, "error")!!.contentSize(measurer).height)
    }

    /**
     * A measurer in which every character is 5 pixels wide.
     */
    private val measurer = object : dev.slne.surf.roleplay.fabric.ui.TextMeasurer {
        override val lineHeight: Int = 9
        override fun width(json: String): Int = json.length * 5
        override fun plainWidth(text: String): Int = text.length * 5
    }
}
