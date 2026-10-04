package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.Corners
import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.fabric.ui.layout.LayoutBox
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.InputGroupAlign
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.OtpPattern
import dev.slne.surf.roleplay.protocol.screen.Sizing
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.math.abs
import kotlin.math.floor

/**
 * Breaks text into the lines it is drawn in at a width.
 */
object TextLines {

    /**
     * One drawn line: the characters from [start] up to, but not including, [end]. A line break
     * or the space a line was wrapped at belongs to no line.
     *
     * @property start the index of the first character
     * @property end the index after the last character
     */
    data class Line(val start: Int, val end: Int)

    /**
     * Breaks text at its line breaks, and wraps every paragraph at the last space that fits, or
     * inside a word that does not fit on a line by itself.
     *
     * @param text the text; line breaks are `\n`
     * @param width the width available for a line
     * @param measure returns the width of a string
     * @return the lines, at least one
     */
    fun wrap(text: String, width: Int, measure: (String) -> Int): List<Line> {
        val lines = mutableListOf<Line>()
        var paragraphStart = 0
        while (true) {
            val breakAt = text.indexOf('\n', paragraphStart)
            val paragraphEnd = if (breakAt < 0) text.length else breakAt
            wrapParagraph(text, paragraphStart, paragraphEnd, width, measure, lines)
            if (breakAt < 0) break
            paragraphStart = breakAt + 1
        }
        return lines
    }

    /**
     * Wraps one paragraph and adds its lines.
     *
     * @param text the whole text
     * @param start the index of the paragraph's first character
     * @param end the index after the paragraph's last character
     * @param width the width available for a line
     * @param measure returns the width of a string
     * @param lines the list the lines are added to
     */
    private fun wrapParagraph(text: String, start: Int, end: Int, width: Int, measure: (String) -> Int, lines: MutableList<Line>) {
        if (start == end) {
            lines += Line(start, start)
            return
        }
        var position = start
        while (position < end) {
            var fits = position + 1
            while (fits < end && measure(text.substring(position, fits + 1)) <= width) fits++
            when {
                fits == end -> {
                    lines += Line(position, end)
                    position = end
                }

                text[fits] == ' ' -> {
                    lines += Line(position, fits)
                    position = fits + 1
                }

                else -> {
                    val space = text.lastIndexOf(' ', fits - 1).takeIf { it > position }
                    if (space != null) {
                        lines += Line(position, space)
                        position = space + 1
                    } else {
                        lines += Line(position, fits)
                        position = fits
                    }
                }
            }
        }
    }

    /**
     * Finds the line a cursor position is drawn on.
     *
     * @param lines the lines
     * @param cursor the cursor position
     * @return the index of the last line that starts at or before the cursor
     */
    fun lineOf(lines: List<Line>, cursor: Int): Int = lines.indexOfLast { it.start <= cursor }.coerceAtLeast(0)

    /**
     * Finds the cursor position on a line that is closest to a horizontal offset.
     *
     * @param text the whole text
     * @param line the line
     * @param x the offset from the start of the line
     * @param measure returns the width of a string
     * @return the cursor position
     */
    fun positionAt(text: String, line: Line, x: Int, measure: (String) -> Int): Int =
        (line.start..line.end).minBy { abs(measure(text.substring(line.start, it)) - x) }

    /**
     * Finds the character of a line that is drawn under a horizontal offset. Offsets before the
     * line give its first character, offsets after it its last.
     *
     * @param text the whole text
     * @param line the line
     * @param x the offset from the start of the line
     * @param measure returns the width of a string
     * @return the index of the character, or the line's start for an empty line
     */
    fun charIndexAt(text: String, line: Line, x: Int, measure: (String) -> Int): Int {
        if (line.start == line.end) return line.start
        return (line.start until line.end).firstOrNull { measure(text.substring(line.start, it + 1)) > x } ?: (line.end - 1)
    }
}

/**
 * Applies a left click to the text and selection of a field: a single click places the cursor,
 * a double click selects the word under the mouse, and a triple click selects a range chosen by
 * the field. With Shift, the click extends the selection to the clicked position instead.
 *
 * @param edit the state of the field
 * @param shown the text as drawn, such as a password mask, in which a double click finds the word
 * @param clicks the click counter of the field
 * @param context the screen showing the field
 * @param x the mouse x position
 * @param y the mouse y position
 * @param timeMillis the time of the click in milliseconds
 * @param position the cursor position closest to the click
 * @param charIndex the index of the character under the click
 * @param tripleRange the range a triple click selects
 */
internal fun applyTextClick(
    edit: TextEditState,
    shown: String,
    clicks: ClickCounter,
    context: UiContext,
    x: Double,
    y: Double,
    timeMillis: Long,
    position: Int,
    charIndex: Int,
    tripleRange: () -> TextRange,
) {
    if (context.shiftClick) {
        clicks.reset()
        edit.moveCursorTo(position, extend = true)
        return
    }
    when (clicks.click(x, y, timeMillis)) {
        1 -> edit.cursor = position
        2 -> TextBoundaries.wordAt(shown, charIndex).let { edit.select(it.start, it.end) }
        else -> tripleRange().let { edit.select(it.start, it.end) }
    }
}

/**
 * A multi-line text field. Its text wraps at its width, Enter inserts a line break, and it
 * scrolls vertically to keep the cursor visible.
 *
 * @param id the id of the widget
 * @property edit the text and cursor, edited under the field's filter
 * @property placeholder the hint shown while the field is empty, as component JSON
 * @property rows the number of visible lines when the field fits its content
 * @property required whether an empty value is invalid
 */
class TextareaWidget(
    id: String,
    val edit: TextEditState,
    var placeholder: String = "",
    val rows: Int = 3,
    val required: Boolean = false,
) : Widget(id) {

    /**
     * The lines of the text, computed by the last [layoutLines].
     */
    private var lines: List<TextLines.Line> = listOf(TextLines.Line(0, 0))

    /**
     * The measurer of the last [layoutLines], used to move the cursor between lines.
     */
    private var measurer: TextMeasurer? = null

    /**
     * The index of the first visible line.
     */
    private var firstLine: Int = 0

    /**
     * The cursor position the scrolling last followed.
     */
    private var followedCursor: Int = -1

    /**
     * Counts consecutive clicks to tell single, double and triple clicks apart.
     */
    private val clicks: ClickCounter = ClickCounter()

    /**
     * Whether the field can take the keyboard focus, which it can while enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * The field's text.
     */
    override val inputValue: String get() = edit.text

    /**
     * Whether the field shows itself as invalid: while the server marks it invalid, or once touched while required and empty.
     */
    override val showsInvalid: Boolean get() = serverInvalid || touched && required && edit.text.isEmpty()

    /**
     * Returns the default input width and the height of the visible rows.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(UiMetrics.INPUT_WIDTH, rows * measurer.lineHeight + 2 * PADDING_Y)

    /**
     * The width available for text.
     */
    private val innerWidth: Int get() = (bounds.width - 2 * UiMetrics.WIDGET_PADDING).coerceAtLeast(1)

    /**
     * The number of lines that fit into the field.
     *
     * @param lineHeight the height of one line
     * @return at least one
     */
    private fun visibleLines(lineHeight: Int): Int = ((bounds.height - 2 * PADDING_Y) / lineHeight).coerceAtLeast(1)

    /**
     * Breaks the text into lines at the field's current width.
     *
     * @param measurer the text measurer
     */
    fun layoutLines(measurer: TextMeasurer) {
        this.measurer = measurer
        lines = TextLines.wrap(edit.text, innerWidth, measurer::plainWidth)
    }

    /**
     * Scrolls so that the cursor's line is visible, if the cursor moved since the last call.
     *
     * @param lineHeight the height of one line
     */
    private fun followCursor(lineHeight: Int) {
        val visible = visibleLines(lineHeight)
        if (edit.cursor != followedCursor) {
            followedCursor = edit.cursor
            val line = TextLines.lineOf(lines, edit.cursor)
            if (line < firstLine) firstLine = line
            if (line >= firstLine + visible) firstLine = line - visible + 1
        }
        firstLine = firstLine.coerceIn(0, (lines.size - visible).coerceAtLeast(0))
    }

    /**
     * Draws the field with its visible lines or placeholder and, while focused, the highlighted
     * selection and a blinking cursor.
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
        layoutLines(ui)
        followCursor(ui.lineHeight)
        val textX = bounds.x + UiMetrics.WIDGET_PADDING
        val top = bounds.y + PADDING_Y
        val visible = visibleLines(ui.lineHeight)
        val color = if (enabled) tokens.foreground else ui.disabled(tokens.foreground)
        ui.clipped(Rect(textX, top, innerWidth, visible * ui.lineHeight)) {
            if (edit.text.isEmpty() && !focused) ui.text(placeholder, textX, top + 1, tokens.mutedForeground)
            for (index in firstLine until minOf(lines.size, firstLine + visible)) {
                val line = lines[index]
                val lineY = top + (index - firstLine) * ui.lineHeight + 1
                ui.plainText(edit.text.substring(line.start, line.end), textX, lineY, color)
                if (focused) ui.textSelection(edit.text, line.start, line.end, edit, textX, lineY)
            }
            if (focused && System.currentTimeMillis() / CURSOR_BLINK_MILLIS % 2 == 0L) {
                val lineIndex = TextLines.lineOf(lines, edit.cursor)
                val line = lines[lineIndex]
                val cursorX = textX + ui.plainWidth(edit.text.substring(line.start, edit.cursor.coerceIn(line.start, line.end)))
                ui.fill(Rect(cursorX, top + (lineIndex - firstLine) * ui.lineHeight, 1, ui.lineHeight), tokens.foreground)
            }
        }
    }

    /**
     * Focuses an enabled field when it is clicked. A left click places the cursor at the clicked
     * position, a double click selects the word under the mouse and a triple click the line
     * between line breaks; with Shift, the click extends the selection.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on this field
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (!enabled) return true
        context.focus(this)
        val measurer = measurer
        if (measurer == null) {
            edit.cursor = edit.text.length
            return true
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true
        val line = lines[(firstLine + ((y - bounds.y - PADDING_Y) / measurer.lineHeight).toInt()).coerceIn(0, lines.lastIndex)]
        val offset = (x - bounds.x - UiMetrics.WIDGET_PADDING).toInt()
        val position = TextLines.positionAt(edit.text, line, offset, measurer::plainWidth)
        val charIndex = TextLines.charIndexAt(edit.text, line, offset, measurer::plainWidth)
        applyTextClick(edit, edit.text, clicks, context, x, y, context.timeMillis, position, charIndex) { TextBoundaries.lineAt(edit.text, position) }
        return true
    }

    /**
     * Whether a press in the field starts a drag that selects text, which it does while enabled.
     */
    override val draggable: Boolean get() = enabled

    /**
     * Extends the selection to the mouse while the button stays held after a press in the field.
     * Above or below the visible lines, the selection reaches into the line before or after
     * them, which scrolls the field.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     */
    override fun mouseDragged(context: UiContext, x: Double, y: Double) {
        val measurer = measurer ?: return
        val row = floor((y - bounds.y - PADDING_Y) / measurer.lineHeight).toInt()
        val line = lines[(firstLine + row).coerceIn(0, lines.lastIndex)]
        val offset = (x - bounds.x - UiMetrics.WIDGET_PADDING).toInt()
        edit.moveCursorTo(TextLines.positionAt(edit.text, line, offset, measurer::plainWidth), extend = true)
    }

    /**
     * Scrolls the lines when the text is longer than the field.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param amount the scroll amount; positive scrolls up
     * @return whether the field scrolled
     */
    override fun mouseScrolled(context: UiContext, x: Double, y: Double, amount: Double): Boolean {
        val lineHeight = measurer?.lineHeight ?: return false
        if (!isOver(x, y)) return false
        val maxFirst = (lines.size - visibleLines(lineHeight)).coerceAtLeast(0)
        val next = (firstLine - amount.toInt().coerceIn(-1, 1)).coerceIn(0, maxFirst)
        if (next == firstLine) return false
        firstLine = next
        return true
    }

    /**
     * Inserts pasted text at the cursor in place of the selection, keeping its line breaks and
     * removing characters that cannot be typed.
     *
     * @param context the screen showing the widget
     * @param text the pasted text
     */
    fun paste(context: UiContext, text: String) {
        if (edit.insert(TextEditKeys.cleanPaste(text, lineBreak = null))) markChanged(context, immediate = false)
    }

    /**
     * Moves the cursor to the closest position on the line above or below.
     *
     * @param delta `-1` for the line above, `1` for the line below
     * @param extend whether to extend the selection; otherwise the selection is cleared
     */
    private fun moveVertically(delta: Int, extend: Boolean) {
        val measurer = measurer ?: return
        layoutLines(measurer)
        val current = TextLines.lineOf(lines, edit.cursor)
        val target = current + delta
        if (target !in lines.indices) {
            edit.moveCursorTo(if (delta < 0) 0 else edit.text.length, extend)
            return
        }
        val line = lines[current]
        val x = measurer.plainWidth(edit.text.substring(line.start, edit.cursor.coerceIn(line.start, line.end)))
        edit.moveCursorTo(TextLines.positionAt(edit.text, lines[target], x, measurer::plainWidth), extend)
    }

    /**
     * Handles line breaks, the clipboard, moves between and within drawn lines, and the editing keys of
     * [TextEditKeys]. Home and End move to the start and end of the drawn line, or of the whole
     * text with Control; Shift extends the selection with every move.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled) return false
        clicks.reset()
        when (TextEditKeys.handleClipboard(edit, event, context, lineBreak = null)) {
            TextEditKeys.Result.IGNORED -> Unit
            TextEditKeys.Result.MOVED -> return true
            TextEditKeys.Result.CHANGED -> return true.also { markChanged(context, immediate = false) }
        }
        val extend = event.hasShiftDown()
        val whole = event.hasControlDownWithQuirk()
        val line = lines.getOrNull(TextLines.lineOf(lines, edit.cursor))
        when (event.key()) {
            GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> if (edit.insert("\n")) markChanged(context, immediate = false)
            GLFW.GLFW_KEY_UP -> moveVertically(-1, extend)
            GLFW.GLFW_KEY_DOWN -> moveVertically(1, extend)
            GLFW.GLFW_KEY_HOME -> edit.moveCursorTo(if (whole) 0 else line?.start ?: 0, extend)
            GLFW.GLFW_KEY_END -> edit.moveCursorTo(if (whole) edit.text.length else line?.end ?: edit.text.length, extend)
            else -> return when (TextEditKeys.handle(edit, event)) {
                TextEditKeys.Result.IGNORED -> false
                TextEditKeys.Result.MOVED -> true
                TextEditKeys.Result.CHANGED -> true.also { markChanged(context, immediate = false) }
            }
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
     * Holds the spacing and cursor timing.
     */
    private companion object {
        /**
         * The space above and below the lines.
         */
        const val PADDING_Y: Int = 4

        /**
         * How long the cursor stays visible or hidden while blinking.
         */
        const val CURSOR_BLINK_MILLIS: Long = 500
    }
}

/**
 * An input for a one-time code, entered one character per slot. Typing fills the next free slot,
 * Backspace clears the last filled one, and pasting replaces the code with the pasted characters
 * the pattern accepts.
 *
 * @param id the id of the widget
 * @property length the number of slots
 * @property groups the number of slots in each group; empty for a single group
 * @property pattern the characters the slots accept
 * @param initial the initially entered characters
 * @property required whether an empty value is invalid
 */
class InputOtpWidget(
    id: String,
    val length: Int,
    val groups: List<Int>,
    val pattern: OtpPattern,
    initial: String,
    val required: Boolean,
) : Widget(id) {

    /**
     * The characters entered so far.
     */
    private var value: String = initial.filter(::accepts).take(length)

    /**
     * The sizes of the drawn groups.
     */
    private val groupSizes: List<Int> get() = groups.takeIf { it.isNotEmpty() && it.sum() == length } ?: listOf(length)

    /**
     * The slot the next character goes into, or the last slot once the code is complete.
     */
    val activeSlot: Int get() = value.length.coerceAtMost(length - 1)

    /**
     * Whether the input can take the keyboard focus, which it can while enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * The input highlights its active slot instead of a focus ring.
     */
    override val drawsOwnFocus: Boolean get() = true

    /**
     * The characters entered so far.
     */
    override val inputValue: String get() = value

    /**
     * Whether the input shows itself as invalid: while the server marks it invalid, or once
     * touched while incomplete, or empty and required.
     */
    override val showsInvalid: Boolean get() = serverInvalid || touched && (if (value.isEmpty()) required else value.length != length)

    /**
     * Checks whether a slot accepts a character.
     *
     * @param char the character
     * @return whether the character matches the pattern
     */
    private fun accepts(char: Char): Boolean = when (pattern) {
        OtpPattern.DIGITS -> char in '0'..'9'
        OtpPattern.ALPHANUMERIC -> char in '0'..'9' || char in 'a'..'z' || char in 'A'..'Z'
    }

    /**
     * Returns the width of all slots and group separators and the slot height.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size =
        Size(length * SLOT_WIDTH + (groupSizes.size - 1) * SEPARATOR_WIDTH, UiMetrics.WIDGET_HEIGHT)

    /**
     * Draws the slots, joined within their groups, with the separators between groups. The active
     * slot of a focused input shows the ring and a blinking cursor while empty. An invalid input is
     * outlined in the destructive colour while not focused.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        val focused = context.focusedWidget === this
        val invalid = showsInvalid && !focused
        var x = bounds.x
        var slot = 0
        groupSizes.forEachIndexed { groupIndex, size ->
            for (inGroup in 0 until size) {
                val area = Rect(x, bounds.y, SLOT_WIDTH, bounds.height)
                val corners = when {
                    size == 1 -> Corners.ALL
                    inGroup == 0 -> Corners.LEFT
                    inGroup == size - 1 -> Corners.RIGHT
                    else -> Corners.NONE
                }
                ui.fillRounded(area, inputFill(ui), corners = corners)
                ui.borderRounded(area, if (invalid) tokens.destructive else tokens.input, corners = corners)
                value.getOrNull(slot)?.let { char ->
                    val text = char.toString()
                    val color = if (enabled) tokens.foreground else ui.disabled(tokens.foreground)
                    ui.plainText(text, area.x + (area.width - ui.plainWidth(text) + 1) / 2, area.y + (area.height - ui.lineHeight + 1) / 2, color)
                }
                if (focused && slot == activeSlot) {
                    ui.borderRounded(area, tokens.ring, corners = corners)
                    if (slot >= value.length && System.currentTimeMillis() / CURSOR_BLINK_MILLIS % 2 == 0L) {
                        ui.fill(Rect(area.x + area.width / 2, area.y + (area.height - ui.lineHeight) / 2, 1, ui.lineHeight), tokens.foreground)
                    }
                }
                x += SLOT_WIDTH
                slot++
            }
            if (groupIndex < groupSizes.lastIndex) {
                val side = UiMetrics.INLINE_ICON
                ui.icon("minus", Rect(x + (SEPARATOR_WIDTH - side) / 2, bounds.y + (bounds.height - side) / 2, side, side), tokens.mutedForeground)
                x += SEPARATOR_WIDTH
            }
        }
    }

    /**
     * Replaces the code with the characters of a pasted text that the pattern accepts, up to the
     * number of slots.
     *
     * @param context the screen showing the widget
     * @param text the pasted text
     */
    fun paste(context: UiContext, text: String) {
        val next = text.filter(::accepts).take(length)
        if (next == value) return
        value = next
        markChanged(context, immediate = value.length == length)
    }

    /**
     * Focuses an enabled input when it is clicked.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on this input
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (enabled) context.focus(this)
        return true
    }

    /**
     * Clears the last filled slot on Backspace and pastes the clipboard.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled) return false
        if (event.isPaste) {
            paste(context, context.clipboard)
            return true
        }
        if (event.key() != GLFW.GLFW_KEY_BACKSPACE) return false
        if (value.isNotEmpty()) {
            value = value.dropLast(1)
            markChanged(context, immediate = false)
        }
        return true
    }

    /**
     * Fills the next free slot with a typed character the pattern accepts.
     *
     * @param context the screen showing the widget
     * @param event the character event
     * @return whether the character was handled
     */
    override fun charTyped(context: UiContext, event: CharacterEvent): Boolean {
        if (!enabled) return false
        val typed = event.codepointAsString().singleOrNull() ?: return true
        if (value.length < length && accepts(typed)) {
            value += typed
            markChanged(context, immediate = value.length == length)
        }
        return true
    }

    /**
     * Replaces the code with the accepted characters of a value.
     *
     * @param value the new code
     */
    override fun applyValue(value: String) {
        this.value = value.filter(::accepts).take(length)
    }

    /**
     * Holds the slot sizes and cursor timing.
     */
    private companion object {
        /**
         * The width of one slot.
         */
        const val SLOT_WIDTH: Int = 16

        /**
         * The width of the separator between two groups.
         */
        const val SEPARATOR_WIDTH: Int = 14

        /**
         * How long the cursor stays visible or hidden while blinking.
         */
        const val CURSOR_BLINK_MILLIS: Long = 500
    }
}

