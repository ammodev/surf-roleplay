package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.text.Component

/**
 * The kind of text of a form field.
 */
enum class FieldTextKind {
    /**
     * The heading of a field set.
     */
    LEGEND,

    /**
     * The heading of a field set, drawn like a label.
     */
    LEGEND_LABEL,

    /**
     * The label of a field's input; a click on it focuses the input it names.
     */
    LABEL,

    /**
     * The title of a field whose input has its own label.
     */
    TITLE,

    /**
     * A muted description of a field.
     */
    DESCRIPTION,

    /**
     * The error of a field, in the destructive colour, and not drawn while empty.
     */
    ERROR,
}

/**
 * A group of related fields with a legend.
 *
 * @property id the id of this element
 * @property children the legend, fields and groups of the set, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class FieldSetElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A stack of fields.
 *
 * @property id the id of this element
 * @property children the fields and separators of the group, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class FieldGroupElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * One form field: its input with a label, description and error. [ScreenClick.fail] shows an
 * error of one of the field's inputs in the field's error text and draws the field as invalid.
 *
 * @property id the id of this element
 * @property children the parts of the field, in order
 * @property orientation whether the parts are stacked or placed side by side
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class FieldElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val orientation: Orientation = Orientation.VERTICAL,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A stack of a field's texts next to its input in a horizontal field.
 *
 * @property id the id of this element
 * @property children the texts, in order
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class FieldContentElement(
    override val id: String,
    override val children: List<ScreenElement>,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement

/**
 * A text of a form: a legend, label, title, description or error.
 *
 * @property id the id of this element
 * @property kind the kind of text
 * @property text the text
 * @property forId the id of the input a click on a label focuses, or `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class FieldTextElement(
    override val id: String,
    val kind: FieldTextKind,
    val text: Component,
    val forId: String? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A line between fields, optionally with a text in its middle.
 *
 * @property id the id of this element
 * @property text the text, or `null` for a plain line
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class FieldSeparatorElement(
    override val id: String,
    val text: Component? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement

/**
 * A form. Enter in one of its single-line inputs clicks its submit button.
 *
 * @property id the id of this element
 * @property children the content of the form, in order
 * @property submitId the id of the button that Enter clicks, or `null` for none
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 */
data class FormElement(
    override val id: String,
    override val children: List<ScreenElement>,
    val submitId: String? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ContainerElement
