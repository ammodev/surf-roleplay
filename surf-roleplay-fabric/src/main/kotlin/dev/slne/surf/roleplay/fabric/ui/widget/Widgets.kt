package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.RoleplayTheme
import dev.slne.surf.roleplay.protocol.screen.DropdownOption
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

/**
 * A piece of styled text.
 *
 * @param id the id of the widget
 * @property text the text as component JSON
 */
class LabelWidget(id: String, var text: String = "") : Widget(id) {

    /**
     * Returns the size of the text on one line.
     *
     * @param measurer the text measurer
     * @return the text size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(measurer.width(text), measurer.lineHeight)

    /**
     * Draws the text, vertically centered in the widget.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        ui.text(text, bounds.x, bounds.y + (bounds.height - ui.lineHeight + 1) / 2, RoleplayTheme.TEXT)
    }

    /**
     * Replaces the text.
     *
     * @param json the new text as component JSON
     */
    override fun applyText(json: String) {
        text = json
    }
}

/**
 * A button that reports clicks to the screen.
 *
 * @param id the id of the widget
 * @property text the caption as component JSON
 */
class ButtonWidget(id: String, var text: String = "") : Widget(id) {

    /**
     * Returns the size of the caption with the button's padding.
     *
     * @param measurer the text measurer
     * @return the button size
     */
    override fun contentSize(measurer: TextMeasurer): Size =
        Size(measurer.width(text) + 2 * RoleplayTheme.WIDGET_PADDING, RoleplayTheme.WIDGET_HEIGHT)

    /**
     * Draws the button, highlighted under the mouse and dimmed when disabled.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val background = when {
            !enabled -> RoleplayTheme.WIDGET_DISABLED
            isOver(mouseX, mouseY) -> RoleplayTheme.WIDGET_HOVER
            else -> RoleplayTheme.WIDGET
        }
        ui.fill(bounds, background)
        ui.border(bounds, if (enabled && isOver(mouseX, mouseY)) RoleplayTheme.ACCENT else RoleplayTheme.WIDGET_BORDER)
        ui.clipped(bounds) {
            ui.centeredText(text, bounds, if (enabled) RoleplayTheme.TEXT else RoleplayTheme.TEXT_DISABLED)
        }
    }

    /**
     * Reports a left click on an enabled button to the screen.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on this button
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) context.buttonClicked(this)
        return true
    }

    /**
     * Replaces the caption.
     *
     * @param json the new caption as component JSON
     */
    override fun applyText(json: String) {
        text = json
    }
}

/**
 * A single-line text field.
 *
 * @param id the id of the widget
 * @property edit the text and cursor, edited under the field's filter
 * @property placeholder the hint shown while the field is empty, as component JSON
 * @property required whether an empty value is invalid
 */
open class TextInputWidget(
    id: String,
    val edit: TextEditState,
    var placeholder: String = "",
    val required: Boolean = false,
) : Widget(id) {

    /**
     * The index of the first character that is visible in the field.
     */
    private var scrollStart: Int = 0

    /**
     * Whether the field can be focused, which it can while enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * The field's text.
     */
    override val inputValue: String get() = edit.text

    /**
     * Whether the current text satisfies the field's constraints.
     */
    open val isValid: Boolean get() = !required || edit.text.isNotEmpty()

    /**
     * Returns the default input size.
     *
     * @param measurer the text measurer
     * @return the input size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(RoleplayTheme.INPUT_WIDTH, RoleplayTheme.WIDGET_HEIGHT)

    /**
     * Draws the field with its text or placeholder and, while focused, a blinking cursor. The
     * border shows focus and invalid values.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val focused = context.focusedWidget === this
        ui.fill(bounds, if (enabled) RoleplayTheme.INPUT else RoleplayTheme.WIDGET_DISABLED)
        ui.border(
            bounds,
            when {
                !isValid -> RoleplayTheme.INVALID
                focused -> RoleplayTheme.ACCENT
                else -> RoleplayTheme.WIDGET_BORDER
            },
        )

        val innerWidth = bounds.width - 2 * RoleplayTheme.WIDGET_PADDING
        val textX = bounds.x + RoleplayTheme.WIDGET_PADDING
        val textY = bounds.y + (bounds.height - ui.lineHeight + 1) / 2
        ui.clipped(Rect(textX, bounds.y, innerWidth.coerceAtLeast(0), bounds.height)) {
            if (edit.text.isEmpty()) {
                if (!focused) ui.text(placeholder, textX, textY, RoleplayTheme.TEXT_MUTED)
            } else {
                keepCursorVisible(ui, innerWidth)
                val visible = ui.font.plainSubstrByWidth(edit.text.substring(scrollStart), innerWidth)
                ui.plainText(visible, textX, textY, if (enabled) RoleplayTheme.TEXT else RoleplayTheme.TEXT_DISABLED)
            }
            if (focused && System.currentTimeMillis() / CURSOR_BLINK_MILLIS % 2 == 0L) {
                keepCursorVisible(ui, innerWidth)
                val cursorX = textX + ui.plainWidth(edit.text.substring(scrollStart, edit.cursor))
                ui.fill(Rect(cursorX, textY - 1, 1, ui.lineHeight + 1), RoleplayTheme.TEXT)
            }
        }
    }

    /**
     * Moves the visible part of the text so that the cursor is inside it.
     *
     * @param ui the graphics, used to measure text
     * @param innerWidth the width available for text
     */
    private fun keepCursorVisible(ui: UiGraphics, innerWidth: Int) {
        scrollStart = scrollStart.coerceIn(0, edit.cursor)
        while (scrollStart < edit.cursor && ui.plainWidth(edit.text.substring(scrollStart, edit.cursor)) > innerWidth) {
            scrollStart++
        }
    }

    /**
     * Focuses an enabled field when it is clicked, and places the cursor at the end of the text.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on this field
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (enabled) {
            context.focus(this)
            edit.cursor = edit.text.length
        }
        return true
    }

    /**
     * Handles cursor movement, deletion and pasting.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled) return false
        if (event.isPaste) {
            edit.insert(context.clipboard.replace("\n", "").replace("\r", ""))
            return true
        }
        when (event.key()) {
            GLFW.GLFW_KEY_BACKSPACE -> edit.backspace()
            GLFW.GLFW_KEY_DELETE -> edit.delete()
            GLFW.GLFW_KEY_LEFT -> edit.moveCursor(-1)
            GLFW.GLFW_KEY_RIGHT -> edit.moveCursor(1)
            GLFW.GLFW_KEY_HOME -> edit.cursor = 0
            GLFW.GLFW_KEY_END -> edit.cursor = edit.text.length
            else -> return false
        }
        return true
    }

    /**
     * Inserts a typed character at the cursor if the filter accepts it.
     *
     * @param context the screen showing the widget
     * @param event the character event
     * @return whether the character was handled
     */
    override fun charTyped(context: UiContext, event: CharacterEvent): Boolean {
        if (!enabled || !event.isAllowedChatCharacter) return false
        edit.insert(event.codepointAsString())
        return true
    }

    /**
     * Replaces the text.
     *
     * @param value the new text
     */
    override fun applyValue(value: String) {
        edit.text = value
    }

    /**
     * Replaces the placeholder.
     *
     * @param json the new placeholder as component JSON
     */
    override fun applyText(json: String) {
        placeholder = json
    }

    /**
     * Holds the cursor timing.
     */
    private companion object {
        /**
         * How long the cursor stays visible or hidden while blinking.
         */
        const val CURSOR_BLINK_MILLIS: Long = 500
    }
}

/**
 * A field for a whole number, which accepts only input within its range while typing.
 *
 * @param id the id of the widget
 * @param filter the number filter with the field's range
 * @param initial the initial number, or `null` for an empty field
 * @param required whether an empty value is invalid
 */
