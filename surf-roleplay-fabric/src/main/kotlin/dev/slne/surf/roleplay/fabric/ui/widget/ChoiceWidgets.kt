package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.TextMeasurer
import dev.slne.surf.roleplay.fabric.ui.UiGraphics
import dev.slne.surf.roleplay.fabric.ui.layout.Rect
import dev.slne.surf.roleplay.fabric.ui.layout.Size
import dev.slne.surf.roleplay.fabric.ui.theme.ThemeColors
import dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics
import dev.slne.surf.roleplay.protocol.screen.Orientation
import dev.slne.surf.roleplay.protocol.screen.RadioOption
import dev.slne.surf.roleplay.protocol.screen.SliderValues
import dev.slne.surf.roleplay.protocol.screen.SwitchSize
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW
import kotlin.math.abs

/**
 * A switch that is on or off, toggled by a click, Space or Enter.
 *
 * @param id the id of the widget
 * @property checked whether the switch is on
 * @property size the size of the switch
 */
class SwitchWidget(id: String, var checked: Boolean, val size: SwitchSize) : Widget(id) {

    /**
     * Whether the switch can take the keyboard focus, which it can while enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * `true` if the switch is on, `false` otherwise.
     */
    override val inputValue: String get() = checked.toString()

    /**
     * The width of the track.
     */
    private val trackWidth: Int get() = if (size == SwitchSize.SM) 16 else 22

    /**
     * The height of the track.
     */
    private val trackHeight: Int get() = if (size == SwitchSize.SM) 9 else 12

    /**
     * Returns the size of the track.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size = Size(trackWidth, trackHeight)

    /**
     * Draws the track, in the primary colour while on, and the round thumb at its side.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        val fade: (Int) -> Int = { if (enabled) it else ui.disabled(it) }
        val track = Rect(bounds.x, bounds.y + (bounds.height - trackHeight) / 2, trackWidth, trackHeight)
        ui.fillRounded(track, fade(if (checked) tokens.primary else tokens.input), trackHeight / 2)
        val thumb = trackHeight - 2
        val thumbX = if (checked) track.right - 1 - thumb else track.x + 1
        ui.fillRounded(Rect(thumbX, track.y + 1, thumb, thumb), fade(tokens.background), thumb / 2)
    }

    /**
     * Switches the switch and reports the change at once.
     *
     * @param context the screen showing the widget
     */
    fun toggle(context: UiContext) {
        checked = !checked
        markChanged(context, immediate = true)
    }

    /**
     * Toggles an enabled switch on a left click and focuses it.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on this switch
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
     * Toggles the switch on Enter or Space.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled || !isActivation(event)) return false
        toggle(context)
        return true
    }

    /**
     * Focuses and toggles an enabled switch when a label that targets it is clicked.
     *
     * @param context the screen showing the widget
     */
    override fun labelClicked(context: UiContext) {
        if (!enabled) return
        context.focus(this)
        toggle(context)
    }

    /**
     * Switches the switch on for `true` and off for any other value.
     *
     * @param value the new state
     */
    override fun applyValue(value: String) {
        checked = value == "true"
    }
}

/**
 * A choice of one option, each drawn as a radio button with its label. The arrow keys select the
 * next or previous enabled option, wrapping at the ends.
 *
 * @param id the id of the widget
 * @property options the options, in order
 * @property selected the value of the selected option, or `null` if none is selected
 * @property orientation whether the options run vertically or horizontally
 * @property required whether having no selection is invalid
 */
class RadioGroupWidget(
    id: String,
    val options: List<RadioOption>,
    var selected: String?,
    val orientation: Orientation,
    val required: Boolean,
) : Widget(id) {

    /**
     * The sizes of the options, measured by the last layout.
     */
    private var optionSizes: List<Size> = emptyList()

    /**
     * Whether the group can take the keyboard focus, which it can while enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * The value of the selected option, or empty.
     */
    override val inputValue: String get() = selected ?: ""

    /**
     * Whether the group shows itself as invalid: while the server marks it invalid, or once touched while required and empty.
     */
    override val showsInvalid: Boolean get() = serverInvalid || touched && required && selected == null

    /**
     * Measures the options and returns their total size along the group's orientation.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size {
        optionSizes = options.map { option ->
            val label = measurer.width(option.label)
            Size(CIRCLE + if (label > 0) LABEL_GAP + label else 0, maxOf(CIRCLE, measurer.lineHeight))
        }
        val gaps = (options.size - 1).coerceAtLeast(0) * gap
        return if (orientation == Orientation.VERTICAL) {
            Size(optionSizes.maxOfOrNull { it.width } ?: 0, optionSizes.sumOf { it.height } + gaps)
        } else {
            Size(optionSizes.sumOf { it.width } + gaps, optionSizes.maxOfOrNull { it.height } ?: 0)
        }
    }

    /**
     * The space between two options.
     */
    private val gap: Int get() = if (orientation == Orientation.VERTICAL) VERTICAL_GAP else HORIZONTAL_GAP

    /**
     * Returns the area of every option within the group's bounds.
     *
     * @return the option areas, in order
     */
    private fun optionAreas(): List<Rect> {
        var cursor = if (orientation == Orientation.VERTICAL) bounds.y else bounds.x
        return options.indices.map { index ->
            val size = optionSizes.getOrElse(index) { Size(CIRCLE, CIRCLE) }
            val area = if (orientation == Orientation.VERTICAL) Rect(bounds.x, cursor, bounds.width, size.height) else Rect(cursor, bounds.y, size.width, bounds.height)
            cursor += (if (orientation == Orientation.VERTICAL) size.height else size.width) + gap
            area
        }
    }

    /**
     * Draws every option: a round button with a dot while selected, and its label.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        optionAreas().forEachIndexed { index, area ->
            val option = options[index]
            val active = enabled && option.enabled
            val fade: (Int) -> Int = { if (active) it else ui.disabled(it) }
            val circle = Rect(area.x, area.y + (area.height - CIRCLE) / 2, CIRCLE, CIRCLE)
            ui.fillRounded(circle, inputFill(ui), CIRCLE / 2)
            ui.borderRounded(circle, fade(if (showsInvalid) tokens.destructive else tokens.input), CIRCLE / 2)
            if (option.value == selected) {
                ui.fillRounded(Rect(circle.x + (CIRCLE - DOT) / 2, circle.y + (CIRCLE - DOT) / 2, DOT, DOT), fade(tokens.primary), DOT / 2)
            }
            ui.text(option.label, circle.right + LABEL_GAP, area.y + (area.height - ui.lineHeight + 1) / 2, fade(tokens.foreground))
        }
    }

    /**
     * Selects an enabled option and reports the change at once. Disabled options are ignored.
     *
     * @param index the option index
     * @param context the screen showing the widget
     */
    fun select(index: Int, context: UiContext) {
        val option = options.getOrNull(index) ?: return
        if (!enabled || !option.enabled || option.value == selected) return
        selected = option.value
        markChanged(context, immediate = true)
    }

    /**
     * Selects the next or previous enabled option, wrapping at the ends. Without a selection, the
     * first enabled option in that direction is selected.
     *
     * @param delta `1` for the next option, `-1` for the previous one
     * @param context the screen showing the widget
     */
    private fun moveSelection(delta: Int, context: UiContext) {
        if (options.none { it.enabled }) return
        val current = options.indexOfFirst { it.value == selected }
        var index = if (current < 0) (if (delta > 0) -1 else 0) else current
        repeat(options.size) {
            index = (index + delta).mod(options.size)
            if (options[index].enabled) {
                select(index, context)
                return
            }
        }
    }

    /**
     * Selects the option under a left click and focuses the group.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on this group
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (!enabled || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true
        context.focus(this)
        val index = optionAreas().indexOfFirst { it.contains(x, y) }
        if (index >= 0) select(index, context)
        return true
    }

    /**
     * Moves the selection with the arrow keys.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled) return false
        when (event.key()) {
            GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_RIGHT -> moveSelection(1, context)
            GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_LEFT -> moveSelection(-1, context)
            GLFW.GLFW_KEY_SPACE -> if (selected == null) moveSelection(1, context)
            else -> return false
        }
        return true
    }

    /**
     * Selects the option with a value, or clears the selection for an unknown value.
     *
     * @param value the value of the option to select
     */
    override fun applyValue(value: String) {
        selected = value.takeIf { candidate -> options.any { it.value == candidate } }
    }

    /**
     * Holds the option sizes.
     */
    private companion object {
        /**
         * The diameter of a radio button.
         */
        const val CIRCLE: Int = 10

        /**
         * The diameter of the dot of a selected radio button.
         */
        const val DOT: Int = 4

        /**
         * The space between a radio button and its label.
         */
        const val LABEL_GAP: Int = 5

        /**
         * The space between two options stacked vertically.
         */
        const val VERTICAL_GAP: Int = 6

        /**
         * The space between two options side by side.
         */
        const val HORIZONTAL_GAP: Int = 12
    }
}

/**
 * A slider with one thumb, or two for a range. Pressing the track moves the closest thumb there,
 * dragging moves it further, and the arrow keys move it by one step, Page Up and Page Down by ten
 * steps, and Home and End to the ends. In a range slider, Enter or Space switches the thumb the
 * keyboard moves. Thumbs never pass each other.
 *
 * @param id the id of the widget
 * @param initial the initial thumb values, ascending
 * @property min the smallest value
 * @property max the largest value
 * @property step the distance between two selectable values, starting at [min]
 * @property orientation whether the slider runs horizontally or vertically
 */
