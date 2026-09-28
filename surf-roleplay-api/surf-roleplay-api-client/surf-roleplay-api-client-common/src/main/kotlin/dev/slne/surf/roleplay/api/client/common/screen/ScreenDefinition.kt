package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.text.Component
import java.util.UUID

/**
 * A generic server-driven screen, ready to be opened for a player.
 *
 * @property title the title shown above the screen
 * @property root the root element
 * @property closable whether the player can close the screen with Escape
 * @property onClose the handler run when the screen is closed for any reason, or `null` for none
 */
data class ScreenDefinition(
    val title: Component,
    val root: ScreenElement,
    val closable: Boolean = true,
    val onClose: CloseHandler? = null,
)

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
data class ScreenClick(val screen: OpenScreen, val buttonId: String, val values: ScreenValues)

/**
 * The validated input values of a screen, keyed by input element id.
 *
 * @property all the values in their string form: text for text inputs, a decimal number or an
 *           empty string for number inputs, `true` or `false` for checkboxes, and the option value
 *           or an empty string for dropdowns
 */
class ScreenValues(val all: Map<String, String>) {

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
     * Returns the selected option of a dropdown.
     *
     * @param id the dropdown id
     * @return the value of the selected option, or `null` if nothing is selected or the screen
     *         has no such dropdown
     */
    fun selected(id: String): String? = all[id]?.takeIf { it.isNotEmpty() }

    /**
     * Returns a readable description of the values.
     *
     * @return the description
     */
    override fun toString(): String = "ScreenValues($all)"
}

/**
 * A screen that is open for a player.
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
}
