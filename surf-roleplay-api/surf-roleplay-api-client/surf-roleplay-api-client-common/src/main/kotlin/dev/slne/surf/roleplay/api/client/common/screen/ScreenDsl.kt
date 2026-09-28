package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

/**
 * Marks the builders of the screen DSL, so that an inner block cannot call the builder of an
 * outer one by accident.
 */
@DslMarker
annotation class ScreenDsl

/**
 * Builds a generic screen with the element DSL.
 *
 * ```
 * val definition = screen(Component.text("Anmeldung")) {
 *     column("root", gap = 4) {
 *         textInput("name", required = true)
 *         button("submit", Component.text("Senden")) { click -> ... }
 *     }
 * }
 * ```
 *
 * @param title the title shown above the screen
 * @param build the builder that sets the screen's options and adds exactly one root element
 * @return the screen definition
 * @throws IllegalStateException if [build] does not add exactly one root element
 * @throws IllegalArgumentException if two elements share an id
 */
fun screen(title: Component, build: ScreenBuilder.() -> Unit): ScreenDefinition = ScreenBuilder(title).apply(build).build()

/**
 * Builds a [ScreenDefinition].
 *
 * @property title the title shown above the screen
 */
@ScreenDsl
class ScreenBuilder internal constructor(private val title: Component) : ElementsBuilder() {
    /**
     * Whether the player can close the screen with Escape.
     */
    var closable: Boolean = true

    /**
     * The name of the theme the screen is drawn with, such as [ScreenThemes.POLICE], or `null` to
     * use the parent screen's theme.
     */
    var theme: String? = null

    /**
     * The light or dark variant of the theme, or `null` to use the parent screen's variant.
     */
    var variant: ScreenVariant? = null

    /**
     * The handler run when the screen is closed, or `null` for none.
     */
    private var closeHandler: CloseHandler? = null

    /**
     * Sets the handler run when the screen is closed for any reason.
     *
     * @param handler the handler
     */
    fun onClose(handler: CloseHandler) {
        closeHandler = handler
    }

    /**
     * Creates the definition.
     *
     * @return the definition
     * @throws IllegalStateException if the builder does not hold exactly one root element
     * @throws IllegalArgumentException if two elements share an id
     */
    internal fun build(): ScreenDefinition = ScreenDefinition(title, single(), closable, closeHandler, theme, variant)
}

/**
 * Collects elements, either as the root of a screen or as the children of a container.
 */
@ScreenDsl
open class ElementsBuilder {
    /**
     * The collected elements, in order.
     */
    internal val elements: MutableList<ScreenElement> = mutableListOf()

    /**
     * Returns the only collected element.
     *
     * @return the element
     * @throws IllegalStateException if not exactly one element was collected
     */
    internal fun single(): ScreenElement {
        check(elements.size == 1) { "Expected exactly one element, found ${elements.size}" }
        return elements.single()
    }

    /**
     * Adds a row whose children are built by a block.
     *
     * @param id the id of the row
     * @param width how wide the row is laid out
     * @param height how tall the row is laid out
     * @param gap the space between two children
     * @param padding the space inside the row's edges
     * @param mainAlign how the children are placed horizontally
     * @param crossAlign how the children are placed vertically
     * @param children the builder of the children
     */
    fun row(
        id: String,
        width: ElementSize = ElementSize.FIT,
        height: ElementSize = ElementSize.FIT,
        gap: Int = 0,
        padding: Spacing = Spacing.NONE,
        mainAlign: Alignment = Alignment.START,
        crossAlign: Alignment = Alignment.START,
        children: ElementsBuilder.() -> Unit,
    ) {
        elements += RowElement(id, ElementsBuilder().apply(children).elements.toList(), width, height, gap, padding, mainAlign, crossAlign)
    }

    /**
     * Adds a column whose children are built by a block.
     *
     * @param id the id of the column
     * @param width how wide the column is laid out
     * @param height how tall the column is laid out
     * @param gap the space between two children
     * @param padding the space inside the column's edges
     * @param mainAlign how the children are placed vertically
     * @param crossAlign how the children are placed horizontally
     * @param children the builder of the children
     */
    fun column(
        id: String,
        width: ElementSize = ElementSize.FIT,
        height: ElementSize = ElementSize.FIT,
        gap: Int = 0,
        padding: Spacing = Spacing.NONE,
        mainAlign: Alignment = Alignment.START,
        crossAlign: Alignment = Alignment.START,
        children: ElementsBuilder.() -> Unit,
    ) {
        elements += ColumnElement(id, ElementsBuilder().apply(children).elements.toList(), width, height, gap, padding, mainAlign, crossAlign)
    }

    /**
     * Adds a scroll list whose children are built by a block.
     *
     * @param id the id of the list
     * @param width how wide the list is laid out
     * @param height how tall the list is laid out
     * @param gap the space between two children
     * @param children the builder of the children
     */
    fun scrollList(
        id: String,
        width: ElementSize = ElementSize.FIT,
        height: ElementSize = ElementSize.FIT,
        gap: Int = 0,
        children: ElementsBuilder.() -> Unit,
    ) {
        elements += ScrollListElement(id, ElementsBuilder().apply(children).elements.toList(), width, height, gap)
    }

    /**
     * Adds a label.
     *
     * @param id the id of the label
     * @param text the text
     * @param width how wide the label is laid out
     * @param height how tall the label is laid out
     * @param icon the Lucide name of an icon drawn before the text, or `null` for none
     * @param forId the id of the input that a click on the label focuses, or `null` for none
     */
    fun label(
        id: String,
        text: Component,
        width: ElementSize = ElementSize.FIT,
        height: ElementSize = ElementSize.FIT,
        icon: String? = null,
        forId: String? = null,
    ) {
        elements += LabelElement(id, text, width, height, icon, forId)
    }

    /**
     * Adds a Lucide icon.
     *
     * @param id the id of the icon element
     * @param icon the Lucide name of the icon
     * @param size the side length when the icon fits its content
     * @param tint the theme colour the icon is tinted with
     * @param width how wide the icon is laid out
     * @param height how tall the icon is laid out
     */
    fun icon(
        id: String,
        icon: String,
        size: Int = 16,
        tint: IconTint = IconTint.FOREGROUND,
        width: ElementSize = ElementSize.FIT,
        height: ElementSize = ElementSize.FIT,
    ) {
        elements += IconElement(id, icon, size, tint, width, height)
    }

    /**
     * Adds a button.
     *
     * @param id the id of the button
     * @param text the caption
     * @param enabled whether the button can be clicked
     * @param width how wide the button is laid out
     * @param height how tall the button is laid out
     * @param submitsInput whether a click requires every input of the screen to be valid
     * @param icon the Lucide name of an icon drawn before the caption, or `null` for none
     * @param variant the look of the button
     * @param size the size of the button
     * @param onClick the handler run on a validated click, or `null` for none
     */
    fun button(
        id: String,
        text: Component,
        enabled: Boolean = true,
        width: ElementSize = ElementSize.FIT,
        height: ElementSize = ElementSize.FIT,
        submitsInput: Boolean = true,
        icon: String? = null,
        variant: ButtonVariant = ButtonVariant.DEFAULT,
        size: ButtonSize = ButtonSize.DEFAULT,
        onClick: ButtonHandler? = null,
    ) {
        elements += ButtonElement(id, text, enabled, onClick, width, height, submitsInput, icon, variant, size)
    }

