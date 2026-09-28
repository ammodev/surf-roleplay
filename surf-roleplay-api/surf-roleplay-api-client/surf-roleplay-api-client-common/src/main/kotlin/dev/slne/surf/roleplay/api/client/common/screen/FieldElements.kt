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

/**
 * Builds the children of a container element.
 *
 * @param children the builder of the children
 * @return the built children
 */
private fun build(children: ElementsBuilder.() -> Unit): List<ScreenElement> = ElementsBuilder().apply(children).elements.toList()

/**
 * Adds a form.
 *
 * @param id the id of the form
 * @param submitId the id of the button that Enter clicks, or `null` for none
 * @param width how wide the form is laid out
 * @param height how tall the form is laid out
 * @param children the builder of the form's content
 */
fun ElementsBuilder.form(
    id: String,
    submitId: String? = null,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    children: ElementsBuilder.() -> Unit,
) {
    elements += FormElement(id, build(children), submitId, width, height)
}

/**
 * Adds a field set.
 *
 * @param id the id of the set
 * @param width how wide the set is laid out
 * @param height how tall the set is laid out
 * @param children the builder of the set's legend, fields and groups
 */
fun ElementsBuilder.fieldSet(id: String, width: ElementSize = ElementSize.FIT, height: ElementSize = ElementSize.FIT, children: ElementsBuilder.() -> Unit) {
    elements += FieldSetElement(id, build(children), width, height)
}

/**
 * Adds a field group.
 *
 * @param id the id of the group
 * @param width how wide the group is laid out
 * @param height how tall the group is laid out
 * @param children the builder of the group's fields and separators
 */
fun ElementsBuilder.fieldGroup(id: String, width: ElementSize = ElementSize.FIT, height: ElementSize = ElementSize.FIT, children: ElementsBuilder.() -> Unit) {
    elements += FieldGroupElement(id, build(children), width, height)
}

/**
 * Adds a field.
 *
 * @param id the id of the field
 * @param orientation whether the parts are stacked or placed side by side
 * @param width how wide the field is laid out
 * @param height how tall the field is laid out
 * @param children the builder of the field's parts
 */
fun ElementsBuilder.field(
    id: String,
    orientation: Orientation = Orientation.VERTICAL,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    children: ElementsBuilder.() -> Unit,
) {
    elements += FieldElement(id, build(children), orientation, width, height)
}

/**
 * Adds the text stack of a horizontal field.
 *
 * @param id the id of the stack
 * @param width how wide the stack is laid out
 * @param children the builder of the texts
 */
fun ElementsBuilder.fieldContent(id: String, width: ElementSize = ElementSize.grow(), children: ElementsBuilder.() -> Unit) {
    elements += FieldContentElement(id, build(children), width)
}

/**
 * Adds the legend of a field set.
 *
 * @param id the id of the legend
 * @param text the legend
 * @param asLabel whether the legend is drawn like a label
 */
fun ElementsBuilder.fieldLegend(id: String, text: Component, asLabel: Boolean = false) {
    elements += FieldTextElement(id, if (asLabel) FieldTextKind.LEGEND_LABEL else FieldTextKind.LEGEND, text)
}

/**
 * Adds the label of a field.
 *
 * @param id the id of the label
 * @param text the label
 * @param forId the id of the input a click on the label focuses, or `null` for none
 */
fun ElementsBuilder.fieldLabel(id: String, text: Component, forId: String? = null) {
    elements += FieldTextElement(id, FieldTextKind.LABEL, text, forId)
}

/**
 * Adds the title of a field.
 *
 * @param id the id of the title
 * @param text the title
 */
fun ElementsBuilder.fieldTitle(id: String, text: Component) {
    elements += FieldTextElement(id, FieldTextKind.TITLE, text)
}

/**
 * Adds the description of a field.
 *
 * @param id the id of the description
 * @param text the description
 */
fun ElementsBuilder.fieldDescription(id: String, text: Component) {
    elements += FieldTextElement(id, FieldTextKind.DESCRIPTION, text)
}

/**
 * Adds the error text of a field, which [ScreenClick.fail] fills.
 *
 * @param id the id of the error text
 * @param text the initial error, empty for none
 */
fun ElementsBuilder.fieldError(id: String, text: Component = Component.empty()) {
    elements += FieldTextElement(id, FieldTextKind.ERROR, text)
}

/**
 * Adds a separator between fields.
 *
 * @param id the id of the separator
 * @param text the text in its middle, or `null` for a plain line
 */
fun ElementsBuilder.fieldSeparator(id: String, text: Component? = null) {
    elements += FieldSeparatorElement(id, text, ElementSize.grow())
}
