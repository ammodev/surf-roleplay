package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonGroupTextElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ToggleElement
import dev.slne.surf.roleplay.api.client.common.screen.ToggleGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.CheckboxElement
import dev.slne.surf.roleplay.api.client.common.screen.ComboboxElement
import dev.slne.surf.roleplay.api.client.common.screen.NativeSelectElement
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoiceGroup
import dev.slne.surf.roleplay.api.client.common.screen.SelectElement
import dev.slne.surf.roleplay.api.client.common.screen.LabelElement
import dev.slne.surf.roleplay.api.client.common.screen.NumberInputElement
import dev.slne.surf.roleplay.api.client.common.screen.ProgressElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldTextElement
import dev.slne.surf.roleplay.api.client.common.screen.CalendarElement
import dev.slne.surf.roleplay.protocol.screen.CalendarValues
import dev.slne.surf.roleplay.protocol.screen.CalendarMode as NodeCalendarMode
import dev.slne.surf.roleplay.api.client.common.screen.RadioGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.SliderElement
import dev.slne.surf.roleplay.api.client.common.screen.SwitchElement
import dev.slne.surf.roleplay.protocol.screen.SliderValues
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupTextElement
import dev.slne.surf.roleplay.api.client.common.screen.InputOtpElement
import dev.slne.surf.roleplay.api.client.common.screen.OtpSlots
import dev.slne.surf.roleplay.api.client.common.screen.TextInputType
import dev.slne.surf.roleplay.api.client.common.screen.TextareaElement
import dev.slne.surf.roleplay.api.client.common.screen.AvatarElement
import dev.slne.surf.roleplay.api.client.common.screen.AvatarGroupCountElement
import dev.slne.surf.roleplay.api.client.common.screen.BadgeElement
import dev.slne.surf.roleplay.api.client.common.screen.ItemElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogElement
import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogElement
import dev.slne.surf.roleplay.api.client.common.screen.SheetElement
import dev.slne.surf.roleplay.api.client.common.screen.DrawerElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandEmptyElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandInputElement
import dev.slne.surf.roleplay.api.client.common.screen.CommandItemElement
import dev.slne.surf.roleplay.api.client.common.screen.DropdownMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSubElement
import dev.slne.surf.roleplay.api.client.common.screen.ContextMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.MenubarMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuItemElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuCheckboxItemElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuRadioGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuRadioItemElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuLabelElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSubTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.AccordionType
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuItemElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuLinkElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarProviderElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarGroupLabelElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarGroupActionElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuActionElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuBadgeElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarMenuSubButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.SidebarTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselContentElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselItemElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselPreviousElement
import dev.slne.surf.roleplay.api.client.common.screen.CarouselNextElement
import dev.slne.surf.roleplay.api.client.common.screen.ResizablePanelGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.ResizablePanelElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbLinkElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationLinkElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationPreviousElement
import dev.slne.surf.roleplay.api.client.common.screen.PaginationNextElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbPageElement
import dev.slne.surf.roleplay.api.client.common.screen.BreadcrumbEllipsisElement
import dev.slne.surf.roleplay.api.client.common.screen.TabsElement
import dev.slne.surf.roleplay.api.client.common.screen.TabsListElement
import dev.slne.surf.roleplay.api.client.common.screen.TabsTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.CollapsibleElement
import dev.slne.surf.roleplay.api.client.common.screen.AccordionElement
import dev.slne.surf.roleplay.api.client.common.screen.AccordionItemElement
import dev.slne.surf.roleplay.api.client.common.screen.AccordionTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.MenubarTriggerElement
import dev.slne.surf.roleplay.api.client.common.screen.HoverCardElement
import dev.slne.surf.roleplay.api.client.common.screen.PopoverElement
import dev.slne.surf.roleplay.api.client.common.screen.TooltipElement
import dev.slne.surf.roleplay.api.client.common.screen.KbdElement
import dev.slne.surf.roleplay.api.client.common.screen.TextElement
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
     * Creates the input rule of an overlay whose value is its open state, `true` or `false`.
     *
     * @param E the element class
     * @param open returns whether the overlay is open
     * @param withOpen returns a copy that is open or closed
     * @param onChange returns the handler of open-state changes, or `null` for none
     * @return the rule
     */
    fun <E : ScreenElement> openState(open: (E) -> Boolean, withOpen: (E, Boolean) -> E, onChange: (E) -> ChangeHandler?): InputRule<E> = InputRule(
        current = { open(it).toString() },
        violation = { _, v -> if (v == "true" || v == "false") null else "value must be true or false" },
        withValue = { e, v -> withOpen(e, v == "true") },
        onChange = onChange,
    )

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
     * The largest number of characters of a text input or textarea without its own limit.
     */
    const val MAX_TEXT_LENGTH: Int = 4096

    /**
     * The shape of an email address: a local part, an at sign, and a domain with a dot, without
     * whitespace.
     */
    private val EMAIL = Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")

    /**
     * Checks a text value against a length limit and required.
     *
     * @param value the text
     * @param maxLength the maximum number of characters, or `null` for [MAX_TEXT_LENGTH]
     * @param required whether an empty value is invalid
     * @return a description of the violated constraint, or `null` if the value is valid
     */
    fun textViolation(value: String, maxLength: Int?, required: Boolean): String? = when {
        value.length > (maxLength ?: MAX_TEXT_LENGTH) -> "value is too long"
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
     * Checks whether a value is an option of a list of groups.
     *
     * @param groups the option groups
     * @param value the value
     * @return whether an option has the value
     */
    fun isOption(groups: List<SelectChoiceGroup>, value: String): Boolean = groups.any { group -> group.options.any { it.value == value } }

    /**
     * Checks the options a selection would have chosen: known options only, each once, no newly
     * chosen disabled option, and at least one if required.
     *
     * @param groups the option groups
     * @param current the values selected now
     * @param chosen the values that would be selected
     * @param required whether an empty selection is invalid
     * @return a description of the violated constraint, or `null` if the selection is valid
     */
    private fun choiceViolation(groups: List<SelectChoiceGroup>, current: List<String>, chosen: List<String>, required: Boolean): String? {
        val options = groups.flatMap { it.options }.associateBy { it.value }
        if (chosen.any { it !in options }) return "value is not an option"
        if (chosen.toSet().size != chosen.size) return "value repeats an option"
        if (chosen.any { it !in current && options[it]?.enabled == false }) return "a disabled option was selected"
        if (required && chosen.isEmpty()) return "value is required"
        return null
    }

    /**
     * Returns the protocol mode of a calendar.
     *
     * @param calendar the calendar
     * @return the mode with the same name
     */
    private fun modeOf(calendar: CalendarElement): NodeCalendarMode = NodeCalendarMode.valueOf(calendar.mode.name)

    /**
     * Checks the dates a calendar would have selected: the mode's value form, every date within
     * the limits, no newly selected disabled date, and at least one date if required.
     *
     * @param calendar the calendar
     * @param value the value in the calendar's value form
     * @return a description of the violated constraint, or `null` if the value is valid
     */
    private fun calendarViolation(calendar: CalendarElement, value: String): String? {
        val dates = CalendarValues.parse(modeOf(calendar), value) ?: return "value is not a date selection"
        if (calendar.required && dates.isEmpty()) return "value is required"
        if (dates.any { calendar.min != null && it.isBefore(calendar.min) || calendar.max != null && it.isAfter(calendar.max) }) return "a date is out of range"
        if (dates.any { it in calendar.disabled && it !in calendar.selected }) return "a disabled date was selected"
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
     * Returns the triggers of every tab list of tabs, in order.
     *
     * @param tabs the tabs
     * @return the triggers
     */
    private fun tabTriggers(tabs: TabsElement): List<TabsTriggerElement> =
        tabs.children.filterIsInstance<TabsListElement>().flatMap { list -> list.children.filterIsInstance<TabsTriggerElement>() }

    /**
     * Returns the shares of the panels of a resizable group as the server holds them: the stored
     * shares, or else the default sizes of the panels, where panels without one share what the
     * others leave equally.
     *
     * @param group the group
     * @return the shares in percent, in the order of the panels
     */
    private fun resizableSizes(group: ResizablePanelGroupElement): List<Double> {
        val panels = group.children.filterIsInstance<ResizablePanelElement>()
        if (group.sizes.size == panels.size) return group.sizes
        val fixed = panels.filter { it.defaultSize > 0.0 }.sumOf { it.defaultSize }
        val open = panels.count { it.defaultSize <= 0.0 }
        val rest = if (open > 0) ((100.0 - fixed) / open).coerceAtLeast(0.0) else 0.0
        return panels.map { (if (it.defaultSize > 0.0) it.defaultSize else rest).coerceIn(it.minSize, it.maxSize) }
    }

    /**
     * Formats a share with at most one decimal.
     *
     * @param share the share in percent
     * @return the share as text
     */
    private fun formatShare(share: Double): String {
        val rounded = kotlin.math.round(share * 10) / 10
        return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
    }

    /**
     * Checks the shares a resizable group would take: one number per panel, each inside its
     * panel's limits, together 100 percent.
     *
     * @param group the group
     * @param value the shares in percent, comma separated
     * @return a description of the violated constraint, or `null` if the shares are valid
     */
    private fun resizableViolation(group: ResizablePanelGroupElement, value: String): String? {
        val panels = group.children.filterIsInstance<ResizablePanelElement>()
        val shares = value.split(',').map { it.trim().toDoubleOrNull() ?: return "value is not a list of numbers" }
        if (shares.size != panels.size) return "value does not name every panel"
        if (shares.indices.any { shares[it] < panels[it].minSize - SHARE_TOLERANCE || shares[it] > panels[it].maxSize + SHARE_TOLERANCE }) return "a share is outside its panel's limits"
        if (kotlin.math.abs(shares.sum() - 100.0) > SUM_TOLERANCE) return "the shares do not sum to 100"
        return null
    }

    /**
     * How far a panel share may lie outside its limits through rounding, in percent.
     */
    private const val SHARE_TOLERANCE: Double = 0.05

    /**
     * How far the shares of a resizable group may miss 100 percent through rounding.
     */
    private const val SUM_TOLERANCE: Double = 0.5

    /**
     * Returns the items of an accordion, in order.
     *
     * @param accordion the accordion
     * @return the items
     */
    private fun accordionItems(accordion: AccordionElement): List<AccordionItemElement> = accordion.children.filterIsInstance<AccordionItemElement>()

    /**
     * Checks the items an accordion would have open.
     *
     * @param accordion the accordion
     * @param open the values of the items that would be open
     * @return a description of the violated constraint, or `null` if the choice is valid
     */
    private fun accordionViolation(accordion: AccordionElement, open: List<String>): String? {
        val items = accordionItems(accordion).associateBy { it.value }
        if (open.any { it !in items }) return "value is not an item"
        if (open.toSet().size != open.size) return "value repeats an item"
        if (accordion.type == AccordionType.SINGLE && open.size > 1) return "only one item can be open"
        val changed = (open.toSet() - accordion.value.toSet()) + (accordion.value.toSet() - open.toSet())
        if (changed.any { items[it]?.enabled == false }) return "a disabled item changed"
        return null
    }

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
        register(FieldTextElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(TextElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(KbdElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(BadgeElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(AvatarElement::class, ElementRule(withText = { e, t -> e.copy(fallback = t) }))
        register(ItemElement::class, ElementRule(action = { item -> item.onClick?.let { ActionRule(it, submitsInput = false) } }))
        register(PopoverElement::class, ElementRule(input = openState({ it.open }, { e, open -> e.copy(open = open) }, { it.onChange })))
        register(HoverCardElement::class, ElementRule(input = openState({ it.open }, { e, open -> e.copy(open = open) }, { it.onChange })))
        register(TooltipElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(DropdownMenuElement::class, ElementRule(input = openState({ it.open }, { e, open -> e.copy(open = open) }, { it.onChange })))
        register(MenuSubElement::class, ElementRule(input = openState({ it.open }, { e, open -> e.copy(open = open) }, { it.onChange })))
        register(ContextMenuElement::class, ElementRule(input = openState({ it.open }, { e, open -> e.copy(open = open) }, { it.onChange })))
        register(MenubarMenuElement::class, ElementRule(input = openState({ it.open }, { e, open -> e.copy(open = open) }, { it.onChange })))
        register(
            MenuItemElement::class,
            ElementRule(
                withText = { e, t -> e.copy(text = t) },
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                action = { ActionRule(it.onClick, submitsInput = false) },
            ),
        )
        register(
            MenuCheckboxItemElement::class,
            ElementRule(
                withText = { e, t -> e.copy(text = t) },
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                action = { ActionRule(it.onToggle, submitsInput = false) },
                input = InputRule(
                    current = { it.checked.toString() },
                    violation = { _, v -> if (v == "true" || v == "false") null else "value must be true or false" },
                    withValue = { e, v -> e.copy(checked = v == "true") },
                    onChange = { null },
                ),
            ),
        )
        register(
            MenuRadioGroupElement::class,
            ElementRule(
                action = { ActionRule(it.onSelect, submitsInput = false) },
                input = InputRule(
                    current = { it.value },
                    violation = { group, v ->
                        val allowed = group.children.filterIsInstance<MenuRadioItemElement>().filter { it.enabled }.map { it.value }
                        if (v.isEmpty() || v in allowed) null else "value is not an enabled item of the group"
                    },
                    withValue = { e, v -> e.copy(value = v) },
                    onChange = { null },
                ),
            ),
        )
        register(MenuRadioItemElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(MenuLabelElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(
            MenuSubTriggerElement::class,
            ElementRule(withText = { e, t -> e.copy(text = t) }, enabled = { it.enabled }, withEnabled = { e, on -> e.copy(enabled = on) }),
        )
        register(MenubarTriggerElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(
            CommandItemElement::class,
            ElementRule(
                withText = { e, t -> e.copy(text = t) },
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                action = { ActionRule(it.onClick, submitsInput = false) },
            ),
        )
        register(CommandEmptyElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(DialogElement::class, ElementRule(input = openState({ it.open }, { e, open -> e.copy(open = open) }, { it.onChange })))
        register(CollapsibleElement::class, ElementRule(input = openState({ it.open }, { e, open -> e.copy(open = open) }, { it.onChange })))
        register(
            AccordionElement::class,
            ElementRule(
                input = InputRule(
                    current = { e -> accordionItems(e).filter { it.value in e.value }.joinToString(",") { it.value } },
                    violation = { e, v -> accordionViolation(e, splitList(v)) },
                    withValue = { e, v -> e.copy(value = splitList(v)) },
                    onChange = { it.onChange },
                ),
            ),
        )
        register(AccordionItemElement::class, ElementRule(enabled = { it.enabled }, withEnabled = { e, on -> e.copy(enabled = on) }))
        register(AccordionTriggerElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(NavigationMenuItemElement::class, ElementRule(input = openState({ it.open }, { e, open -> e.copy(open = open) }, { it.onChange })))
        register(
            NavigationMenuTriggerElement::class,
            ElementRule(withText = { e, t -> e.copy(text = t) }, enabled = { it.enabled }, withEnabled = { e, on -> e.copy(enabled = on) }),
        )
        register(
            NavigationMenuLinkElement::class,
            ElementRule(enabled = { it.enabled }, withEnabled = { e, on -> e.copy(enabled = on) }, action = { ActionRule(it.onClick, submitsInput = false) }),
        )
        register(SidebarProviderElement::class, ElementRule(input = openState({ it.open }, { e, open -> e.copy(open = open) }, { it.onChange })))
        register(SidebarGroupLabelElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(SidebarMenuBadgeElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(SidebarGroupActionElement::class, ElementRule(enabled = { it.enabled }, withEnabled = { e, on -> e.copy(enabled = on) }, action = { ActionRule(it.onClick, submitsInput = false) }))
        register(SidebarMenuActionElement::class, ElementRule(enabled = { it.enabled }, withEnabled = { e, on -> e.copy(enabled = on) }, action = { ActionRule(it.onClick, submitsInput = false) }))
        register(SidebarMenuButtonElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }, enabled = { it.enabled }, withEnabled = { e, on -> e.copy(enabled = on) }, action = { ActionRule(it.onClick, submitsInput = false) }))
        register(SidebarMenuSubButtonElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }, enabled = { it.enabled }, withEnabled = { e, on -> e.copy(enabled = on) }, action = { ActionRule(it.onClick, submitsInput = false) }))
        register(SidebarTriggerElement::class, ElementRule(enabled = { it.enabled }, withEnabled = { e, on -> e.copy(enabled = on) }))
        register(
            CarouselElement::class,
            ElementRule(
                input = InputRule(
                    current = { it.index.toString() },
                    violation = { e, v ->
                        val slides = e.children.filterIsInstance<CarouselContentElement>().sumOf { content -> content.children.count { it is CarouselItemElement } }
                        val index = v.toIntOrNull()
                        if (index == null || index < 0 || index >= slides) "value is not a slide" else null
                    },
                    withValue = { e, v -> e.copy(index = v.toInt()) },
                    onChange = { it.onChange },
                ),
            ),
        )
        register(CarouselPreviousElement::class, ElementRule(enabled = { it.enabled }, withEnabled = { e, on -> e.copy(enabled = on) }))
        register(CarouselNextElement::class, ElementRule(enabled = { it.enabled }, withEnabled = { e, on -> e.copy(enabled = on) }))
        register(
            ResizablePanelGroupElement::class,
            ElementRule(
                input = InputRule(
                    current = { e -> resizableSizes(e).joinToString(",") { formatShare(it) } },
                    violation = { e, v -> resizableViolation(e, v) },
                    withValue = { e, v -> e.copy(sizes = v.split(',').map { it.trim().toDouble() }) },
                    onChange = { it.onChange },
                ),
            ),
        )
        register(
            BreadcrumbLinkElement::class,
            ElementRule(
                withText = { e, t -> e.copy(text = t) },
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                action = { ActionRule(it.onClick, submitsInput = false) },
            ),
        )
        register(
            PaginationLinkElement::class,
            ElementRule(
                withText = { e, t -> e.copy(text = t) },
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                action = { ActionRule(it.onClick, submitsInput = false) },
            ),
        )
        register(
            PaginationPreviousElement::class,
            ElementRule(
                withText = { e, t -> e.copy(text = t) },
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                action = { ActionRule(it.onClick, submitsInput = false) },
            ),
        )
        register(
            PaginationNextElement::class,
            ElementRule(
                withText = { e, t -> e.copy(text = t) },
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                action = { ActionRule(it.onClick, submitsInput = false) },
            ),
        )
        register(BreadcrumbPageElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(BreadcrumbEllipsisElement::class, ElementRule(action = { ActionRule(null, submitsInput = false) }))
        register(
            TabsElement::class,
            ElementRule(
                input = InputRule(
                    current = { it.value },
                    violation = { e, v ->
                        val trigger = tabTriggers(e).firstOrNull { it.value == v }
                        when {
                            trigger == null -> "value is not a tab"
                            !trigger.enabled && v != e.value -> "the tab is disabled"
                            else -> null
                        }
                    },
                    withValue = { e, v -> e.copy(value = v) },
                    onChange = { it.onChange },
                ),
            ),
        )
        register(
            TabsTriggerElement::class,
            ElementRule(withText = { e, t -> e.copy(text = t) }, enabled = { it.enabled }, withEnabled = { e, on -> e.copy(enabled = on) }),
        )
        register(AlertDialogElement::class, ElementRule(input = openState({ it.open }, { e, open -> e.copy(open = open) }, { it.onChange })))
        register(SheetElement::class, ElementRule(input = openState({ it.open }, { e, open -> e.copy(open = open) }, { it.onChange })))
        register(DrawerElement::class, ElementRule(input = openState({ it.open }, { e, open -> e.copy(open = open) }, { it.onChange })))
        register(CommandInputElement::class, ElementRule(withText = { e, t -> e.copy(placeholder = t) }))
        register(AvatarGroupCountElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
        register(FieldSeparatorElement::class, ElementRule(withText = { e, t -> e.copy(text = t) }))
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
            CalendarElement::class,
            ElementRule(
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                input = InputRule(
                    current = { e -> CalendarValues.format(modeOf(e), e.selected) },
                    violation = { e, v -> calendarViolation(e, v) },
                    withValue = { e, v -> CalendarValues.parse(modeOf(e), v)?.let { runCatching { e.copy(selected = it) }.getOrNull() } },
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
            SelectElement::class,
            ElementRule(
                withText = { e, t -> e.copy(placeholder = t) },
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                input = InputRule(
                    current = { it.selected ?: "" },
                    violation = { e, v -> choiceViolation(e.groups, listOfNotNull(e.selected), listOfNotNull(v.takeIf { it.isNotEmpty() }), e.required) },
                    withValue = { e, v -> if (v.isEmpty()) e.copy(selected = null) else e.copy(selected = v.takeIf { isOption(e.groups, it) }) },
                    onChange = { it.onChange },
                ),
            ),
        )
        register(
            NativeSelectElement::class,
            ElementRule(
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                input = InputRule(
                    current = { it.selected ?: "" },
                    violation = { e, v -> choiceViolation(e.groups, listOfNotNull(e.selected), listOfNotNull(v.takeIf { it.isNotEmpty() }), e.required) },
                    withValue = { e, v -> if (v.isEmpty()) e.copy(selected = null) else e.copy(selected = v.takeIf { isOption(e.groups, it) }) },
                    onChange = { it.onChange },
                ),
            ),
        )
        register(
            ComboboxElement::class,
            ElementRule(
                withText = { e, t -> e.copy(placeholder = t) },
                enabled = { it.enabled },
                withEnabled = { e, on -> e.copy(enabled = on) },
                input = InputRule(
                    current = { it.selected.joinToString(",") },
                    violation = { e, v ->
                        val chosen = splitList(v)
                        if (!e.multiple && chosen.size > 1) "only one option can be selected" else choiceViolation(e.groups, e.selected, chosen, e.required)
                    },
                    withValue = { e, v ->
                        val chosen = splitList(v)
                        if (chosen.all { isOption(e.groups, it) } && chosen.toSet().size == chosen.size && (e.multiple || chosen.size <= 1)) e.copy(selected = chosen) else null
                    },
                    onChange = { it.onChange },
                ),
            ),
        )
    }
}