    /**
     * Adds a text input.
     *
     * @param id the id of the input
     * @param value the initial text
     * @param placeholder the hint shown while the input is empty
     * @param maxLength the maximum number of characters, or `null` for no limit
     * @param required whether an empty value is invalid
     * @param enabled whether the player can edit the input
     * @param width how wide the input is laid out
     * @param height how tall the input is laid out
     * @param icon the Lucide name of an icon drawn at the start of the field, or `null` for none
     * @param type the kind of text the input holds
     * @param onChange the handler run on every validated change, or `null`
     */
    fun textInput(
        id: String,
        value: String = "",
        placeholder: Component = Component.empty(),
        maxLength: Int? = null,
        required: Boolean = false,
        enabled: Boolean = true,
        width: ElementSize = ElementSize.FIT,
        height: ElementSize = ElementSize.FIT,
        icon: String? = null,
        type: TextInputType = TextInputType.TEXT,
        onChange: ChangeHandler? = null,
    ) {
        elements += TextInputElement(id, value, placeholder, maxLength, required, enabled, width, height, icon, onChange, type)
    }

    /**
     * Adds a number input.
     *
     * @param id the id of the input
     * @param value the initial number, or `null` for an empty input
     * @param min the smallest allowed number, or `null` for no lower bound
     * @param max the largest allowed number, or `null` for no upper bound
     * @param required whether an empty value is invalid
     * @param enabled whether the player can edit the input
     * @param width how wide the input is laid out
     * @param height how tall the input is laid out
     * @param onChange the handler run on every validated change, or `null`
     */
    fun numberInput(
        id: String,
        value: Long? = null,
        min: Long? = null,
        max: Long? = null,
        required: Boolean = false,
        enabled: Boolean = true,
        width: ElementSize = ElementSize.FIT,
        height: ElementSize = ElementSize.FIT,
        onChange: ChangeHandler? = null,
    ) {
        elements += NumberInputElement(id, value, min, max, required, enabled, width, height, onChange)
    }

    /**
     * Adds a checkbox.
     *
     * @param id the id of the checkbox
     * @param label the label
     * @param checked whether the box is initially checked
     * @param enabled whether the player can toggle the box
     * @param width how wide the checkbox is laid out
     * @param height how tall the checkbox is laid out
     * @param onChange the handler run on every validated change, or `null`
     */
    fun checkbox(
        id: String,
        label: Component = Component.empty(),
        checked: Boolean = false,
        enabled: Boolean = true,
        width: ElementSize = ElementSize.FIT,
        height: ElementSize = ElementSize.FIT,
        onChange: ChangeHandler? = null,
    ) {
        elements += CheckboxElement(id, label, checked, enabled, width, height, onChange)
    }

    /**
     * Adds a dropdown.
     *
     * @param id the id of the dropdown
     * @param options the options, in display order
     * @param selected the value of the initially selected option, or `null` for none
     * @param required whether having no selection is invalid
     * @param enabled whether the player can change the selection
     * @param width how wide the dropdown is laid out
     * @param height how tall the dropdown is laid out
     * @param onChange the handler run on every validated change, or `null`
     */
    fun dropdown(
        id: String,
        options: List<DropdownChoice>,
        selected: String? = null,
        required: Boolean = false,
        enabled: Boolean = true,
        width: ElementSize = ElementSize.FIT,
        height: ElementSize = ElementSize.FIT,
        onChange: ChangeHandler? = null,
    ) {
        elements += DropdownElement(id, options, selected, required, enabled, width, height, onChange)
    }

    /**
     * Adds an image.
     *
     * @param id the id of the image
     * @param texture the texture
     * @param width how wide the image is laid out
     * @param height how tall the image is laid out
     */
    fun image(id: String, texture: Key, width: ElementSize = ElementSize.FIT, height: ElementSize = ElementSize.FIT) {
        elements += ImageElement(id, texture, width, height)
    }

    /**
     * Adds a progress bar.
     *
     * @param id the id of the bar
     * @param progress the filled fraction, from `0` to `1`
     * @param label the text drawn over the bar, or `null` for none
     * @param width how wide the bar is laid out
     * @param height how tall the bar is laid out
     */
    fun progress(
        id: String,
        progress: Float,
        label: Component? = null,
        width: ElementSize = ElementSize.FIT,
        height: ElementSize = ElementSize.FIT,
    ) {
        elements += ProgressElement(id, progress, label, width, height)
    }
}
