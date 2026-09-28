package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.protocol.screen.ButtonSize
import dev.slne.surf.roleplay.protocol.screen.ButtonVariant
import dev.slne.surf.roleplay.protocol.screen.DropdownOption
import dev.slne.surf.roleplay.protocol.screen.IconColor
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

/**
 * A piece of styled text, optionally led by an icon.
 *
 * @param id the id of the widget
 * @property text the text as component JSON
 * @property icon the Lucide name of the leading icon, or `null` for none
 */
class LabelWidget(id: String, var text: String = "", var icon: String? = null) : Widget(id) {

    /**
     * Returns the size of the icon and the text on one line.
     *
     * @param measurer the text measurer
     * @return the text size
     */
    override fun contentSize(measurer: TextMeasurer): Size =
        Size(iconSpace(icon, measurer.width(text)) + measurer.width(text), maxOf(measurer.lineHeight, if (icon != null) UiMetrics.INLINE_ICON else 0))

    /**
     * Draws the text, vertically centered in the widget.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val color = ui.tokens.foreground
        val textX = drawLeadingIcon(ui, icon, bounds.x, bounds, color, ui.width(text))
        ui.text(text, textX, bounds.y + (bounds.height - ui.lineHeight + 1) / 2, color)
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
 * A button that reports clicks to the screen, optionally with an icon before its caption.
 *
 * @param id the id of the widget
 * @property text the caption as component JSON
 * @property icon the Lucide name of the leading icon, or `null` for none
 * @property submitsInput whether a click marks every input of the screen as touched
 * @property variant the look of the button
 * @property size the size of the button
 */
class ButtonWidget(
    id: String,
    var text: String = "",
    var icon: String? = null,
    val submitsInput: Boolean = true,
    val variant: ButtonVariant = ButtonVariant.DEFAULT,
    val size: ButtonSize = ButtonSize.DEFAULT,
) : Widget(id) {

    /**
     * Whether the widget can take the keyboard focus, which it can while enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * Returns the size of the caption with the button's padding.
     *
     * @param measurer the text measurer
     * @return the button size
     */
    override fun contentSize(measurer: TextMeasurer): Size = ButtonStyle.size(size, icon, measurer.width(text))

    /**
     * Draws the button, highlighted under the mouse and dimmed when disabled.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        ButtonStyle.draw(ui, bounds, corners, variant, size, text, icon, enabled, isOver(mouseX, mouseY), pressed = false)
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
        if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            context.focus(this)
            context.actionTriggered(this, submitsInput)
        }
        return true
    }

    /**
     * Activates the button on Enter or Space.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled || !isActivation(event)) return false
        context.actionTriggered(this, submitsInput)
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
 * @property icon the Lucide name of an icon drawn at the start of the field, or `null` for none
 */
open class TextInputWidget(
    id: String,
    val edit: TextEditState,
    var placeholder: String = "",
    val required: Boolean = false,
    var icon: String? = null,
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
     * Whether the field shows itself as invalid: only once it was touched.
     */
    override val showsInvalid: Boolean get() = touched && !isValid

    /**
     * Returns the default input size.
     *
     * @param measurer the text measurer
     * @return the input size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(UiMetrics.INPUT_WIDTH, UiMetrics.WIDGET_HEIGHT)

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
        val tokens = ui.tokens
        ui.fillRounded(bounds, inputFill(ui))
        ui.borderRounded(
            bounds,
            when {
                showsInvalid -> tokens.destructive
                focused -> tokens.ring
                else -> tokens.input
            },
        )

        val iconWidth = if (icon != null) UiMetrics.INLINE_ICON + UiMetrics.ICON_GAP else 0
        icon?.let { name ->
            val size = UiMetrics.INLINE_ICON
            ui.icon(name, Rect(bounds.x + UiMetrics.WIDGET_PADDING, bounds.y + (bounds.height - size) / 2, size, size), tokens.mutedForeground)
        }
        val innerWidth = bounds.width - 2 * UiMetrics.WIDGET_PADDING - iconWidth
        val textX = bounds.x + UiMetrics.WIDGET_PADDING + iconWidth
        val textY = bounds.y + (bounds.height - ui.lineHeight + 1) / 2
        ui.clipped(Rect(textX, bounds.y, innerWidth.coerceAtLeast(0), bounds.height)) {
            if (edit.text.isEmpty()) {
                if (!focused) ui.text(placeholder, textX, textY, tokens.mutedForeground)
            } else {
                keepCursorVisible(ui, innerWidth)
                val visible = ui.font.plainSubstrByWidth(edit.text.substring(scrollStart), innerWidth)
                ui.plainText(visible, textX, textY, if (enabled) tokens.foreground else ui.disabled(tokens.foreground))
            }
            if (focused && System.currentTimeMillis() / CURSOR_BLINK_MILLIS % 2 == 0L) {
                keepCursorVisible(ui, innerWidth)
                val cursorX = textX + ui.plainWidth(edit.text.substring(scrollStart, edit.cursor))
                ui.fill(Rect(cursorX, textY - 1, 1, ui.lineHeight + 1), tokens.foreground)
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
            if (edit.insert(context.clipboard.replace("\n", "").replace("\r", ""))) markChanged(context, immediate = false)
            return true
        }
        when (event.key()) {
            GLFW.GLFW_KEY_BACKSPACE -> if (edit.backspace()) markChanged(context, immediate = false)
            GLFW.GLFW_KEY_DELETE -> if (edit.delete()) markChanged(context, immediate = false)
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
        if (edit.insert(event.codepointAsString())) markChanged(context, immediate = false)
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
     * Whether the widget can take the keyboard focus, which it can while enabled.
     */
    override val focusable: Boolean get() = enabled

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
        val width = UiMetrics.CHECKBOX_SIZE + if (labelWidth > 0) LABEL_GAP + labelWidth else 0
        return Size(width, maxOf(UiMetrics.CHECKBOX_SIZE, measurer.lineHeight))
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
        val size = UiMetrics.CHECKBOX_SIZE
        val box = Rect(bounds.x, bounds.y + (bounds.height - size) / 2, size, size)
        val hovered = enabled && isOver(mouseX, mouseY)
        val tokens = ui.tokens
        val radius = CHECKBOX_RADIUS
        if (checked) {
            ui.fillRounded(box, if (enabled) tokens.primary else ui.disabled(tokens.primary), radius)
            drawCheck(ui, box, if (enabled) tokens.primaryForeground else ui.disabled(tokens.primaryForeground))
        } else {
            ui.fillRounded(box, inputFill(ui), radius)
            ui.borderRounded(box, if (hovered) tokens.ring else tokens.input, radius)
        }
        ui.text(label, box.right + LABEL_GAP, bounds.y + (bounds.height - ui.lineHeight + 1) / 2, if (enabled) tokens.foreground else ui.disabled(tokens.foreground))
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
        if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            context.focus(this)
            toggle(context)
        }
        return true
    }

    /**
     * Toggles the box on Space.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled || event.key() != GLFW.GLFW_KEY_SPACE) return false
        toggle(context)
        return true
    }

    /**
     * Toggles the box and reports the change.
     *
     * @param context the screen showing the widget
     */
    fun toggle(context: UiContext) {
        checked = !checked
        markChanged(context, immediate = true)
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
     * Whether the widget can take the keyboard focus, which it can while enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * The value of the selected option, or an empty string if none is selected.
     */
    override val inputValue: String get() = selected ?: ""

    /**
     * Whether the selection satisfies the widget's constraints.
     */
    val isValid: Boolean get() = !required || selected != null

    /**
     * Whether the dropdown shows itself as invalid: only once it was touched.
     */
    override val showsInvalid: Boolean get() = touched && !isValid

    /**
     * Selects an option chosen by the player and reports the change.
     *
     * @param value the value of the chosen option
     * @param context the screen showing the widget
     */
    fun choose(value: String, context: UiContext) {
        selected = value
        markChanged(context, immediate = true)
    }

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
        val width = maxOf(UiMetrics.INPUT_WIDTH, widest + 2 * UiMetrics.WIDGET_PADDING + measurer.plainWidth(ARROW) + 4)
        return Size(width, UiMetrics.WIDGET_HEIGHT)
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
        val tokens = ui.tokens
        ui.fillRounded(bounds, if (hovered) ThemeColors.blend(inputFill(ui), tokens.accent, HOVER_ACCENT) else inputFill(ui))
        ui.borderRounded(bounds, if (showsInvalid) tokens.destructive else tokens.input)
        val textY = bounds.y + (bounds.height - ui.lineHeight + 1) / 2
        val color = if (enabled) tokens.foreground else ui.disabled(tokens.foreground)
        ui.clipped(bounds) {
            val label = selectedLabel
            if (label != null) {
                ui.text(label, bounds.x + UiMetrics.WIDGET_PADDING, textY, color)
            } else {
                ui.plainText("-", bounds.x + UiMetrics.WIDGET_PADDING, textY, tokens.mutedForeground)
            }
            ui.plainText(ARROW, bounds.right - UiMetrics.WIDGET_PADDING - ui.plainWidth(ARROW), textY, color)
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
        if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            context.focus(this)
            context.openDropdown(this)
        }
        return true
    }

    /**
     * Opens the option list on Enter, Space or Down.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled || !(isActivation(event) || event.key() == GLFW.GLFW_KEY_DOWN)) return false
        context.openDropdown(this)
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
    override fun contentSize(measurer: TextMeasurer): Size = Size(UiMetrics.IMAGE_SIZE, UiMetrics.IMAGE_SIZE)

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
        val labelWidth = label?.let { measurer.width(it) + 2 * UiMetrics.WIDGET_PADDING } ?: 0
        return Size(maxOf(UiMetrics.PROGRESS_WIDTH, labelWidth), maxOf(UiMetrics.PROGRESS_HEIGHT, measurer.lineHeight + 2))
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
        val tokens = ui.tokens
        val radius = bounds.height / 2
        ui.fillRounded(bounds, ThemeColors.withAlpha(tokens.primary, TRACK_ALPHA), radius)
        val filled = (bounds.width * progress.coerceIn(0f, 1f)).toInt()
        if (filled > 0) ui.fillRounded(Rect(bounds.x, bounds.y, filled, bounds.height), tokens.primary, radius)
        label?.let { ui.centeredText(it, bounds, tokens.foreground) }
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

/**
 * A Lucide icon drawn square within the widget's area.
 *
 * @param id the id of the widget
 * @property icon the Lucide name of the icon
 * @property size the side length used when the widget fits its content
 * @property color the theme token the icon is tinted with
 */
class IconWidget(id: String, var icon: String, val size: Int, val color: IconColor) : Widget(id) {

    /**
     * Returns a square of the icon's size.
     *
     * @param measurer the text measurer
     * @return the icon size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(size, size)

    /**
     * Draws the icon, square and centered in the widget's area, tinted with its colour token.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val side = minOf(bounds.width, bounds.height)
        val tint = when (color) {
            IconColor.FOREGROUND -> ui.tokens.foreground
            IconColor.MUTED -> ui.tokens.mutedForeground
            IconColor.PRIMARY -> ui.tokens.primary
            IconColor.DESTRUCTIVE -> ui.tokens.destructive
        }
        ui.icon(icon, Rect(bounds.x + (bounds.width - side) / 2, bounds.y + (bounds.height - side) / 2, side, side), tint)
    }
}

/**
 * The opacity of the translucent fill behind inputs, relative to the input token.
 */
private const val INPUT_FILL_ALPHA: Float = 0.3f

/**
 * How far a hovered dropdown is blended towards the accent colour.
 */
private const val HOVER_ACCENT: Float = 0.6f

/**
 * The opacity of a progress bar's track, relative to the primary colour.
 */
private const val TRACK_ALPHA: Float = 0.2f

/**
 * The corner radius of checkbox boxes.
 */
private const val CHECKBOX_RADIUS: Int = 2

/**
 * Returns the translucent fill drawn behind text inputs, checkboxes and dropdowns.
 *
 * @param ui the graphics, whose tokens define the colour
 * @return the ARGB fill colour
 */
internal fun inputFill(ui: UiGraphics): Int =
    ThemeColors.withAlpha(ui.tokens.input, ((ui.tokens.input ushr 24) / 255f) * INPUT_FILL_ALPHA)

/**
 * Draws a check mark inside a checkbox's box.
 *
 * @param ui the graphics to draw with
 * @param box the box
 * @param color the ARGB colour of the mark
 */
internal fun drawCheck(ui: UiGraphics, box: Rect, color: Int) {
    val x = box.x + box.width / 4
    val y = box.y + box.height / 2
    val short = box.width / 4
    val long = box.width / 2
    for (step in 0 until short) ui.fill(Rect(x + step, y + step - 1, 1, 2), color)
    for (step in 0 until long) ui.fill(Rect(x + short + step, y + short - step - 2, 1, 2), color)
}

/**
 * Checks whether a key activates a focused widget: Enter, keypad Enter or Space.
 *
 * @param event the key event
 * @return whether the key activates
 */
internal fun isActivation(event: KeyEvent): Boolean =
    event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER || event.key() == GLFW.GLFW_KEY_SPACE

/**
 * Returns the horizontal space a leading icon takes before a text.
 *
 * @param icon the icon name, or `null` for none
 * @param textWidth the width of the text after it
 * @return the icon width plus the gap to the text, the icon width alone without text, or zero
 */
internal fun iconSpace(icon: String?, textWidth: Int): Int = when {
    icon == null -> 0
    textWidth == 0 -> UiMetrics.INLINE_ICON
    else -> UiMetrics.INLINE_ICON + UiMetrics.ICON_GAP
}

/**
 * Draws a leading icon vertically centered in an area and returns where the text after it starts.
 *
 * @param ui the graphics to draw with
 * @param icon the icon name, or `null` for none
 * @param x the left edge of the icon
 * @param area the area to center the icon in vertically
 * @param color the ARGB tint
 * @param textWidth the width of the text after the icon
 * @return the left edge of the text
 */
internal fun drawLeadingIcon(ui: UiGraphics, icon: String?, x: Int, area: Rect, color: Int, textWidth: Int): Int {
    if (icon == null) return x
    val size = UiMetrics.INLINE_ICON
    ui.icon(icon, Rect(x, area.y + (area.height - size) / 2, size, size), color)
    return x + iconSpace(icon, textWidth)
}
