package dev.slne.surf.roleplay.paper.screen

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
        val widget = tree.find(widgetId) ?: return Result.Rejected("unknown widget ${display(widgetId)}")
        val action = ElementRules.rule(widget)?.action?.invoke(widget)
            ?: return Result.Rejected("widget ${display(widgetId)} is not a button")
        if (!ElementRules.isEnabled(widget)) return Result.Rejected("button ${display(widgetId)} is disabled")

        val values = LinkedHashMap<String, String>()
        tree.elements().forEach { element -> ElementRules.input(element)?.current?.invoke(element)?.let { values[element.id] = it } }

        val seen = mutableSetOf<String>()
        for (value in submitted) {
            val id = value.widgetId
            if (!seen.add(id)) return Result.Rejected("input ${display(id)} was submitted twice")
            val element = tree.find(id) ?: return Result.Rejected("unknown input ${display(id)}")
            val current = ElementRules.input(element)?.current?.invoke(element) ?: return Result.Rejected("widget ${display(id)} is not an input")
            if (!ElementRules.isEnabled(element)) {
                if (value.value != current) return Result.Rejected("input ${display(id)} is disabled but its value changed")
                continue
            }
            val violation = ElementRules.input(element)!!.violation(element, value.value)
            if (violation != null) {
                if (action.submitsInput) return Result.Rejected("input ${display(id)}: $violation")
                continue
            }
            values[id] = value.value
        }
        if (action.submitsInput) {
            for (element in tree.elements()) {
                val rule = ElementRules.input(element) ?: continue
                if (!ElementRules.isEnabled(element)) continue
                rule.violation(element, values.getValue(element.id))?.let { return Result.Rejected("input ${display(element.id)}: $it") }
            }
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
    internal fun display(id: String): String {
        val shown = id.take(MAX_SHOWN_ID).map { if (it.isISOControl()) '?' else it }.joinToString("")
        return if (id.length > MAX_SHOWN_ID) "'$shown…'" else "'$shown'"
    }

    /**
     * The largest number of characters of a client-supplied id shown in a rejection reason.
     */
    private const val MAX_SHOWN_ID = 48
}
