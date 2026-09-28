package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupTextElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ToggleElement
import dev.slne.surf.roleplay.api.client.common.screen.ToggleGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.CheckboxElement
import dev.slne.surf.roleplay.api.client.common.screen.DropdownElement
import dev.slne.surf.roleplay.api.client.common.screen.LabelElement
import dev.slne.surf.roleplay.api.client.common.screen.NumberInputElement
import dev.slne.surf.roleplay.api.client.common.screen.ProgressElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import dev.slne.surf.roleplay.api.client.common.screen.RadioGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.SliderElement
import dev.slne.surf.roleplay.api.client.common.screen.SwitchElement
import dev.slne.surf.roleplay.protocol.screen.SliderValues
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupTextElement
import dev.slne.surf.roleplay.api.client.common.screen.InputOtpElement
import dev.slne.surf.roleplay.api.client.common.screen.OtpSlots
import dev.slne.surf.roleplay.api.client.common.screen.TextInputType
import dev.slne.surf.roleplay.api.client.common.screen.TextareaElement
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
 * @property action returns how the element triggers widget actions, or `null` if it does not
 */
class ElementRule<E : ScreenElement>(
    val withText: (E, Component) -> E? = { _, _ -> null },
    val enabled: (E) -> Boolean? = { null },
    val withEnabled: (E, Boolean) -> E? = { _, _ -> null },
    val input: InputRule<E>? = null,
    val action: (E) -> ActionRule? = { null },
)

/**
 * How an element triggers widget actions.
 *
 * @property handler the handler run on a validated action, or `null` for none
 * @property submitsInput whether the action requires every input of the screen to be valid
 */