/**
 * A text field or textarea joined with addons into one bordered control. The group draws the
 * frame and the control is drawn embedded in it; block addons sit above and below the control,
 * inline addons beside it.
 *
 * @param id the id of the widget
 * @param parts the control and the addon widgets, in the order the server sent them
 */
class InputGroupWidget(id: String, parts: List<Widget>) : ContainerWidget(id, Axis.VERTICAL) {

    /**
     * The row of the inline addons and the control.
     */
    private val row = ContainerWidget("$id#row", Axis.HORIZONTAL).apply { crossAlign = Align.CENTER }

    init {
        crossAlign = Align.STRETCH
        val addons = parts.filterIsInstance<InputGroupAddonWidget>()
        childList += addons.filter { it.align == InputGroupAlign.BLOCK_START }
        row.childList += addons.filter { it.align == InputGroupAlign.INLINE_START }
        row.childList += parts.filter { it !is InputGroupAddonWidget }
        row.childList += addons.filter { it.align == InputGroupAlign.INLINE_END }
        childList += row
        childList += addons.filter { it.align == InputGroupAlign.BLOCK_END }
    }

    /**
     * The text field or textarea of the group, or `null` if it has none.
     */
    val control: Widget? get() = row.childList.firstOrNull { it is TextInputWidget || it is TextareaWidget }

    /**
     * Returns whether the focused widget is inside this group.
     *
     * @param context the screen showing the widget
     * @return whether the focus is within the group
     */
    private fun hasFocusWithin(context: UiContext): Boolean {
        val focused = context.focusedWidget ?: return false
        return WidgetTree.find(this, focused.id) === focused
    }

    /**
     * Embeds the control, lets it take the row's remaining width, and creates the layout boxes.
     *
     * @param measurer the text measurer
     * @return the layout box
     */
    override fun createLayout(measurer: TextMeasurer): LayoutBox {
        control?.let {
            it.embedded = true
            it.focusFrame = this
            it.width = Sizing.grow()
        }
        return super.createLayout(measurer)
    }

    /**
     * Draws the group's frame, showing focus within it and an invalid control, and its parts.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        ui.fillRounded(bounds, inputFill(ui))
        ui.borderRounded(
            bounds,
            when {
                control?.showsInvalid == true -> tokens.destructive
                hasFocusWithin(context) -> tokens.ring
                else -> tokens.input
            },
        )
        super.render(ui, context, mouseX, mouseY)
    }

    /**
     * Passes a click to the parts, and focuses the control when the click hit no part that
     * handles it.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on the group
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (super.mouseClicked(context, x, y, button)) return true
        if (!isOver(x, y)) return false
        control?.takeIf { it.enabled }?.let { context.focus(it) }
        return true
    }
}

/**
 * A row of texts, icons or buttons at one side of an input group's control.
 *
 * @param id the id of the widget
 * @property align where the addon sits
 */
class InputGroupAddonWidget(id: String, val align: InputGroupAlign) : ContainerWidget(id, Axis.HORIZONTAL) {
    init {
        gap = ADDON_GAP
        crossAlign = Align.CENTER
        padding = when (align) {
            InputGroupAlign.INLINE_START -> Insets(left = UiMetrics.WIDGET_PADDING)
            InputGroupAlign.INLINE_END -> Insets(right = ADDON_EDGE)
            InputGroupAlign.BLOCK_START, InputGroupAlign.BLOCK_END ->
                Insets(top = ADDON_EDGE, right = UiMetrics.WIDGET_PADDING, bottom = ADDON_EDGE, left = UiMetrics.WIDGET_PADDING)
        }
    }

    /**
     * Holds the addon spacing.
     */
    private companion object {
        /**
         * The space between the parts of an addon.
         */
        const val ADDON_GAP: Int = 4

        /**
         * The space at the outer edge of an inline end addon and above and below block addons.
         */
        const val ADDON_EDGE: Int = 3
    }
}

/**
 * A muted text in an input group addon, optionally led by an icon.
 *
 * @param id the id of the widget
 * @property text the text as component JSON
 * @property icon the Lucide name of the leading icon, or `null` for none
 */
class InputGroupTextWidget(id: String, var text: String, var icon: String?) : Widget(id) {

    /**
     * Returns the size of the icon and the text on one line.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size =
        Size(iconSpace(icon, measurer.width(text)) + measurer.width(text), maxOf(measurer.lineHeight, if (icon != null) UiMetrics.INLINE_ICON else 0))

    /**
     * Draws the icon and the text in the muted foreground colour.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val color = ui.tokens.mutedForeground
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
