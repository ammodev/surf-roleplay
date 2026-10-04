package dev.slne.surf.roleplay.api.client.common.screen.dsl

import dev.slne.surf.roleplay.api.client.common.screen.ScreenValues
import java.time.LocalDate

/**
 * A typed reference to an input of a screen, returned by the input components of the component
 * DSL. Handlers read the input's value with `values[ref]` on [ScreenValues].
 *
 * @param T the type of the input's value
 * @property id the id of the input element
 * @property parse the function that turns the value in its string form, or `null` if the screen
 *           has no such input, into a typed value
 */
class InputRef<T> internal constructor(val id: String, internal val parse: (String?) -> T) {
    /**
     * Returns a readable description of the reference.
     *
     * @return the description
     */
    override fun toString(): String = "InputRef($id)"
}

/**
 * The parsers of the input value formats documented on [ScreenValues.all].
 */
internal object InputParsers {
    /**
     * Parses the text of a text input, textarea or one-time code input; empty if missing.
     */
    val text: (String?) -> String = { it ?: "" }

    /**
     * Parses the number of a number input; `null` if empty, not a number or missing.
     */
    val number: (String?) -> Long? = { it?.toLongOrNull() }

    /**
     * Parses the state of a checkbox, switch or toggle; `false` if missing.
     */
    val checked: (String?) -> Boolean = { it == "true" }

    /**
     * Parses the selected option of a select, native select, radio group or single combobox;
     * `null` if nothing is selected or the input is missing.
     */
    val selected: (String?) -> String? = { it?.takeIf { value -> value.isNotEmpty() } }

    /**
     * Parses the comma-separated values of a toggle group or multiple combobox; empty if missing.
     */
    val list: (String?) -> List<String> = { value ->
        value?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
    }

    /**
     * Parses the comma-separated thumb values of a slider; empty if missing.
     */
    val numbers: (String?) -> List<Double> = { value ->
        value?.split(',')?.mapNotNull { it.trim().toDoubleOrNull() }.orEmpty()
    }

    /**
     * Parses the ISO dates of a calendar, separated by commas or by `/` for a range; empty if
     * missing.
     */
    val dates: (String?) -> List<LocalDate> = { value ->
        value?.split(',', '/')?.mapNotNull { runCatching { LocalDate.parse(it.trim()) }.getOrNull() }.orEmpty()
    }
}
