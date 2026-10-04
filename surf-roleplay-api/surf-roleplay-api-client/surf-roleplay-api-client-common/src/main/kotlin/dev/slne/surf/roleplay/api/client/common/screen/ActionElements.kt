package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.text.Component

/**
 * The look of a button.
 */
enum class ButtonVariant {
    /**
     * A filled button in the primary colour.
     */
    DEFAULT,

    /**
     * A filled button in the destructive colour.
     */
    DESTRUCTIVE,

    /**
     * A bordered button on the background.
     */
    OUTLINE,

    /**
     * A filled button in the secondary colour.
     */
    SECONDARY,

    /**
     * A button without background until hovered.
     */
    GHOST,

    /**
     * A button that looks like a link.
     */
    LINK,
}

/**
 * The size of a button. The icon sizes are square and show only the icon.
 */
enum class ButtonSize {
    /**
     * The regular size.
     */
    DEFAULT,

    /**
     * The smallest size.
     */
    XS,

    /**
     * A small size.
     */
    SM,

    /**
     * A large size.
     */
    LG,

    /**
     * A square button of the regular height.
     */
    ICON,

    /**
     * A square button of the smallest height.
     */
    ICON_XS,

    /**
     * A square button of the small height.
     */
    ICON_SM,

    /**
     * A square button of the large height.
     */
    ICON_LG,
}

/**
 * The direction in which a component arranges its parts.
 */
enum class Orientation {
    /**
     * From left to right.
     */
    HORIZONTAL,

    /**
     * From top to bottom.
     */
    VERTICAL,
}

/**
 * The look of a toggle.
 */
enum class ToggleVariant {
    /**
     * A toggle without border.
     */
    DEFAULT,

    /**
     * A bordered toggle.
     */
    OUTLINE,
}

/**
 * The size of a toggle.
 */
enum class ToggleSize {
    /**
     * The regular size.
     */
    DEFAULT,

    /**
     * A small size.
     */
    SM,

    /**
     * A large size.
     */
    LG,
}

/**
 * A group of buttons joined into one control.
 *
 * @property id the id of this element
 * @property children the buttons, texts and separators of the group, in order
 * @property orientation whether the group runs horizontally or vertically
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ButtonGroupElement(
    override val id: String,
    override val children: List<ScreenElement> = emptyList(),
    val orientation: Orientation = Orientation.HORIZONTAL,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A text part of a button group, drawn like a muted button.
 *
 * @property id the id of this element
 * @property text the text
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ButtonGroupTextElement(
    override val id: String,
    val text: Component,
    val icon: String? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A line between the parts of a button group.
 *
 * @property id the id of this element
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ButtonGroupSeparatorElement(
    override val id: String,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A two-state button. Pressing it switches its state and runs its handler; the handler reads
 * the new state with [ScreenValues.checked] under the toggle's id. Other inputs do not have to be
 * valid for a toggle press.
 *
 * @property id the id of this element
 * @property text the caption
 * @property icon the Lucide name of an icon drawn before the caption, or `null` for none
 * @property pressed whether the toggle is initially on
 * @property variant the look of the toggle
 * @property size the size of the toggle
 * @property enabled whether the toggle can be pressed
 * @property onToggle the handler run on every validated press, or `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ToggleElement(
    override val id: String,
    val text: Component = Component.empty(),
    val icon: String? = null,
    val pressed: Boolean = false,
    val variant: ToggleVariant = ToggleVariant.DEFAULT,
    val size: ToggleSize = ToggleSize.DEFAULT,
    val enabled: Boolean = true,
    val onToggle: ButtonHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * One item of a [ToggleGroupElement].
 *
 * @property value the value reported when the item is on
 * @property text the caption
 * @property icon the Lucide name of an icon drawn before the caption, or `null` for none
 * @property enabled whether the item can be switched
 */
data class ToggleGroupChoice(
    val value: String,
    val text: Component = Component.empty(),
    val icon: String? = null,
    val enabled: Boolean = true,
)

/**
 * A group of toggles joined into one input. Its value is the values of the items that are on,
 * joined by commas in item order.
 *
 * @property id the id of this element
 * @property items the items, in order
 * @property multiple whether several items can be on at once; otherwise at most one is
 * @property selected the values of the items that are initially on
 * @property variant the look of the items
 * @property size the size of the items
 * @property spacing the space between items in GUI pixels; zero joins them
 * @property orientation whether the items run horizontally or vertically
 * @property enabled whether the player can switch items
 * @property required whether having no item on is invalid
 * @property onChange the handler run on every validated change, or `null`
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class ToggleGroupElement(
    override val id: String,
    val items: List<ToggleGroupChoice>,
    val multiple: Boolean = false,
    val selected: List<String> = emptyList(),
    val variant: ToggleVariant = ToggleVariant.DEFAULT,
    val size: ToggleSize = ToggleSize.DEFAULT,
    val spacing: Int = 0,
    val orientation: Orientation = Orientation.HORIZONTAL,
    val enabled: Boolean = true,
    val required: Boolean = false,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement
