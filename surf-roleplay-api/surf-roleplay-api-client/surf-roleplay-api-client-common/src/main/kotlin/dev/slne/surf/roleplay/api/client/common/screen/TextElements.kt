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

/**
 * Adds a textarea.
 *
 * @param id the id of the textarea
 * @param value the initial text
 * @param placeholder the hint shown while the textarea is empty
 * @param rows the number of visible lines
 * @param maxLength the maximum number of characters, or `null` for no limit
 * @param required whether an empty value is invalid
 * @param enabled whether the player can edit the textarea
 * @param width how wide the textarea is laid out
 * @param height how tall the textarea is laid out
 * @param onChange the handler run on every validated change, or `null`
 */
fun ElementsBuilder.textarea(
    id: String,
    value: String = "",
    placeholder: Component = Component.empty(),
    rows: Int = 3,
    maxLength: Int? = null,
    required: Boolean = false,
    enabled: Boolean = true,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    onChange: ChangeHandler? = null,
) {
    elements += TextareaElement(id, value, placeholder, rows, maxLength, required, enabled, onChange, width, height)
}

/**
 * Adds an input group whose control and addons are built by a block.
 *
 * @param id the id of the group
 * @param width how wide the group is laid out
 * @param height how tall the group is laid out
 * @param children the builder of exactly one text input or textarea and its addons
 */
fun ElementsBuilder.inputGroup(
    id: String,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    children: ElementsBuilder.() -> Unit,
) {
    elements += InputGroupElement(id, ElementsBuilder().apply(children).elements.toList(), width, height)
}

/**
 * Adds an addon to an input group.
 *
 * @param id the id of the addon
 * @param align where the addon sits
 * @param children the builder of the addon's texts, icons and buttons
 */
fun ElementsBuilder.inputGroupAddon(id: String, align: InputGroupAlign = InputGroupAlign.INLINE_START, children: ElementsBuilder.() -> Unit) {
    elements += InputGroupAddonElement(id, align, ElementsBuilder().apply(children).elements.toList())
}

/**
 * Adds a muted text to an input group addon.
 *
 * @param id the id of the text
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 */
fun ElementsBuilder.inputGroupText(id: String, text: Component, icon: String? = null) {
    elements += InputGroupTextElement(id, text, icon)
}

/**
 * Adds a button to an input group addon: a ghost button of the smallest size unless set
 * otherwise. It does not submit input unless asked to.
 *
 * @param id the id of the button
 * @param text the caption
 * @param icon the Lucide name of the icon, or `null` for none
 * @param variant the look of the button
 * @param size the size of the button
 * @param enabled whether the button can be clicked
 * @param submitsInput whether a click requires every input of the screen to be valid
 * @param onClick the handler run on the server when the button is clicked, or `null`
 */
fun ElementsBuilder.inputGroupButton(
    id: String,
    text: Component = Component.empty(),
    icon: String? = null,
    variant: ButtonVariant = ButtonVariant.GHOST,
    size: ButtonSize = ButtonSize.XS,
    enabled: Boolean = true,
    submitsInput: Boolean = false,
    onClick: ButtonHandler? = null,
) {
    elements += ButtonElement(id, text, enabled, onClick, submitsInput = submitsInput, icon = icon, variant = variant, size = size)
}

/**
 * Adds a one-time code input.
 *
 * @param id the id of the input
 * @param length the number of slots
 * @param groups the number of slots in each group; empty for a single group
 * @param pattern the characters the slots accept
 * @param value the initially entered characters
 * @param required whether an empty value is invalid
 * @param enabled whether the player can enter a code
 * @param onChange the handler run on every validated change, or `null`
 */
fun ElementsBuilder.inputOtp(
    id: String,
    length: Int = 6,
    groups: List<Int> = emptyList(),
    pattern: OtpPattern = OtpPattern.DIGITS,
    value: String = "",
    required: Boolean = false,
    enabled: Boolean = true,
    onChange: ChangeHandler? = null,
) {
    elements += InputOtpElement(id, length, groups, pattern, value, required, enabled, onChange)
}
