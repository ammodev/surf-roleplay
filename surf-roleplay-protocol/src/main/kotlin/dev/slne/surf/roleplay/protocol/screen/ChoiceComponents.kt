package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The size of a switch.
 */
@Serializable
enum class SwitchSize {
    /**
     * The regular size.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * A small size.
     */
    @ProtoNumber(1)
    SM,
}

/**
 * A switch that is on or off. Its value is `true` or `false`.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property checked whether the switch is on
 * @property size the size of the switch
 * @property enabled whether the player can switch it
 * @property notifyChange whether the mod reports every change of the value at once
 */
@Serializable
@SerialName("switch")
data class SwitchNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val checked: Boolean = false,
    @ProtoNumber(5) val size: SwitchSize = SwitchSize.DEFAULT,
    @ProtoNumber(6) val enabled: Boolean = true,
    @ProtoNumber(7) val notifyChange: Boolean = false,
) : ScreenNode

/**
 * One option of a [RadioGroupNode].
 *
 * @property value the value of the group while this option is selected
 * @property label the label as component JSON
 * @property enabled whether the option can be selected
 */
@Serializable
data class RadioOption(
    @ProtoNumber(1) val value: String,
    @ProtoNumber(2) val label: String = "",
    @ProtoNumber(3) val enabled: Boolean = true,
)

/**
 * A choice of one option, each drawn as a radio button with its label. Its value is the value of
 * the selected option, or empty.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property options the options, in order
 * @property selected the value of the selected option, or `null` if none is selected
 * @property orientation whether the options run vertically or horizontally
 * @property required whether having no selection is invalid
 * @property enabled whether the player can change the selection
 * @property notifyChange whether the mod reports every change of the value at once
 */
@Serializable
@SerialName("radio_group")
data class RadioGroupNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val options: List<RadioOption> = emptyList(),
    @ProtoNumber(5) val selected: String? = null,
    @ProtoNumber(6) val orientation: Orientation = Orientation.VERTICAL,
    @ProtoNumber(7) val required: Boolean = false,
    @ProtoNumber(8) val enabled: Boolean = true,
    @ProtoNumber(9) val notifyChange: Boolean = false,
) : ScreenNode

/**
 * A slider with one thumb, or two for a range. Its value is the thumb values in ascending order,
 * joined by commas, each in its shortest decimal form.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property values the value of every thumb, ascending; one or two
 * @property min the smallest value
 * @property max the largest value
 * @property step the distance between two selectable values, starting at [min]
 * @property orientation whether the slider runs horizontally or vertically
 * @property enabled whether the player can move the thumbs
 * @property notifyChange whether the mod reports every change of the value at once
 */
@Serializable
@SerialName("slider")
data class SliderNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val values: List<Double> = listOf(0.0),
    @ProtoNumber(5) val min: Double = 0.0,
    @ProtoNumber(6) val max: Double = 100.0,
    @ProtoNumber(7) val step: Double = 1.0,
    @ProtoNumber(8) val orientation: Orientation = Orientation.HORIZONTAL,
    @ProtoNumber(9) val enabled: Boolean = true,
    @ProtoNumber(10) val notifyChange: Boolean = false,
) : ScreenNode

/**
 * The value arithmetic of sliders, shared by the server and the mod.
 */
object SliderValues {

    /**
     * Moves a value onto the closest selectable value within the range.
     *
     * @param value the value
     * @param min the smallest value
     * @param max the largest value
     * @param step the distance between two selectable values, starting at [min]
     * @return the snapped value
     */
    fun snap(value: Double, min: Double, max: Double, step: Double): Double {
        val clamped = value.coerceIn(min, max)
        val steps = Math.round((clamped - min) / step)
        return clean(min + steps * step).coerceIn(min, max)
    }

    /**
     * Checks whether a value is selectable: within the range and on a step.
     *
     * @param value the value
     * @param min the smallest value
     * @param max the largest value
     * @param step the distance between two selectable values, starting at [min]
     * @return whether the value is selectable
     */
    fun isSelectable(value: Double, min: Double, max: Double, step: Double): Boolean =
        value in min..max && kotlin.math.abs(snap(value, min, max, step) - value) <= EPSILON * maxOf(1.0, kotlin.math.abs(value))

    /**
     * Returns the shortest decimal form of a value, without a fraction for whole numbers.
     *
     * @param value the value
     * @return the decimal form, such as `25` or `0.5`
     */
    fun format(value: Double): String =
        java.math.BigDecimal.valueOf(clean(value)).stripTrailingZeros().toPlainString().let { if (it == "-0") "0" else it }

    /**
     * Parses the value of a slider.
     *
     * @param value the thumb values joined by commas
     * @return the thumb values, or `null` if a part is not a number
     */
    fun parse(value: String): List<Double>? =
        value.split(',').map { part -> part.trim().toDoubleOrNull()?.takeIf { it.isFinite() } ?: return null }

    /**
     * Rounds away the binary noise of step arithmetic.
     *
     * @param value the value
     * @return the value rounded to nine decimal places
     */
    private fun clean(value: Double): Double = Math.round(value * 1e9) / 1e9

    /**
     * The relative tolerance of the step check.
     */
    private const val EPSILON: Double = 1e-9
}
