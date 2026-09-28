package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

/**
 * An element of a server-driven screen: a container or a widget.
 *
 * Every element has an id that is unique within its screen, and a width and height that its
 * parent container uses for layout.
 */
sealed interface ScreenElement {
    /**
     * The id of this element, unique within its screen.
     */
    val id: String

    /**
     * How wide this element is laid out.
     */
    val width: ElementSize

    /**
     * How tall this element is laid out.
     */
    val height: ElementSize
}

/**
 * An element that lays out child elements.
 */
sealed interface ContainerElement : ScreenElement {
    /**
     * The child elements, in layout order.
     */
    val children: List<ScreenElement>
}

/**
 * The ways an element can be sized along one axis.
 */
enum class ElementSizeMode {
    /**
     * As large as the element's content needs.
     */
    FIT,

    /**
     * A fixed number of GUI pixels.
     */
    FIXED,

    /**
     * A weighted share of the space the container has left after its other children.
     */
    GROW,
}

/**
 * How an element is sized along one axis.
 *
 * @property mode the sizing mode
 * @property value the size in GUI pixels for [ElementSizeMode.FIXED], the weight for
 *           [ElementSizeMode.GROW], and unused for [ElementSizeMode.FIT]
 */
data class ElementSize(val mode: ElementSizeMode, val value: Int = 0) {
    /**
     * Creates the common sizes.
     */
    companion object {
        /**
         * The size that fits the element's content.
         */
        val FIT: ElementSize = ElementSize(ElementSizeMode.FIT)

        /**
         * Creates a fixed size.
         *
         * @param pixels the size in GUI pixels
         * @return the size
         */
        fun fixed(pixels: Int): ElementSize = ElementSize(ElementSizeMode.FIXED, pixels)

        /**
         * Creates a size that takes a share of the remaining space.
         *
         * @param weight the share relative to the weights of the element's growing siblings
         * @return the size
         */
        fun grow(weight: Int = 1): ElementSize = ElementSize(ElementSizeMode.GROW, weight)
    }
}

/**
 * How children are placed along an axis of a container.
 */
enum class Alignment {
    /**
     * At the start of the axis.
     */
    START,

    /**
     * Centered on the axis.
     */
    CENTER,

    /**
     * At the end of the axis.
     */
    END,

    /**
     * Stretched over the whole cross axis. On the main axis it behaves like [START].
     */
    STRETCH,
}

/**
 * Space inside a container's edges, in GUI pixels.
 *
 * @property top the space at the top
 * @property right the space at the right
 * @property bottom the space at the bottom
 * @property left the space at the left
 */
data class Spacing(val top: Int = 0, val right: Int = 0, val bottom: Int = 0, val left: Int = 0) {
    /**
     * Creates common spacings.
     */
    companion object {
        /**
         * No space at any edge.
         */
        val NONE: Spacing = Spacing()

        /**
         * Creates the same space at every edge.
         *
         * @param pixels the space in GUI pixels
         * @return the spacing
         */
        fun all(pixels: Int): Spacing = Spacing(pixels, pixels, pixels, pixels)
    }
}

/**
 * A container that lays out its children from left to right.
 *
 * @property id the id of this element
 * @property children the child elements, from left to right
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 * @property gap the space between two children
 * @property padding the space inside the container's edges
 * @property mainAlign how the children are placed horizontally
 * @property crossAlign how the children are placed vertically
 */
data class RowElement(
    override val id: String,
    override val children: List<ScreenElement> = emptyList(),
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
    val gap: Int = 0,
    val padding: Spacing = Spacing.NONE,
    val mainAlign: Alignment = Alignment.START,
    val crossAlign: Alignment = Alignment.START,
) : ContainerElement

/**
 * A container that lays out its children from top to bottom.
 *
 * @property id the id of this element
 * @property children the child elements, from top to bottom
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 * @property gap the space between two children
 * @property padding the space inside the container's edges
 * @property mainAlign how the children are placed vertically
 * @property crossAlign how the children are placed horizontally
 */
