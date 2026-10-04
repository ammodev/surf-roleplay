package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.text.Component

/**
 * The kind of text a single-line text field holds.
 */
enum class TextInputType {
    /**
     * Any text.
     */
    TEXT,

    /**
     * A password, drawn masked.
     */
    PASSWORD,

    /**
     * An email address, which must have the shape `name@domain.tld`.
     */
    EMAIL,
}

/**
 * The characters a one-time code input accepts.
 */
enum class OtpPattern {
    /**
     * The digits `0` to `9`.
     */
    DIGITS,

    /**
     * Latin letters and digits.
     */
    ALPHANUMERIC,
}

/**
 * Where an addon sits in an input group.
 */
enum class InputGroupAlign {
    /**
     * Before the control, on the same line.
     */
    INLINE_START,

    /**
     * After the control, on the same line.
     */
    INLINE_END,

    /**
     * Above the control, across the group's width.
     */
    BLOCK_START,

    /**
     * Below the control, across the group's width.
     */
    BLOCK_END,
}

/**
 * A multi-line text field whose text wraps at its width and scrolls vertically. Line breaks in
 * its value are `\n`.
 *
 * @property id the id of this element
 * @property value the initial text
 * @property placeholder the hint shown while the field is empty
 * @property rows the number of visible lines when the field fits its content
 * @property maxLength the maximum number of characters, or `null` for no limit
 * @property required whether an empty value is invalid
 * @property enabled whether the player can edit the field
 * @property onChange the handler run on every validated change, or `null`
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class TextareaElement(
    override val id: String,
    val value: String = "",
    val placeholder: Component = Component.empty(),
    val rows: Int = 3,
    val maxLength: Int? = null,
    val required: Boolean = false,
    val enabled: Boolean = true,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement {
    init {
        require(rows >= 1) { "A textarea needs at least one row: $rows" }
        require(maxLength == null || maxLength > 0) { "A maximum length must be positive: $maxLength" }
    }
}

/**
 * A text field or textarea joined with addons into one bordered control.
 *
 * @property id the id of this element
 * @property children exactly one [TextInputElement] or [TextareaElement] and any number of
 *           [InputGroupAddonElement]s
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 * @throws IllegalArgumentException if the children are not one control and addons
 */
data class InputGroupElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement {
    init {
        val controls = children.count { it is TextInputElement || it is TextareaElement }
        require(controls == 1) { "An input group needs exactly one text input or textarea, found $controls" }
        require(children.all { it is TextInputElement || it is TextareaElement || it is InputGroupAddonElement }) {
            "An input group holds only its control and addons"
        }
    }
}

/**
 * A row of texts, icons or buttons at one side of an input group's control.
 *
 * @property id the id of this element
 * @property align where the addon sits
 * @property children the parts of the addon, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class InputGroupAddonElement(
    override val id: String,
    val align: InputGroupAlign = InputGroupAlign.INLINE_START,
    override val children: List<ScreenElement> = emptyList(),
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A muted text in an input group addon.
 *
 * @property id the id of this element
 * @property text the text
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class InputGroupTextElement(
    override val id: String,
    val text: Component,
    val icon: String? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * An input for a one-time code, entered one character per slot. A non-empty value is valid only
 * once every slot is filled.
 *
 * @property id the id of this element
 * @property length the number of slots, from 1 to 12
 * @property groups the number of slots in each group, which are drawn apart with a separator;
 *           empty for a single group
 * @property pattern the characters the slots accept
 * @property value the initially entered characters
 * @property required whether an empty value is invalid
 * @property enabled whether the player can enter a code
 * @property onChange the handler run on every validated change, or `null`
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 * @throws IllegalArgumentException if the length is out of range, the groups do not add up to it,
 *         or the value does not fit
 */
data class InputOtpElement(
    override val id: String,
    val length: Int = 6,
    val groups: List<Int> = emptyList(),
    val pattern: OtpPattern = OtpPattern.DIGITS,
    val value: String = "",
    val required: Boolean = false,
    val enabled: Boolean = true,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement {
    init {
        require(length in 1..MAX_LENGTH) { "A one-time code has 1 to $MAX_LENGTH slots: $length" }
        require(groups.isEmpty() || (groups.all { it > 0 } && groups.sum() == length)) { "The groups $groups do not add up to $length slots" }
        require(value.length <= length && value.all { OtpSlots.accepts(pattern, it) }) { "The value does not fit the code: $value" }
    }

    /**
     * Holds the slot limit.
     */
    companion object {
        /**
         * The largest number of slots.
         */
        const val MAX_LENGTH: Int = 12
    }
}

/**
 * The characters one-time code slots accept.
 */
object OtpSlots {
    /**
     * Checks whether a slot accepts a character.
     *
     * @param pattern the pattern of the code
     * @param char the character
     * @return whether the character is an ASCII digit, or for [OtpPattern.ALPHANUMERIC] also an
     *         ASCII letter
     */
    fun accepts(pattern: OtpPattern, char: Char): Boolean = when (pattern) {
        OtpPattern.DIGITS -> char in '0'..'9'
        OtpPattern.ALPHANUMERIC -> char in '0'..'9' || char in 'a'..'z' || char in 'A'..'Z'
    }
}