class ActionRule(val handler: ButtonHandler?, val submitsInput: Boolean)

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

    /**
     * The shape of an email address: a local part, an at sign, and a domain with a dot, without
     * whitespace.
     */
    private val EMAIL = Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")

    /**
     * Checks a text value against a length limit and required.
     *
     * @param value the text
     * @param maxLength the maximum number of characters, or `null` for no limit
     * @param required whether an empty value is invalid
     * @return a description of the violated constraint, or `null` if the value is valid
     */
    fun textViolation(value: String, maxLength: Int?, required: Boolean): String? = when {
        maxLength != null && value.length > maxLength -> "value is too long"
        required && value.isEmpty() -> "value is required"
        else -> null
    }

    /**
     * Checks a one-time code: empty unless required, or complete and of the code's pattern.
     *
     * @param otp the one-time code input
     * @param value the code
     * @return a description of the violated constraint, or `null` if the value is valid
     */
    private fun otpViolation(otp: InputOtpElement, value: String): String? = when {
        value.isEmpty() -> if (otp.required) "value is required" else null
        value.length != otp.length -> "value is not a complete code"
        value.any { !OtpSlots.accepts(otp.pattern, it) } -> "value does not match the code pattern"
        else -> null
    }

    /**
     * Checks the option a radio group would have selected: an option of the group, not newly
     * selected if disabled, and present if required.
     *
     * @param group the radio group
     * @param value the value of the selected option, or empty
     * @return a description of the violated constraint, or `null` if the value is valid
     */
    private fun radioViolation(group: RadioGroupElement, value: String): String? {
        if (value.isEmpty()) return if (group.required) "value is required" else null
        val option = group.options.firstOrNull { it.value == value } ?: return "value is not an option"
        if (!option.enabled && value != group.selected) return "a disabled option was selected"
        return null
    }

    /**
     * Checks the thumb values a slider would have: as many as it has thumbs, ascending, and each
     * within the range and on a step.
     *
     * @param slider the slider
     * @param value the thumb values joined by commas
     * @return a description of the violated constraint, or `null` if the value is valid
     */
    private fun sliderViolation(slider: SliderElement, value: String): String? {
        val values = SliderValues.parse(value) ?: return "value is not a number"
        if (values.size != slider.values.size) return "value has the wrong number of thumbs"
        if (values.zipWithNext().any { (a, b) -> a > b }) return "value is not ascending"
        if (values.any { !SliderValues.isSelectable(it, slider.min, slider.max, slider.step) }) return "value is not selectable"
        return null
    }

    /**
     * Splits a comma-separated list value into its non-empty parts.
     *
     * @param value the list value
     * @return the parts, in order
     */
    fun splitList(value: String): List<String> = value.split(',').map { it.trim() }.filter { it.isNotEmpty() }

    /**
     * Checks the items a toggle group would have switched on.
     *
     * @param group the toggle group
     * @param chosen the values of the items that would be on
     * @return a description of the violated constraint, or `null` if the choice is valid
     */
    private fun toggleGroupViolation(group: ToggleGroupElement, chosen: List<String>): String? {
        val items = group.items.associateBy { it.value }
        if (chosen.any { it !in items }) return "value is not an item"
        if (chosen.toSet().size != chosen.size) return "value repeats an item"
        if (!group.multiple && chosen.size > 1) return "only one item can be on"
        if (group.required && chosen.isEmpty()) return "value is required"
        val changed = (chosen.toSet() - group.selected.toSet()) + (group.selected.toSet() - chosen.toSet())
        if (changed.any { items[it]?.enabled == false }) return "a disabled item changed"
        return null
    }

    init {
        register(LabelElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(ProgressElement::class, ElementRule(withText = { e, t -> e.copy(label = t) }))
        register(
            ButtonElement::class,
            ElementRule(
                withText = { e, t -> e.copy(text = t) },
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                action = { ActionRule(it.onClick, it.submitsInput) },
            ),
        )
        register(ButtonGroupTextElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(
            ToggleElement::class,
            ElementRule(
                withText = { e, t -> e.copy(text = t) },
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                action = { ActionRule(it.onToggle, submitsInput = false) },
                input = InputRule(
                    current = { it.pressed.toString() },
                    violation = { _, v -> if (v == "true" || v == "false") null else "value must be true or false" },
                    withValue = { e, v -> e.copy(pressed = v == "true") },
                    onChange = { null },
                ),
            ),
        )
        register(
            ToggleGroupElement::class,
            ElementRule(
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                input = InputRule(
                    current = { e -> e.items.filter { it.value in e.selected }.joinToString(",") { it.value } },
                    violation = { e, v -> toggleGroupViolation(e, splitList(v)) },
                    withValue = { e, v -> e.copy(selected = splitList(v)) },
                    onChange = { it.onChange },
                ),
            ),
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
                        textViolation(v, e.maxLength, e.required)
                            ?: when {
                                v.any { it == '\n' || it == '\r' } -> "value contains a line break"
                                e.type == TextInputType.EMAIL && v.isNotEmpty() && !EMAIL.matches(v) -> "value is not an email address"
                                else -> null
                            }
                    },
                    withValue = { e, v -> e.copy(value = v) },
                    onChange = { it.onChange },
                ),
            ),
        )
        register(
            TextareaElement::class,
            ElementRule(
                withText = { e, t -> e.copy(placeholder = t) },
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                input = InputRule(
                    current = { it.value },
                    violation = { e, v -> textViolation(v, e.maxLength, e.required) },
                    withValue = { e, v -> e.copy(value = v) },
                    onChange = { it.onChange },
                ),
            ),
        )
        register(InputGroupTextElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(
            InputOtpElement::class,
            ElementRule(
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                input = InputRule(
                    current = { it.value },
                    violation = { e, v -> otpViolation(e, v) },
                    withValue = { e, v -> if (otpViolation(e.copy(required = false), v) == null) e.copy(value = v) else null },
                    onChange = { it.onChange },
                ),
            ),
        )
        register(
            SwitchElement::class,
            ElementRule(
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
            RadioGroupElement::class,
            ElementRule(
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                input = InputRule(
                    current = { it.selected ?: "" },
                    violation = { e, v -> radioViolation(e, v) },
                    withValue = { e, v -> if (v.isEmpty()) e.copy(selected = null) else e.copy(selected = v.takeIf { c -> e.options.any { it.value == c } }) },
                    onChange = { it.onChange },
                ),
            ),
        )
        register(
            SliderElement::class,
            ElementRule(
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                input = InputRule(
                    current = { e -> e.values.joinToString(",") { SliderValues.format(it) } },
                    violation = { e, v -> sliderViolation(e, v) },
                    withValue = { e, v -> if (sliderViolation(e, v) == null) e.copy(values = SliderValues.parse(v)!!) else null },
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
