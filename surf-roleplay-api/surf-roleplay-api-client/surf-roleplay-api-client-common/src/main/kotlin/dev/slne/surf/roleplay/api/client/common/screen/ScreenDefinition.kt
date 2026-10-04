package dev.slne.surf.roleplay.api.client.common.screen

import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputRef
import net.kyori.adventure.text.Component
import java.time.LocalDate
import java.util.UUID

/**
 * A generic server-driven screen, ready to be opened for a player.
 *
 * @property title the title shown above the screen
 * @property root the root element
 * @property closable whether the player can close the screen with Escape
 * @property onClose the handler run when the screen is closed for any reason, or `null` for none
 * @property theme the name of the theme the screen is drawn with, such as [ScreenThemes.POLICE];
 *           `null` uses the parent screen's theme, or [ScreenThemes.DEFAULT] without a parent;
 *           unknown names draw the default theme
 * @property variant the light or dark variant of the theme; `null` uses the parent screen's
 *           variant, or [ScreenVariant.DARK] without a parent
 * @throws IllegalArgumentException if two elements of the tree share an id
 */
data class ScreenDefinition(
    val title: Component,
    val root: ScreenElement,
    val closable: Boolean = true,
    val onClose: CloseHandler? = null,
    val theme: String? = null,
    val variant: ScreenVariant? = null,
) {
    init {
        val ids = mutableListOf<String>()
        collectIds(root, ids)
        val duplicates = ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        require(duplicates.isEmpty()) { "Screen element ids are not unique: $duplicates" }
    }

    /**
     * Holds the id collection helper.
     */
    private companion object {
        /**
         * Adds the id of an element and of its descendants to a list.
         *
         * @param element the element
         * @param ids the list to add to
         */
        fun collectIds(element: ScreenElement, ids: MutableList<String>) {
            ids += element.id
            if (element is ContainerElement) element.children.forEach { collectIds(it, ids) }
        }
    }
}

/**
 * Handles a click on a button.
 */
fun interface ButtonHandler {
    /**
     * Handles a click. It runs only after the click and every submitted input value passed
     * validation.
     *
     * @param click the click
     */
    fun onClick(click: ScreenClick)
}

/**
 * Handles a change of an input that reports its changes at once.
 */
fun interface ChangeHandler {
    /**
     * Handles a change. It runs only after the new value passed the input's constraints.
     *
     * @param change the change
     */
    fun onChange(change: ScreenInputChange)
}

/**
 * A validated change of an input's value.
 *
 * @property screen the screen the input is on
 * @property inputId the id of the changed input
 * @property value the new value, in the string form of [ScreenValues.all]
 * @property values the values of every input of the screen after the change
 */
data class ScreenInputChange(val screen: OpenScreen, val inputId: String, val value: String, val values: ScreenValues) {
    /**
     * Returns the typed value of an input after the change, as [ScreenValues.get] does.
     *
     * @param ref the reference to the input
     * @return the typed value
     */
    operator fun <T> get(ref: InputRef<T>): T = values[ref]
}

/**
 * Handles the closing of a screen.
 */
fun interface CloseHandler {
    /**
     * Handles the closing of a screen.
     *
     * @param screen the screen, which is no longer open
     */
    fun onClose(screen: OpenScreen)
}

/**
 * A validated click on a button.
 *
 * @property screen the screen the button is on
 * @property buttonId the id of the clicked button
 * @property values the validated values of the screen's inputs
 */
data class ScreenClick(val screen: OpenScreen, val buttonId: String, val values: ScreenValues) {
    /**
     * Shows errors of the submitted inputs, as [OpenScreen.showErrors] does.
     *
     * @param errors the error of every invalid input, keyed by input id
     */
    fun fail(errors: Map<String, Component>) = screen.showErrors(errors)

    /**
     * Returns the typed value of an input, as [ScreenValues.get] does.
     *
     * @param ref the reference to the input
     * @return the typed value
     */
    operator fun <T> get(ref: InputRef<T>): T = values[ref]
}

/**
 * The validated input values of a screen, keyed by input element id.
 *
 * @property all the values in their string form: text for text inputs, a decimal number or an
 *           empty string for number inputs, `true` or `false` for checkboxes, switches and
 *           toggles, the option value or an empty string for selects, native selects and radio
 *           groups, comma-separated values for comboboxes, toggle groups and sliders, and
 *           ISO dates for calendars
 */
class ScreenValues(val all: Map<String, String>) {

    /**
     * Returns the typed value of an input referenced by the component DSL.
     *
     * @param ref the reference to the input
     * @return the value parsed by the reference: an empty text, `null`, `false` or an empty list
     *         if the value is empty or the screen has no such input, as fits the input's type
     */
    operator fun <T> get(ref: InputRef<T>): T = ref.parse(all[ref.id])

    /**
     * Returns the text of a text input.
     *
     * @param id the input id
     * @return the text, or `null` if the screen has no such input
     */
    fun text(id: String): String? = all[id]

    /**
     * Returns the number of a number input.
     *
     * @param id the input id
     * @return the number, or `null` if the input is empty or the screen has no such input
     */
    fun number(id: String): Long? = all[id]?.toLongOrNull()

    /**
     * Returns whether a checkbox is checked.
     *
     * @param id the checkbox id
     * @return whether it is checked, or `null` if the screen has no such checkbox
     */
    fun checked(id: String): Boolean? = all[id]?.let { it == "true" }

    /**
     * Returns the thumb values of a slider.
     *
     * @param id the slider id
     * @return the values in ascending order, or `null` if the screen has no such slider
     */
    fun numbers(id: String): List<Double>? = all[id]?.split(',')?.mapNotNull { it.trim().toDoubleOrNull() }

    /**
     * Returns the selected dates of a calendar.
     *
     * @param id the calendar id
     * @return the dates in ascending order, a range as its first and last date, or `null` if the
     *         screen has no such calendar
     */
    fun dates(id: String): List<LocalDate>? = all[id]?.split(',', '/')?.mapNotNull { runCatching { LocalDate.parse(it.trim()) }.getOrNull() }

    /**
     * Returns the selected option of a select, native select, radio group or single combobox.
     *
     * @param id the input id
     * @return the value of the selected option, or `null` if nothing is selected or the screen
     *         has no such input
     */
    fun selected(id: String): String? = all[id]?.takeIf { it.isNotEmpty() }

    /**
     * Returns the selected values of a combobox or toggle group.
     *
     * @param id the input id
     * @return the selected values, or `null` if the screen has no such input
     */
    fun list(id: String): List<String>? = all[id]?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }

    /**
     * Returns a readable description of the values.
     *
     * @return the description
     */
    override fun toString(): String = "ScreenValues($all)"
}

/**
 * A screen that is open for a player.
 *
 * Every member must be used on the thread that owns the viewer, and throws
 * [IllegalStateException] otherwise.
 */
interface OpenScreen {
    /**
     * The session id of the screen, unique for its viewer.
     */
    val sessionId: Int

    /**
     * The unique id of the player who sees the screen.
     */
    val viewer: UUID

    /**
     * Whether the screen is still open.
     */
    val isOpen: Boolean

    /**
     * Changes the open screen. The changes are applied in order, both to the server's copy of the
     * screen and on the player's client. Does nothing if the screen is closed.
     *
     * @param changes the builder that records the changes
     */
    fun patch(changes: ScreenPatchBuilder.() -> Unit)

    /**
     * Shows the errors of inputs found by a check the server made. Every field that holds an
     * input with an error shows the error in its error text and is drawn as invalid, and the input
     * is drawn as invalid until the player changes it. Every other field and input loses its error.
     * Does nothing for screens without inputs, or if the screen is closed.
     *
     * @param errors the error of every invalid input, keyed by input id
     */
    fun showErrors(errors: Map<String, Component>)

    /**
     * Closes the screen and every screen opened on top of it. Does nothing if the screen is
     * already closed.
     */
    fun close()
}

/**
 * One change to an open screen, addressed by element id.
 */
sealed interface ScreenChange {
    /**
     * Replaces an element, including its children.
     *
     * @property targetId the id of the element to replace
     * @property element the element that takes its place
     */
    data class Replace(val targetId: String, val element: ScreenElement) : ScreenChange

    /**
     * Inserts an element into a container.
     *
     * @property parentId the id of the container
     * @property index the position among the container's children; a value at or beyond the
     *           number of children appends the element
     * @property element the element to insert
     */
    data class Insert(val parentId: String, val index: Int, val element: ScreenElement) : ScreenChange

    /**
     * Removes an element and its children.
     *
     * @property targetId the id of the element to remove
     */
    data class Remove(val targetId: String) : ScreenChange

    /**
     * Sets the text of a label, the caption of a button, the label of a checkbox or progress bar,
     * or the placeholder of a text input.
     *
     * @property targetId the id of the element
     * @property text the new text
     */
    data class SetText(val targetId: String, val text: Component) : ScreenChange

    /**
     * Sets the value of an input, in the string form of [ScreenValues.all].
     *
     * @property targetId the id of the input
     * @property value the new value
     */
    data class SetValue(val targetId: String, val value: String) : ScreenChange

    /**
     * Opens or closes an overlay.
     *
     * @property targetId the id of the overlay
     * @property open whether the overlay is open
     */
    data class SetOpen(val targetId: String, val open: Boolean) : ScreenChange

    /**
     * Sets the filled fraction of a progress bar.
     *
     * @property targetId the id of the progress bar
     * @property progress the filled fraction, from `0` to `1`
     */
    data class SetProgress(val targetId: String, val progress: Float) : ScreenChange

    /**
     * Sets whether a button or input can be used.
     *
     * @property targetId the id of the element
     * @property enabled whether the element can be used
     */
    data class SetEnabled(val targetId: String, val enabled: Boolean) : ScreenChange

    /**
     * Replaces the options of a combobox, keeping its selection and the query the player typed.
     * The player's client keeps showing selected options that are missing from the new groups, and
     * the server keeps accepting every option it offered before.
     *
     * @property targetId the id of the combobox
     * @property groups the new option groups
     */
    data class SetOptions(val targetId: String, val groups: List<SelectChoiceGroup>) : ScreenChange

    /**
     * Marks a field or an input as invalid, or clears the mark. An input loses the mark when the
     * player changes it.
     *
     * @property targetId the id of the field or input
     * @property invalid whether it is invalid
     */
    data class SetInvalid(val targetId: String, val invalid: Boolean) : ScreenChange
}

/**
 * Records the changes of one patch to an open screen.
 */
@ScreenDsl
class ScreenPatchBuilder {
    /**
     * The recorded changes, in order.
     */
    private val recorded = mutableListOf<ScreenChange>()

    /**
     * The recorded changes, in order.
     */
    val changes: List<ScreenChange> get() = recorded.toList()

    /**
     * Replaces an element.
     *
     * @param targetId the id of the element to replace
     * @param element the element that takes its place
     */
    fun replace(targetId: String, element: ScreenElement) {
        recorded += ScreenChange.Replace(targetId, element)
    }

    /**
     * Replaces an element with one built by the element DSL.
     *
     * @param targetId the id of the element to replace
     * @param build the builder that adds exactly one element
     * @throws IllegalStateException if [build] does not add exactly one element
     */
    fun replace(targetId: String, build: ElementsBuilder.() -> Unit) {
        replace(targetId, ElementsBuilder().apply(build).single())
    }

    /**
     * Inserts an element into a container.
     *
     * @param parentId the id of the container
     * @param index the position among the container's children
     * @param element the element to insert
     */
    fun insert(parentId: String, index: Int, element: ScreenElement) {
        recorded += ScreenChange.Insert(parentId, index, element)
    }

    /**
     * Appends the elements built by the element DSL to a container, after its other children,
     * such as a new message of a chat view.
     *
     * @param parentId the id of the container
     * @param build the builder of the elements, appended in order
     */
    fun append(parentId: String, build: ElementsBuilder.() -> Unit) {
        ElementsBuilder().apply(build).elements.forEach { insert(parentId, Int.MAX_VALUE, it) }
    }

    /**
     * Removes an element.
     *
     * @param targetId the id of the element to remove
     */
    fun remove(targetId: String) {
        recorded += ScreenChange.Remove(targetId)
    }

    /**
     * Sets the text of an element.
     *
     * @param targetId the id of the element
     * @param text the new text
     */
    fun setText(targetId: String, text: Component) {
        recorded += ScreenChange.SetText(targetId, text)
    }

    /**
     * Sets the value of an input.
     *
     * @param targetId the id of the input
     * @param value the new value in its string form
     */
    fun setValue(targetId: String, value: String) {
        recorded += ScreenChange.SetValue(targetId, value)
    }

    /**
     * Opens or closes an overlay, such as a popover, menu or dialog.
     *
     * @param targetId the id of the overlay
     * @param open whether the overlay is open
     */
    fun setOpen(targetId: String, open: Boolean) {
        recorded += ScreenChange.SetOpen(targetId, open)
    }

    /**
     * Sets the filled fraction of a progress bar.
     *
     * @param targetId the id of the progress bar
     * @param progress the filled fraction, from `0` to `1`
     */
    fun setProgress(targetId: String, progress: Float) {
        recorded += ScreenChange.SetProgress(targetId, progress)
    }

    /**
     * Sets whether a button or input can be used.
     *
     * @param targetId the id of the element
     * @param enabled whether the element can be used
     */
    fun setEnabled(targetId: String, enabled: Boolean) {
        recorded += ScreenChange.SetEnabled(targetId, enabled)
    }

    /**
     * Replaces the options of a combobox.
     *
     * @param targetId the id of the combobox
     * @param groups the new option groups
     */
    fun setOptions(targetId: String, groups: List<SelectChoiceGroup>) {
        recorded += ScreenChange.SetOptions(targetId, groups)
    }

    /**
     * Marks a field or an input as invalid, or clears the mark.
     *
     * @param targetId the id of the field or input
     * @param invalid whether it is invalid
     */
    fun setInvalid(targetId: String, invalid: Boolean) {
        recorded += ScreenChange.SetInvalid(targetId, invalid)
    }
}
