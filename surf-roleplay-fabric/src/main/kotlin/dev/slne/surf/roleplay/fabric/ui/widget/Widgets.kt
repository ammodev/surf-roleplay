package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.FlexLayout
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.text.TextBlock
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.protocol.screen.ButtonSize
import dev.slne.surf.roleplay.protocol.screen.ButtonVariant
import dev.slne.surf.roleplay.protocol.screen.IconColor
import dev.slne.surf.roleplay.protocol.screen.TextInputType
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

/**
 * A piece of styled text, optionally led by an icon. A label with a target focuses the target
 * when clicked.
 *
 * @param id the id of the widget
 * @property text the text as component JSON
 * @property icon the Lucide name of the leading icon, or `null` for none
 * @property forId the id of the widget a click focuses, or `null` for none
 */
class LabelWidget(id: String, var text: String = "", var icon: String? = null, val forId: String? = null) : Widget(id) {

    /**
     * Passes a left click on a label with a target to the target.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on a label with a target
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        val target = forId ?: return false
        if (!isOver(x, y)) return false
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) context.widget(target)?.labelClicked(context)
        return true
    }

    /**
     * Returns the size of the icon and the text on one line.
     *
     * @param measurer the text measurer
     * @return the text size
     */
    override fun contentSize(measurer: TextMeasurer): Size = wrappedSize(measurer, FlexLayout.UNBOUNDED)

    /**
     * Creates a layout box whose text wraps to the width the label gets.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox =
        wrappingLayout(measurer, iconSpace(icon, measurer.width(text)) + measurer.longestWordWidth(text)) { wrappedSize(measurer, it) }

    /**
     * Computes the size of the icon and the text wrapped to a width.
     *
     * @param measurer the text measurer
     * @param maxWidth the largest width of the label
     * @return the size
     */
    private fun wrappedSize(measurer: TextMeasurer, maxWidth: Int): Size {
        val space = iconSpace(icon, measurer.width(text))
        val block = TextBlock.size(measurer, text, (maxWidth - space).coerceAtLeast(1))
        return Size(space + block.width, maxOf(block.height, if (icon != null) UiMetrics.INLINE_ICON else 0))
    }