class NumberInputWidget(
    id: String,
    private val filter: NumberFilter,
    initial: Long? = null,
    required: Boolean = false,
) : TextInputWidget(id, TextEditState(initial?.toString() ?: "", filter), required = required) {

    /**
     * Whether the text is empty and optional, or a number within the range.
     */
    override val isValid: Boolean get() = filter.isValid(edit.text, required)
}

/**
 * A box that is checked or not, with a label next to it.
 *
 * @param id the id of the widget
 * @property label the label as component JSON
 * @property checked whether the box is checked
 */
class CheckboxWidget(id: String, var label: String = "", var checked: Boolean = false) : Widget(id) {

    /**
     * `true` if the box is checked, `false` otherwise.
     */
    override val inputValue: String get() = checked.toString()

    /**
     * Returns the size of the box and its label.
     *
     * @param measurer the text measurer
     * @return the checkbox size
     */
    override fun contentSize(measurer: TextMeasurer): Size {
        val labelWidth = measurer.width(label)
        val width = RoleplayTheme.CHECKBOX_SIZE + if (labelWidth > 0) LABEL_GAP + labelWidth else 0
        return Size(width, maxOf(RoleplayTheme.CHECKBOX_SIZE, measurer.lineHeight))
    }

    /**
     * Draws the box, filled when checked, and the label.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val size = RoleplayTheme.CHECKBOX_SIZE
        val box = Rect(bounds.x, bounds.y + (bounds.height - size) / 2, size, size)
        val hovered = enabled && isOver(mouseX, mouseY)
        ui.fill(box, if (enabled) RoleplayTheme.INPUT else RoleplayTheme.WIDGET_DISABLED)
        ui.border(box, if (hovered) RoleplayTheme.ACCENT else RoleplayTheme.WIDGET_BORDER)
        if (checked) ui.fill(Rect(box.x + 3, box.y + 3, size - 6, size - 6), if (enabled) RoleplayTheme.ACCENT else RoleplayTheme.TEXT_DISABLED)
        ui.text(label, box.right + LABEL_GAP, bounds.y + (bounds.height - ui.lineHeight + 1) / 2, if (enabled) RoleplayTheme.TEXT else RoleplayTheme.TEXT_DISABLED)
    }

    /**
     * Toggles an enabled box when it is left-clicked.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on this checkbox
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) checked = !checked
        return true
    }

    /**
     * Checks the box for `true` and unchecks it for any other value.
     *
     * @param value the new value
     */
    override fun applyValue(value: String) {
        checked = value == "true"
    }

    /**
     * Replaces the label.
     *
     * @param json the new label as component JSON
     */
    override fun applyText(json: String) {
        label = json
    }

    /**
     * Holds the spacing.
     */
    private companion object {
        /**
         * The space between the box and the label.
         */
        const val LABEL_GAP: Int = 4
    }
}

/**
 * A choice of one option, whose list opens above the screen when clicked.
 *
 * @param id the id of the widget
 * @property options the options, in display order
 * @property selected the value of the selected option, or `null` if none is selected
 * @property required whether having no selection is invalid
 */