class SliderWidget(
    id: String,
    initial: List<Double>,
    val min: Double,
    val max: Double,
    val step: Double,
    val orientation: Orientation,
) : Widget(id) {

    /**
     * The thumb values, ascending.
     */
    private val values: MutableList<Double> = initial.map { SliderValues.snap(it, min, max, step) }.sorted().toMutableList()

    /**
     * The index of the thumb that the keyboard and dragging move.
     */
    var activeThumb: Int = 0
        private set

    /**
     * Whether the slider can take the keyboard focus, which it can while enabled.
     */
    override val focusable: Boolean get() = enabled

    /**
     * The slider draws a ring around its active thumb instead of a focus ring.
     */
    override val drawsOwnFocus: Boolean get() = true

    /**
     * A press on the slider starts a drag of its active thumb.
     */
    override val draggable: Boolean get() = true

    /**
     * The thumb values joined by commas, each in its shortest decimal form.
     */
    override val inputValue: String get() = values.joinToString(",") { SliderValues.format(it) }

    /**
     * Returns the default slider size along its orientation.
     *
     * @param measurer the text measurer
     * @return the size
     */
    override fun contentSize(measurer: TextMeasurer): Size =
        if (orientation == Orientation.HORIZONTAL) Size(UiMetrics.INPUT_WIDTH, THUMB + 2) else Size(THUMB + 2, VERTICAL_LENGTH)

    /**
     * The first position of the track that a thumb centre can reach.
     */
    private val trackStart: Int get() = (if (orientation == Orientation.HORIZONTAL) bounds.x else bounds.y) + THUMB / 2

    /**
     * The length of the track that thumb centres move along.
     */
    private val trackLength: Int get() = ((if (orientation == Orientation.HORIZONTAL) bounds.width else bounds.height) - THUMB).coerceAtLeast(1)

    /**
     * Returns how far along the track a value lies.
     *
     * @param value the value
     * @return the fraction from `0` at the minimum to `1` at the maximum
     */
    private fun fraction(value: Double): Double = ((value - min) / (max - min)).coerceIn(0.0, 1.0)

    /**
     * Returns the value at a mouse position.
     *
     * @param x the mouse x position
     * @param y the mouse y position
     * @return the snapped value; vertical sliders have their minimum at the bottom
     */
    private fun valueAt(x: Double, y: Double): Double {
        val along = if (orientation == Orientation.HORIZONTAL) (x - trackStart) / trackLength else 1.0 - (y - trackStart) / trackLength
        return SliderValues.snap(min + along.coerceIn(0.0, 1.0) * (max - min), min, max, step)
    }

    /**
     * Returns the centre of a thumb on screen.
     *
     * @param index the thumb index
     * @return the centre's x and y position
     */
    private fun thumbCentre(index: Int): Pair<Int, Int> {
        val offset = (fraction(values[index]) * trackLength).toInt()
        return if (orientation == Orientation.HORIZONTAL) {
            trackStart + offset to bounds.y + bounds.height / 2
        } else {
            bounds.x + bounds.width / 2 to trackStart + trackLength - offset
        }
    }

    /**
     * Moves a thumb to a value, snapped and kept between its neighbours, and reports a change.
     *
     * @param index the thumb index
     * @param value the target value
     * @param context the screen showing the widget
     */
    private fun moveThumb(index: Int, value: Double, context: UiContext) {
        val lower = values.getOrNull(index - 1) ?: min
        val upper = values.getOrNull(index + 1) ?: max
        val next = SliderValues.snap(value, min, max, step).coerceIn(lower, upper)
        if (next == values[index]) return
        values[index] = next
        markChanged(context, immediate = false)
    }

    /**
     * Moves the thumb closest to a mouse position there and makes it the active thumb.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     */
    fun press(context: UiContext, x: Double, y: Double) {
        val target = valueAt(x, y)
        val closest = values.indices.minBy { abs(values[it] - target) + if (target > values[it]) -EPSILON * it else EPSILON * it }
        activeThumb = closest
        moveThumb(closest, target, context)
    }

    /**
     * Moves the active thumb to the value under the mouse.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     */
    override fun mouseDragged(context: UiContext, x: Double, y: Double) {
        if (enabled) moveThumb(activeThumb, valueAt(x, y), context)
    }

    /**
     * Draws the track, the filled range and the thumbs, with a ring around the active thumb while
     * focused.
     *
     * @param ui the graphics to draw with
     * @param context the screen showing the widget
     * @param mouseX the mouse x position
     * @param mouseY the mouse y position
     */
    override fun render(ui: UiGraphics, context: UiContext, mouseX: Int, mouseY: Int) {
        val tokens = ui.tokens
        val fade: (Int) -> Int = { if (enabled) it else ui.disabled(it) }
        val horizontal = orientation == Orientation.HORIZONTAL
        val track = if (horizontal) {
            Rect(bounds.x, bounds.y + (bounds.height - TRACK) / 2, bounds.width, TRACK)
        } else {
            Rect(bounds.x + (bounds.width - TRACK) / 2, bounds.y, TRACK, bounds.height)
        }
        ui.fillRounded(track, fade(tokens.muted), TRACK / 2)
        val from = if (values.size == 1) null else thumbCentre(0)
        val to = thumbCentre(values.lastIndex)
        val range = if (horizontal) {
            val start = from?.first ?: track.x
            Rect(start, track.y, (to.first - start).coerceAtLeast(0), TRACK)
        } else {
            val end = from?.second ?: track.bottom
            Rect(track.x, to.second, TRACK, (end - to.second).coerceAtLeast(0))
        }
        ui.fillRounded(range, fade(tokens.primary), TRACK / 2)
        val focused = context.focusedWidget === this
        values.indices.forEach { index ->
            val (cx, cy) = thumbCentre(index)
            val thumb = Rect(cx - THUMB / 2, cy - THUMB / 2, THUMB, THUMB)
            val hovered = enabled && thumb.contains(mouseX.toDouble(), mouseY.toDouble())
            if ((focused && index == activeThumb) || hovered) {
                val ring = Rect(thumb.x - 2, thumb.y - 2, THUMB + 4, THUMB + 4)
                ui.fillRounded(ring, ThemeColors.withAlpha(tokens.ring, RING_ALPHA), ring.width / 2)
            }
            ui.fillRounded(thumb, fade(THUMB_FILL), THUMB / 2)
            ui.borderRounded(thumb, fade(tokens.primary), THUMB / 2)
        }
    }

    /**
     * Focuses an enabled slider on a left click and moves the closest thumb to the click.
     *
     * @param context the screen showing the widget
     * @param x the mouse x position
     * @param y the mouse y position
     * @param button the mouse button
     * @return whether the click was on this slider
     */
    override fun mouseClicked(context: UiContext, x: Double, y: Double, button: Int): Boolean {
        if (!isOver(x, y)) return false
        if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            context.focus(this)
            press(context, x, y)
        }
        return true
    }

    /**
     * Moves the active thumb with the arrow, Page, Home and End keys, and switches the active
     * thumb of a range slider with Enter or Space.
     *
     * @param context the screen showing the widget
     * @param event the key event
     * @return whether the key was handled
     */
    override fun keyPressed(context: UiContext, event: KeyEvent): Boolean {
        if (!enabled) return false
        val current = values[activeThumb]
        when (event.key()) {
            GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_UP -> moveThumb(activeThumb, current + step, context)
            GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_DOWN -> moveThumb(activeThumb, current - step, context)
            GLFW.GLFW_KEY_PAGE_UP -> moveThumb(activeThumb, current + PAGE_STEPS * step, context)
            GLFW.GLFW_KEY_PAGE_DOWN -> moveThumb(activeThumb, current - PAGE_STEPS * step, context)
            GLFW.GLFW_KEY_HOME -> moveThumb(activeThumb, min, context)
            GLFW.GLFW_KEY_END -> moveThumb(activeThumb, max, context)
            else -> {
                if (!isActivation(event) || values.size < 2) return false
                activeThumb = (activeThumb + 1) % values.size
            }
        }
        return true
    }

    /**
     * Sets the thumb values from their comma-separated form, if it has one value per thumb.
     *
     * @param value the thumb values
     */
    override fun applyValue(value: String) {
        val parsed = SliderValues.parse(value)?.takeIf { it.size == values.size } ?: return
        parsed.map { SliderValues.snap(it, min, max, step) }.sorted().forEachIndexed { index, v -> values[index] = v }
    }

    /**
     * Holds the slider sizes and key steps.
     */
    private companion object {
        /**
         * The diameter of a thumb.
         */
        const val THUMB: Int = 10

        /**
         * The thickness of the track.
         */
        const val TRACK: Int = 4

        /**
         * The default length of a vertical slider.
         */
        const val VERTICAL_LENGTH: Int = 88

        /**
         * The number of steps Page Up and Page Down move.
         */
        const val PAGE_STEPS: Int = 10

        /**
         * The opacity of the ring around a focused or hovered thumb.
         */
        const val RING_ALPHA: Float = 0.5f

        /**
         * The fill of a thumb.
         */
        const val THUMB_FILL: Int = 0xFFFFFFFF.toInt()

        /**
         * The bias that makes a press between two equal thumbs pick the one on its side.
         */
        const val EPSILON: Double = 1e-9
    }
}