    /**
     * Draws the text wrapped to the widget's width, vertically centered in the widget, with the
     * icon beside its first line.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val color = ui.tokens.foreground
        val space = iconSpace(icon, ui.width(text))
        val textWidth = (bounds.width - space).coerceAtLeast(1)
        val blockHeight = TextBlock.height(ui, ui.lineWidths(text, textWidth).size)
        val top = bounds.y + ((bounds.height - blockHeight + 1) / 2).coerceAtLeast(0)
        val textX = drawLeadingIcon(ui, icon, bounds.x, Rect(bounds.x, top, space, ui.lineHeight), color, ui.width(text))
        ui.wrappedText(text, textX, top, textWidth, color)
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
 * A single-line text field. Password fields draw a mask character for every character, and email
 * fields are invalid unless they hold an address shape.
 *
 * @param id the id of the widget
 * @property edit the text and cursor, edited under the field's filter
 * @property placeholder the hint shown while the field is empty, as component JSON
 * @property required whether an empty value is invalid
 * @property icon the Lucide name of an icon drawn at the start of the field, or `null` for none
 * @property type the kind of text the field holds
 */
open class TextInputWidget(
    id: String,
    val edit: TextEditState,
    var placeholder: String = "",
    val required: Boolean = false,
    var icon: String? = null,
    val type: TextInputType = TextInputType.TEXT,
) : Widget(id) {

    /**
     * The text as drawn: masked for password fields, otherwise the text itself. It always has the
     * text's length.
     */
    val shownText: String get() = if (type == TextInputType.PASSWORD) PASSWORD_MASK.toString().repeat(edit.text.length) else edit.text

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
    override val inputValue: String? get() = edit.text

    /**
     * Whether the current text satisfies the field's constraints.
     */
    open val isValid: Boolean
        get() = when {
            edit.text.isEmpty() -> !required
            type == TextInputType.EMAIL -> EMAIL.matches(edit.text)
            else -> true
        }

    /**
     * Whether the field shows itself as invalid: while the server marks it invalid, or only once it was touched.
     */
    override val showsInvalid: Boolean get() = serverInvalid || touched && !isValid

    /**
     * Returns the default input size.
     *
     * @param measurer the text measurer
     * @return the input size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(UiMetrics.INPUT_WIDTH, UiMetrics.WIDGET_HEIGHT)

    /**
     * Draws the field with its text or placeholder and, while focused, the highlighted selection
     * and a blinking cursor. The border shows focus and invalid values.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val focused = context.focusedWidget === this
        val tokens = ui.tokens
        if (!embedded) {
            ui.fillRounded(bounds, inputFill(ui))
            ui.borderRounded(
                bounds,
                when {
                    showsInvalid -> tokens.destructive
                    focused -> tokens.ring
                    else -> tokens.input
                },
            )
        }
        val shown = shownText

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
                val visible = ui.font.plainSubstrByWidth(shown.substring(scrollStart), innerWidth)
                ui.plainText(visible, textX, textY, if (enabled) tokens.foreground else ui.disabled(tokens.foreground))
                if (focused) ui.textSelection(shown, scrollStart, scrollStart + visible.length, edit, textX, textY)
            }
            if (focused && System.currentTimeMillis() / CURSOR_BLINK_MILLIS % 2 == 0L) {
                keepCursorVisible(ui, innerWidth)
                val cursorX = textX + ui.plainWidth(shown.substring(scrollStart, edit.cursor))
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
        while (scrollStart < edit.cursor && ui.plainWidth(shownText.substring(scrollStart, edit.cursor)) > innerWidth) {
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
     * Handles pasting and the editing keys of [TextEditKeys]: cursor and word movement,
     * selection and deletion.
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
        return when (TextEditKeys.handle(edit, event)) {
            TextEditKeys.Result.IGNORED -> false
            TextEditKeys.Result.MOVED -> true
            TextEditKeys.Result.CHANGED -> true.also { markChanged(context, immediate = false) }
        }
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
     * Holds the cursor timing, the password mask and the email shape.
     */
    private companion object {
        /**
         * How long the cursor stays visible or hidden while blinking.
         */
        const val CURSOR_BLINK_MILLIS: Long = 500

        /**
         * The character drawn for every character of a password.
         */
        const val PASSWORD_MASK: Char = '\u2022'

        /**
         * The shape of an email address: a local part, an at sign, and a domain with a dot,
         * without whitespace.
         */
        val EMAIL = Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")
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
open class CheckboxWidget(id: String, var label: String = "", var checked: Boolean = false) : Widget(id) {

    /**
     * Whether the widget can take the keyboard focus, which it can while enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * `true` if the box is checked, `false` otherwise.
     */
    override val inputValue: String? get() = checked.toString()

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
        val tokens = ui.tokens
        val radius = CHECKBOX_RADIUS
        if (checked) {
            ui.fillRounded(box, if (enabled) tokens.primary else ui.disabled(tokens.primary), radius)
            val mark = size - 2
            ui.icon("check", Rect(box.x + 1, box.y + 1, mark, mark), if (enabled) tokens.primaryForeground else ui.disabled(tokens.primaryForeground))
        } else {
            ui.fillRounded(box, inputFill(ui), radius)
            ui.borderRounded(box, if (showsInvalid) tokens.destructive else if (enabled) tokens.input else ui.disabled(tokens.input), radius)
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
    open fun toggle(context: UiContext) {
        checked = !checked
        markChanged(context, immediate = true)
    }

    /**
     * Focuses and toggles an enabled box when a label that targets it is clicked.
     *
     * @param context the screen showing the widget
     */
    override fun labelClicked(context: UiContext) {
        if (!enabled) return
        context.focus(this)
        toggle(context)
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
     * Returns the default bar size: a thin bar, or one tall enough for the label and widened to
     * fit it.
     *
     * @param measurer the text measurer
     * @return the bar size
     */
    override fun contentSize(measurer: TextMeasurer): Size {
        val text = label ?: return Size(UiMetrics.PROGRESS_WIDTH, BAR_HEIGHT)
        val labelWidth = measurer.width(text) + 2 * UiMetrics.WIDGET_PADDING
        return Size(maxOf(UiMetrics.PROGRESS_WIDTH, labelWidth), maxOf(UiMetrics.PROGRESS_HEIGHT, measurer.lineHeight + 2))
    }

    /**
     * Holds the bar height.
     */
    companion object {
        /**
         * The height of a bar without a label.
         */
        const val BAR_HEIGHT: Int = 4
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
 * The opacity of a progress bar's track, relative to the primary colour.
 */
private const val TRACK_ALPHA: Float = 0.2f

/**
 * The corner radius of checkbox boxes.
 */
private const val CHECKBOX_RADIUS: Int = 2

/**
 * Returns the translucent fill drawn behind text inputs, checkboxes and selects.
 *
 * @param ui the graphics, whose tokens define the colour
 * @return the ARGB fill colour
 */
internal fun inputFill(ui: UiGraphics): Int =
    ThemeColors.withAlpha(ui.tokens.input, ((ui.tokens.input ushr 24) / 255f) * INPUT_FILL_ALPHA)


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
