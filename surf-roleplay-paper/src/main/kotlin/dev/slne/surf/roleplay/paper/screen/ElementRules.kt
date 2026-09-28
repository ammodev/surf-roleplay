package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.CheckboxElement
import dev.slne.surf.roleplay.api.client.common.screen.DropdownElement
import dev.slne.surf.roleplay.api.client.common.screen.LabelElement
import dev.slne.surf.roleplay.api.client.common.screen.NumberInputElement
import dev.slne.surf.roleplay.api.client.common.screen.ProgressElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import net.kyori.adventure.text.Component
import kotlin.reflect.KClass

/**
 * What the server may do with one kind of screen element.
 *
 * Every element kind may take a text and may be enabled or disabled. Inputs additionally have a
 * value in string form, constraints on it, and an optional change handler.
 *
 * @param E the element class
 * @property withText returns a copy of the element with a new text, or `null` if it has no text
 * @property enabled returns whether the element can be used, or `null` if it cannot be disabled
 * @property withEnabled returns an enabled or disabled copy, or `null` if it cannot be disabled
 * @property input the value rules if the element is an input, or `null`
 */
class ElementRule<E : ScreenElement>(
    val withText: (E, Component) -> E? = { _, _ -> null },
    val enabled: (E) -> Boolean? = { null },
    val withEnabled: (E, Boolean) -> E? = { _, _ -> null },
    val input: InputRule<E>? = null,
)

/**
 * The value rules of one kind of input element.
 *
 * @param E the element class
 * @property current returns the input's current value in its string form
 * @property violation checks a submitted value and returns a description of the violated
 *           constraint, or `null` if the value is valid
 * @property withValue returns a copy holding a new value, or `null` if the value does not fit
 * @property onChange returns the handler run on every validated change, or `null` if the input
 *           does not report changes
 */
class InputRule<E : ScreenElement>(
    val current: (E) -> String,
    val violation: (E, String) -> String?,
    val withValue: (E, String) -> E?,
    val onChange: (E) -> ChangeHandler?,
)

/**
 * The element rules of every kind of screen element, keyed by element class.
 */
object ElementRules {

    /**
     * The rules keyed by element class.
     */
    private val rules = mutableMapOf<KClass<out ScreenElement>, ElementRule<*>>()

    /**
     * Registers the rule of an element class, replacing any earlier rule.
     *
     * @param E the element class
     * @param type the element class
     * @param rule the rule
     */
    fun <E : ScreenElement> register(type: KClass<E>, rule: ElementRule<E>) {
        rules[type] = rule
    }

    /**
     * Returns the rule of an element.
     *
     * @param element the element
     * @return the rule, or `null` if the element kind has none
     */
    @Suppress("UNCHECKED_CAST")
    fun rule(element: ScreenElement): ElementRule<ScreenElement>? = rules[element::class] as ElementRule<ScreenElement>?

    /**
     * Returns the input rule of an element.
     *
     * @param element the element
     * @return the input rule, or `null` if the element is not an input
     */
    fun input(element: ScreenElement): InputRule<ScreenElement>? = rule(element)?.input

    /**
     * Returns whether an element can be used: its enabled state, or `true` for elements that
     * cannot be disabled.
     *
     * @param element the element
     * @return whether it is enabled
     */
    fun isEnabled(element: ScreenElement): Boolean = rule(element)?.enabled?.invoke(element) ?: true

    /**
     * Checks a value against the constraints of a number range.
     *
     * @param value the value in decimal form, or empty
     * @param min the smallest allowed number, or `null`
     * @param max the largest allowed number, or `null`
     * @param required whether an empty value is invalid
     * @return a description of the violated constraint, or `null` if the value is valid
     */
    fun numberViolation(value: String, min: Long?, max: Long?, required: Boolean): String? {
        if (value.isEmpty()) return if (required) "value is required" else null
        val number = value.toLongOrNull() ?: return "value is not a number"
        if (min != null && number < min) return "value is out of range"
        if (max != null && number > max) return "value is out of range"
        return null
    }

    init {
        register(LabelElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(ProgressElement::class, ElementRule(withText = { e, t -> e.copy(label = t) }))
        register(
            ButtonElement::class,
            ElementRule(withText = { e, t -> e.copy(text = t) }, enabled = { it.enabled }, withEnabled = { e, on -> e.copy(enabled = on) }),
        )
        register(
            TextInputElement::class,
            ElementRule(
                withText = { e, t -> e.copy(placeholder = t) },
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                input = InputRule(
                    current = { it.value },
                    violation = { e, v ->
                        when {
                            e.maxLength != null && v.length > e.maxLength!! -> "value is too long"
                            e.required && v.isEmpty() -> "value is required"
                            else -> null
                        }
                    },
                    withValue = { e, v -> e.copy(value = v) },
                    onChange = { it.onChange },
                ),
            ),
        )
        register(
            NumberInputElement::class,
            ElementRule(
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                input = InputRule(
                    current = { it.value?.toString() ?: "" },
                    violation = { e, v -> numberViolation(v, e.min, e.max, e.required) },
                    withValue = { e, v -> if (v.isEmpty()) e.copy(value = null) else v.toLongOrNull()?.let { e.copy(value = it) } },
                    onChange = { it.onChange },
                ),
            ),
        )
        register(
            CheckboxElement::class,
            ElementRule(
                withText = { e, t -> e.copy(label = t) },
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                input = InputRule(
                    current = { it.checked.toString() },
                    violation = { _, v -> if (v == "true" || v == "false") null else "value must be true or false" },
                    withValue = { e, v -> e.copy(checked = v == "true") },
                    onChange = { it.onChange },
                ),
            ),
        )
        register(
            DropdownElement::class,
            ElementRule(
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                input = InputRule(
                    current = { it.selected ?: "" },
                    violation = { e, v ->
                        when {
                            v.isEmpty() -> if (e.required) "value is required" else null
                            e.options.none { it.value == v } -> "value is not an option"
                            else -> null
                        }
                    },
                    withValue = { e, v -> e.copy(selected = v.takeIf { candidate -> e.options.any { it.value == candidate } }) },
                    onChange = { it.onChange },
                ),
            ),
        )
    }
}