data class ColumnElement(
    override val id: String,
    override val children: List<ScreenElement> = emptyList(),
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
    val gap: Int = 0,
    val padding: Spacing = Spacing.NONE,
    val mainAlign: Alignment = Alignment.START,
    val crossAlign: Alignment = Alignment.START,
) : ContainerElement

/**
 * A scrollable column of child elements, stretched across the list's width.
 *
 * @property id the id of this element
 * @property children the child elements, from top to bottom
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 * @property gap the space between two children
 */
data class ScrollListElement(
    override val id: String,
    override val children: List<ScreenElement> = emptyList(),
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
    val gap: Int = 0,
) : ContainerElement

/**
 * A piece of styled text.
 *
 * @property id the id of this element
 * @property text the text
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class LabelElement(
    override val id: String,
    val text: Component,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A button. Clicking an enabled button runs its handler with the screen's validated input
 * values.
 *
 * A button that submits input is accepted only if every input of the screen is valid. A button
 * that does not, such as a back or cancel button, is always accepted; its handler receives the
 * valid values, and invalid inputs keep their last valid value.
 *
 * @property id the id of this element
 * @property text the caption
 * @property enabled whether the button can be clicked
 * @property onClick the handler run on the server when the player clicks the button, or `null`
 *           for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 * @property submitsInput whether a click requires every input of the screen to be valid
 */
data class ButtonElement(
    override val id: String,
    val text: Component,
    val enabled: Boolean = true,
    val onClick: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
    val submitsInput: Boolean = true,
) : ScreenElement

/**
 * A single-line text field.
 *
 * @property id the id of this element
 * @property value the initial text
 * @property placeholder the hint shown while the field is empty
 * @property maxLength the maximum number of characters, or `null` for no limit
 * @property required whether an empty value is invalid
 * @property enabled whether the player can edit the field
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TextInputElement(
    override val id: String,
    val value: String = "",
    val placeholder: Component = Component.empty(),
    val maxLength: Int? = null,
    val required: Boolean = false,
    val enabled: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A field for a whole number.
 *
 * @property id the id of this element
 * @property value the initial number, or `null` for an empty field
 * @property min the smallest allowed number, or `null` for no lower bound
 * @property max the largest allowed number, or `null` for no upper bound
 * @property required whether an empty value is invalid
 * @property enabled whether the player can edit the field
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class NumberInputElement(
    override val id: String,
    val value: Long? = null,
    val min: Long? = null,
    val max: Long? = null,
    val required: Boolean = false,
    val enabled: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A box that is checked or not, with a label next to it.
 *
 * @property id the id of this element
 * @property label the label
 * @property checked whether the box is initially checked
 * @property enabled whether the player can toggle the box
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class CheckboxElement(
    override val id: String,
    val label: Component = Component.empty(),
    val checked: Boolean = false,
    val enabled: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A choice of one option from a list.
 *
 * @property id the id of this element
 * @property options the options, in display order
 * @property selected the value of the initially selected option, or `null` for none
 * @property required whether having no selection is invalid
 * @property enabled whether the player can change the selection
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class DropdownElement(
    override val id: String,
    val options: List<DropdownChoice>,
    val selected: String? = null,
    val required: Boolean = false,
    val enabled: Boolean = true,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * One option of a [DropdownElement].
 *
 * @property value the value reported when this option is selected
 * @property label the label shown for this option
 */
data class DropdownChoice(val value: String, val label: Component)

/**
 * A texture drawn over the element's area.
 *
 * @property id the id of this element
 * @property texture the texture, such as `surf-roleplay:textures/gui/logo.png`
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ImageElement(
    override val id: String,
    val texture: Key,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A bar that shows how far something has progressed.
 *
 * @property id the id of this element
 * @property progress the filled fraction, from `0` to `1`
 * @property label the text drawn over the bar, or `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ProgressElement(
    override val id: String,
    val progress: Float,
    val label: Component? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement
