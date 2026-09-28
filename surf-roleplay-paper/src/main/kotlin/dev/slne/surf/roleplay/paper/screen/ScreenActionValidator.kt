package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.CheckboxElement
import dev.slne.surf.roleplay.api.client.common.screen.DropdownElement
import dev.slne.surf.roleplay.api.client.common.screen.NumberInputElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import dev.slne.surf.roleplay.protocol.screen.InputValue

/**
 * Validates a player's click on a generic screen against the server's copy of the screen.
 *
 * A click is accepted only if it targets an enabled button that exists in the tree, and every
 * submitted value belongs to an input of the tree, is submitted once and keeps its value if the
 * input is disabled. For a button that submits input, every value must also satisfy its input's
 * constraints. For a button that does not, values that break their constraints are ignored and
 * their inputs keep their current value.
 */
object ScreenActionValidator {

    /**
     * The outcome of a validation.
     */
    sealed interface Result {
        /**
         * The click is valid.
         *
         * @property values the value of every input of the screen in its string form: the
         *           submitted value, or the current value for inputs that were not submitted
         */
        data class Accepted(val values: Map<String, String>) : Result

        /**
         * The click is invalid.
         *
         * @property reason why the click was rejected, for the log
         */
        data class Rejected(val reason: String) : Result
    }

    /**
     * Validates a click.
     *
     * @param tree the server's copy of the screen
     * @param widgetId the id of the clicked widget
     * @param submitted the submitted input values
     * @return the outcome
     */
    fun validate(tree: ServerScreenTree, widgetId: String, submitted: List<InputValue>): Result {
        val button = when (val widget = tree.find(widgetId)) {
            null -> return Result.Rejected("unknown widget ${display(widgetId)}")
            !is ButtonElement -> return Result.Rejected("widget ${display(widgetId)} is not a button")
            else -> widget
        }
        if (!button.enabled) return Result.Rejected("button ${display(widgetId)} is disabled")

        val values = LinkedHashMap<String, String>()
        tree.elements().forEach { element -> currentValue(element)?.let { values[element.id] = it } }

        val seen = mutableSetOf<String>()
        for (value in submitted) {
            val id = value.widgetId
            if (!seen.add(id)) return Result.Rejected("input ${display(id)} was submitted twice")
            val element = tree.find(id) ?: return Result.Rejected("unknown input ${display(id)}")
            val current = currentValue(element) ?: return Result.Rejected("widget ${display(id)} is not an input")
            if (!isEnabled(element)) {
                if (value.value != current) return Result.Rejected("input ${display(id)} is disabled but its value changed")
                continue
            }
            val violation = constraintViolation(element, value.value)
            if (violation != null) {
                if (button.submitsInput) return Result.Rejected("input ${display(id)}: $violation")
                continue
            }
            values[id] = value.value
        }
        return Result.Accepted(values)
    }

    /**
     * Formats a client-supplied id for a rejection reason: quoted, cut to a short length and with
     * control characters replaced.
     *
     * @param id the id
     * @return the printable id
     */
    private fun display(id: String): String {
        val shown = id.take(MAX_SHOWN_ID).map { if (it.isISOControl()) '?' else it }.joinToString("")
        return if (id.length > MAX_SHOWN_ID) "'$shown…'" else "'$shown'"
    }

    /**
     * The largest number of characters of a client-supplied id shown in a rejection reason.
     */
    private const val MAX_SHOWN_ID = 48

    /**
     * Returns an input's current value in its string form.
     *
     * @param element the element
     * @return the value, or `null` if the element is not an input
     */
    private fun currentValue(element: ScreenElement): String? = when (element) {
        is TextInputElement -> element.value
        is NumberInputElement -> element.value?.toString() ?: ""
        is CheckboxElement -> element.checked.toString()
        is DropdownElement -> element.selected ?: ""
        else -> null
    }

    /**
     * Returns whether an input can be edited by the player.
     *
     * @param element the input
     * @return whether it is enabled
     */
    private fun isEnabled(element: ScreenElement): Boolean = when (element) {
        is TextInputElement -> element.enabled
        is NumberInputElement -> element.enabled
        is CheckboxElement -> element.enabled
        is DropdownElement -> element.enabled
        else -> false
    }

    /**
     * Checks a submitted value against an input's constraints.
     *
     * @param element the input
     * @param value the submitted value
     * @return a description of the violated constraint, or `null` if the value is valid
     */
    private fun constraintViolation(element: ScreenElement, value: String): String? = when (element) {
        is TextInputElement -> when {
            element.maxLength != null && value.length > element.maxLength!! -> "value is too long"
            element.required && value.isEmpty() -> "value is required"
            else -> null
        }

        is NumberInputElement -> when {
            value.isEmpty() -> if (element.required) "value is required" else null
            else -> {
                val number = value.toLongOrNull()
                when {
                    number == null -> "value is not a number"
                    element.min != null && number < element.min!! -> "value is out of range"
                    element.max != null && number > element.max!! -> "value is out of range"
                    else -> null
                }
            }
        }

        is CheckboxElement -> if (value == "true" || value == "false") null else "value must be true or false"
        is DropdownElement -> when {
            value.isEmpty() -> if (element.required) "value is required" else null
            element.options.none { it.value == value } -> "value is not an option"
            else -> null
        }

        else -> "not an input"
    }
}
