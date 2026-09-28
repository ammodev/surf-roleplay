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
 * submitted value belongs to an input of the tree, is submitted once, keeps its value if the
 * input is disabled, and satisfies the input's constraints.
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
        when (val widget = tree.find(widgetId)) {
            null -> return Result.Rejected("unknown widget '$widgetId'")
            !is ButtonElement -> return Result.Rejected("widget '$widgetId' is not a button")
            else -> if (!widget.enabled) return Result.Rejected("button '$widgetId' is disabled")
        }

        val values = LinkedHashMap<String, String>()
        tree.elements().forEach { element -> currentValue(element)?.let { values[element.id] = it } }

        val seen = mutableSetOf<String>()
        for (value in submitted) {
            val id = value.widgetId
            if (!seen.add(id)) return Result.Rejected("input '$id' was submitted twice")
            val element = tree.find(id) ?: return Result.Rejected("unknown input '$id'")
            val current = currentValue(element) ?: return Result.Rejected("widget '$id' is not an input")
            if (!isEnabled(element)) {
                if (value.value != current) return Result.Rejected("input '$id' is disabled but its value changed")
                continue
            }
            constraintViolation(element, value.value)?.let { return Result.Rejected("input '$id': $it") }
            values[id] = value.value
        }
        return Result.Accepted(values)
    }

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