class DropdownWidget(
    id: String,
    val options: List<DropdownOption> = emptyList(),
    var selected: String? = null,
    val required: Boolean = false,
) : Widget(id) {

    /**
     * The value of the selected option, or an empty string if none is selected.
     */
    override val inputValue: String get() = selected ?: ""

    /**
     * Whether the selection satisfies the widget's constraints.
     */
    val isValid: Boolean get() = !required || selected != null

    /**
     * The label of the selected option, or `null` if none is selected.
     */
    private val selectedLabel: String? get() = options.firstOrNull { it.value == selected }?.label

    /**
     * Returns the width of the widest option with the arrow, and the widget height.
     *
     * @param measurer the text measurer
     * @return the dropdown size
     */
    override fun contentSize(measurer: TextMeasurer): Size {
        val widest = options.maxOfOrNull { measurer.width(it.label) } ?: 0
        val width = maxOf(RoleplayTheme.INPUT_WIDTH, widest + 2 * RoleplayTheme.WIDGET_PADDING + measurer.plainWidth(ARROW) + 4)
        return Size(width, RoleplayTheme.WIDGET_HEIGHT)
    }

    /**
     * Draws the selected option, or a dash when nothing is selected, and an arrow.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val hovered = enabled && isOver(mouseX, mouseY)
        ui.fill(bounds, if (!enabled) RoleplayTheme.WIDGET_DISABLED else if (hovered) RoleplayTheme.WIDGET_HOVER else RoleplayTheme.WIDGET)
        ui.border(bounds, if (!isValid) RoleplayTheme.INVALID else if (hovered) RoleplayTheme.ACCENT else RoleplayTheme.WIDGET_BORDER)
        val textY = bounds.y + (bounds.height - ui.lineHeight + 1) / 2
        val color = if (enabled) RoleplayTheme.TEXT else RoleplayTheme.TEXT_DISABLED
        ui.clipped(bounds) {
            val label = selectedLabel
            if (label != null) {
                ui.text(label, bounds.x + RoleplayTheme.WIDGET_PADDING, textY, color)
            } else {
                ui.plainText("-", bounds.x + RoleplayTheme.WIDGET_PADDING, textY, RoleplayTheme.TEXT_MUTED)
            }
            ui.plainText(ARROW, bounds.right - RoleplayTheme.WIDGET_PADDING - ui.plainWidth(ARROW), textY, color)
        }
    }

    /**
     * Opens the option list of an enabled dropdown when it is left-clicked.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on this dropdown
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) context.openDropdown(this)
        return true
    }

    /**
     * Selects the option with the given value, or clears the selection for an empty or unknown
     * value.
     *
     * @param value the value of the option to select
     */
    override fun applyValue(value: String) {
        selected = value.takeIf { candidate -> options.any { it.value == candidate } }
    }

    /**
     * Holds the arrow glyph.
     */
    private companion object {
        /**
         * The glyph that marks the widget as a dropdown.
         */
        const val ARROW: String = "▼"
    }
}

/**
 * A texture drawn over the widget's area.
 *
 * @param id the id of the widget
 * @property texture the texture identifier
 */
class ImageWidget(id: String, var texture: String = "") : Widget(id) {

    /**
     * Returns the default image size.
     *
     * @param measurer the text measurer
     * @return the image size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(RoleplayTheme.IMAGE_SIZE, RoleplayTheme.IMAGE_SIZE)

    /**
     * Draws the texture over the widget's area.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        ui.image(texture, bounds)
    }
}

/**
 * A bar that shows how far something has progressed.
 *
 * @param id the id of the widget
 * @property progress the filled fraction, from `0` to `1`
 * @property label the text drawn over the bar as component JSON, or `null` for none
 */
class ProgressWidget(id: String, var progress: Float = 0f, var label: String? = null) : Widget(id) {

    /**
     * Returns the default bar size, widened to fit the label.
     *
     * @param measurer the text measurer
     * @return the bar size
     */
    override fun contentSize(measurer: TextMeasurer): Size {
        val labelWidth = label?.let { measurer.width(it) + 2 * RoleplayTheme.WIDGET_PADDING } ?: 0
        return Size(maxOf(RoleplayTheme.PROGRESS_WIDTH, labelWidth), maxOf(RoleplayTheme.PROGRESS_HEIGHT, measurer.lineHeight + 2))
    }

    /**
     * Draws the track, the filled part and the label.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        ui.fill(bounds, RoleplayTheme.INPUT)
        val filled = (bounds.width * progress.coerceIn(0f, 1f)).toInt()
        ui.fill(Rect(bounds.x, bounds.y, filled, bounds.height), RoleplayTheme.ACCENT)
        ui.border(bounds, RoleplayTheme.WIDGET_BORDER)
        label?.let { ui.centeredText(it, bounds, RoleplayTheme.TEXT) }
    }

    /**
     * Replaces the label.
     *
     * @param json the new label as component JSON
     */
    override fun applyText(json: String) {
        label = json
    }
}
