package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.text.Component
import kotlin.math.abs

/**
 * The size of a switch.
 */
enum class SwitchSize {
    /**
     * The regular size.
     */
    DEFAULT,

    /**
     * A small size.
     */
    SM,
}

/**
 * A switch that is on or off. Handlers read its state with [ScreenValues.checked].
 *
 * @property id the id of this element
 * @property checked whether the switch is initially on
 * @property size the size of the switch
 * @property enabled whether the player can switch it
 * @property onChange the handler run on every validated change, or `null`
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class SwitchElement(
    override val id: String,
    val checked: Boolean = false,
    val size: SwitchSize = SwitchSize.DEFAULT,
    val enabled: Boolean = true,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * One option of a [RadioGroupElement].
 *
 * @property value the value of the group while this option is selected
 * @property label the label shown next to the radio button
 * @property enabled whether the option can be selected
 */
data class RadioChoice(val value: String, val label: Component, val enabled: Boolean = true)

/**
 * A choice of one option, each drawn as a radio button with its label. Handlers read the value
 * of the selected option with [ScreenValues.text]; it is empty while nothing is selected.
 *
 * @property id the id of this element
 * @property options the options, in order
 * @property selected the value of the initially selected option, or `null` for none
 * @property orientation whether the options run vertically or horizontally
 * @property required whether having no selection is invalid
 * @property enabled whether the player can change the selection
 * @property onChange the handler run on every validated change, or `null`
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 * @throws IllegalArgumentException if option values repeat or the selection is not an option
 */
data class RadioGroupElement(
    override val id: String,
    val options: List<RadioChoice>,
    val selected: String? = null,
    val orientation: Orientation = Orientation.VERTICAL,
    val required: Boolean = false,
    val enabled: Boolean = true,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement {
    init {
        require(options.map { it.value }.toSet().size == options.size) { "Radio option values are not unique" }
        require(selected == null || options.any { it.value == selected }) { "The selection is not an option: $selected" }
    }
}

/**
 * A slider with one thumb, or two for a range. Handlers read the thumb values with
 * [ScreenValues.numbers].
 *
 * @property id the id of this element
 * @property values the initial value of every thumb, ascending; one or two
 * @property min the smallest value
 * @property max the largest value
 * @property step the distance between two selectable values, starting at [min]
 * @property orientation whether the slider runs horizontally or vertically
 * @property enabled whether the player can move the thumbs
 * @property onChange the handler run on every validated change, or `null`
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 * @throws IllegalArgumentException if the range or step is empty, or the values are not one or
 *         two ascending selectable values
 */
data class SliderElement(
    override val id: String,
    val values: List<Double>,
    val min: Double = 0.0,
    val max: Double = 100.0,
    val step: Double = 1.0,
    val orientation: Orientation = Orientation.HORIZONTAL,
    val enabled: Boolean = true,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement {
    init {
        require(min.isFinite() && max.isFinite() && min < max) { "A slider needs a range: $min to $max" }
        require(step.isFinite() && step > 0) { "A slider needs a positive step: $step" }
        require(values.size in 1..2) { "A slider has one or two thumbs: ${values.size}" }
        require(values.zipWithNext().all { (a, b) -> a <= b }) { "The slider values are not ascending: $values" }
        require(values.all { it in min..max && onStep(it) }) { "The slider values are not selectable: $values" }
    }

    /**
     * Checks whether a value lies on a step from [min].
     *
     * @param value the value
     * @return whether the value is a whole number of steps from the minimum
     */
    private fun onStep(value: Double): Boolean {
        val steps = (value - min) / step
        return abs(steps - Math.round(steps)) <= STEP_TOLERANCE
    }

    /**
     * Holds the step tolerance.
     */
    private companion object {
        /**
         * How far from a whole number of steps a value may be.
         */
        const val STEP_TOLERANCE: Double = 1e-6
    }
}
