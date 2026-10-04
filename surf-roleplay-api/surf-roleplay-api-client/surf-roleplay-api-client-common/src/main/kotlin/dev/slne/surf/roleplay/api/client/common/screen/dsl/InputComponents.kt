package dev.slne.surf.roleplay.api.client.common.screen.dsl

import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.ButtonHandler
import dev.slne.surf.roleplay.api.client.common.screen.ButtonSize
import dev.slne.surf.roleplay.api.client.common.screen.ButtonVariant
import dev.slne.surf.roleplay.api.client.common.screen.CalendarElement
import dev.slne.surf.roleplay.api.client.common.screen.CalendarMode
import dev.slne.surf.roleplay.api.client.common.screen.CaptionLayout
import dev.slne.surf.roleplay.api.client.common.screen.ChangeHandler
import dev.slne.surf.roleplay.api.client.common.screen.CheckboxElement
import dev.slne.surf.roleplay.api.client.common.screen.ComboboxElement
import dev.slne.surf.roleplay.api.client.common.screen.ElementSize
import dev.slne.surf.roleplay.api.client.common.screen.FieldContentElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldSeparatorElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldSetElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldTextElement
import dev.slne.surf.roleplay.api.client.common.screen.FieldTextKind
import dev.slne.surf.roleplay.api.client.common.screen.FormElement
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupAddonElement
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupAlign
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.InputGroupTextElement
import dev.slne.surf.roleplay.api.client.common.screen.InputOtpElement
import dev.slne.surf.roleplay.api.client.common.screen.LabelElement
import dev.slne.surf.roleplay.api.client.common.screen.NativeSelectElement
import dev.slne.surf.roleplay.api.client.common.screen.NumberInputElement
import dev.slne.surf.roleplay.api.client.common.screen.Orientation
import dev.slne.surf.roleplay.api.client.common.screen.OtpPattern
import dev.slne.surf.roleplay.api.client.common.screen.RadioChoice
import dev.slne.surf.roleplay.api.client.common.screen.RadioGroupElement
import dev.slne.surf.roleplay.api.client.common.screen.SearchHandler
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoiceGroup
import dev.slne.surf.roleplay.api.client.common.screen.SelectElement
import dev.slne.surf.roleplay.api.client.common.screen.SelectSize
import dev.slne.surf.roleplay.api.client.common.screen.SliderElement
import dev.slne.surf.roleplay.api.client.common.screen.SwitchElement
import dev.slne.surf.roleplay.api.client.common.screen.SwitchSize
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import dev.slne.surf.roleplay.api.client.common.screen.TextInputType
import dev.slne.surf.roleplay.api.client.common.screen.TextareaElement
import net.kyori.adventure.text.Component
import java.time.LocalDate
import java.time.YearMonth

/**
 * Adds a single-line text input.
 *
 * @param value the initial text
 * @param placeholder the hint shown while the input is empty
 * @param maxLength the maximum number of characters, or `null` for no limit
 * @param required whether an empty value is invalid
 * @param enabled whether the player can edit the input
 * @param width how wide the input is laid out
 * @param height how tall the input is laid out
 * @param icon the Lucide name of an icon drawn at the start of the field, or `null` for none
 * @param type the kind of text the input holds
 * @param id the id of the input, or `null` for a generated one
 * @param onChange the handler run on every validated change, or `null` to send the value only
 *        with the next action
 * @return the reference to the input's text
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Input(
    value: String = "",
    placeholder: Component = Component.empty(),
    maxLength: Int? = null,
    required: Boolean = false,
    enabled: Boolean = true,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    icon: String? = null,
    type: TextInputType = TextInputType.TEXT,
    id: String? = null,
    onChange: ChangeHandler? = null,
): InputRef<String> {
    val elementId = nextId(id)
    add(TextInputElement(elementId, value, placeholder, maxLength, required, enabled, width, height, icon, bindChange(elementId, onChange), type))
    return InputRef(elementId, InputParsers.text)
}

/**
 * Adds an input for a whole number.
 *
 * @param value the initial number, or `null` for an empty input
 * @param min the smallest allowed number, or `null` for no lower bound
 * @param max the largest allowed number, or `null` for no upper bound
 * @param required whether an empty value is invalid
 * @param enabled whether the player can edit the input
 * @param width how wide the input is laid out
 * @param height how tall the input is laid out
 * @param id the id of the input, or `null` for a generated one
 * @param onChange the handler run on every validated change, or `null`
 * @return the reference to the input's number, which is `null` while the input is empty
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.NumberInput(
    value: Long? = null,
    min: Long? = null,
    max: Long? = null,
    required: Boolean = false,
    enabled: Boolean = true,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    onChange: ChangeHandler? = null,
): InputRef<Long?> {
    val elementId = nextId(id)
    add(NumberInputElement(elementId, value, min, max, required, enabled, width, height, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.number)
}

/**
 * Adds a multi-line text input whose text wraps at its width.
 *
 * @param value the initial text, with `\n` for line breaks
 * @param placeholder the hint shown while the textarea is empty
 * @param rows the number of visible lines when the textarea fits its content
 * @param maxLength the maximum number of characters, or `null` for no limit
 * @param required whether an empty value is invalid
 * @param enabled whether the player can edit the textarea
 * @param width how wide the textarea is laid out
 * @param height how tall the textarea is laid out
 * @param id the id of the textarea, or `null` for a generated one
 * @param onChange the handler run on every validated change, or `null`
 * @return the reference to the textarea's text
 * @throws IllegalArgumentException if [id] starts with `_`, [rows] is below one or [maxLength] is
 *         not positive
 */
fun ComponentScope.Textarea(
    value: String = "",
    placeholder: Component = Component.empty(),
    rows: Int = 3,
    maxLength: Int? = null,
    required: Boolean = false,
    enabled: Boolean = true,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    onChange: ChangeHandler? = null,
): InputRef<String> {
    val elementId = nextId(id)
    add(TextareaElement(elementId, value, placeholder, rows, maxLength, required, enabled, bindChange(elementId, onChange), width, height))
    return InputRef(elementId, InputParsers.text)
}

/**
 * Adds an input for a one-time code, entered one character per slot.
 *
 * @param length the number of slots
 * @param groups the number of slots in each group; empty for a single group
 * @param pattern the characters the slots accept
 * @param value the initially entered characters
 * @param required whether an empty value is invalid
 * @param enabled whether the player can enter a code
 * @param id the id of the input, or `null` for a generated one
 * @param onChange the handler run on every validated change, or `null`
 * @return the reference to the entered code
 * @throws IllegalArgumentException if [id] starts with `_`, the length is out of range, the groups
 *         do not add up to it, or the value does not fit
 */
fun ComponentScope.InputOtp(
    length: Int = 6,
    groups: List<Int> = emptyList(),
    pattern: OtpPattern = OtpPattern.DIGITS,
    value: String = "",
    required: Boolean = false,
    enabled: Boolean = true,
    id: String? = null,
    onChange: ChangeHandler? = null,
): InputRef<String> {
    val elementId = nextId(id)
    add(InputOtpElement(elementId, length, groups, pattern, value, required, enabled, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.text)
}

/**
 * Adds an input group that joins one text input or textarea with addons into one bordered
 * control.
 *
 * @param width how wide the group is laid out
 * @param height how tall the group is laid out
 * @param id the id of the group, or `null` for a generated one
 * @param children the builder of exactly one text input or textarea and its addons
 * @return the group
 * @throws IllegalArgumentException if [id] starts with `_`, or the children are not one control
 *         and addons
 */
fun ComponentScope.InputGroup(
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): InputGroupElement {
    val elementId = nextId(id)
    return add(InputGroupElement(elementId, this.children(children), width, height))
}

/**
 * Adds an addon to an input group: a row of texts, icons or buttons at one side of the control.
 *
 * @param align where the addon sits
 * @param id the id of the addon, or `null` for a generated one
 * @param children the builder of the addon's texts, icons and buttons
 * @return the addon
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.InputGroupAddon(
    align: InputGroupAlign = InputGroupAlign.INLINE_START,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): InputGroupAddonElement {
    val elementId = nextId(id)
    return add(InputGroupAddonElement(elementId, align, this.children(children)))
}

/**
 * Adds a muted text to an input group addon.
 *
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param id the id of the text, or `null` for a generated one
 * @return the text
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.InputGroupText(text: Component, icon: String? = null, id: String? = null): InputGroupTextElement =
    add(InputGroupTextElement(nextId(id), text, icon))

/**
 * Adds a plain muted text to an input group addon.
 *
 * @param text the text
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param id the id of the text, or `null` for a generated one
 * @return the text
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.InputGroupText(text: String, icon: String? = null, id: String? = null): InputGroupTextElement =
    InputGroupText(Component.text(text), icon, id)

/**
 * Adds a button to an input group addon: a ghost button of the smallest size unless set
 * otherwise, which does not submit input unless asked to.
 *
 * @param text the caption
 * @param icon the Lucide name of the icon, or `null` for none
 * @param variant the look of the button
 * @param size the size of the button
 * @param enabled whether the button can be clicked
 * @param submitsInput whether a click requires every input of the screen to be valid
 * @param id the id of the button, or `null` for a generated one
 * @param onClick the handler run on the server when the button is clicked, or `null`
 * @return the button
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.InputGroupButton(
    text: Component = Component.empty(),
    icon: String? = null,
    variant: ButtonVariant = ButtonVariant.GHOST,
    size: ButtonSize = ButtonSize.XS,
    enabled: Boolean = true,
    submitsInput: Boolean = false,
    id: String? = null,
    onClick: ButtonHandler? = null,
): ButtonElement {
    val elementId = nextId(id)
    return add(
        ButtonElement(
            elementId, text, enabled, bindButton(elementId, onClick),
            submitsInput = submitsInput, icon = icon, variant = variant, size = size,
        ),
    )
}

/**
 * Adds a button with a plain caption to an input group addon: a ghost button of the smallest
 * size unless set otherwise, which does not submit input unless asked to.
 *
 * @param text the caption
 * @param icon the Lucide name of the icon, or `null` for none
 * @param variant the look of the button
 * @param size the size of the button
 * @param enabled whether the button can be clicked
 * @param submitsInput whether a click requires every input of the screen to be valid
 * @param id the id of the button, or `null` for a generated one
 * @param onClick the handler run on the server when the button is clicked, or `null`
 * @return the button
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.InputGroupButton(
    text: String,
    icon: String? = null,
    variant: ButtonVariant = ButtonVariant.GHOST,
    size: ButtonSize = ButtonSize.XS,
    enabled: Boolean = true,
    submitsInput: Boolean = false,
    id: String? = null,
    onClick: ButtonHandler? = null,
): ButtonElement = InputGroupButton(Component.text(text), icon, variant, size, enabled, submitsInput, id, onClick)

/**
 * Adds a checkbox with a label next to it.
 *
 * @param label the label
 * @param checked whether the box is initially checked
 * @param enabled whether the player can toggle the box
 * @param width how wide the checkbox is laid out
 * @param height how tall the checkbox is laid out
 * @param id the id of the checkbox, or `null` for a generated one
 * @param onChange the handler run on every validated change, or `null`
 * @return the reference to whether the box is checked
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Checkbox(
    label: Component = Component.empty(),
    checked: Boolean = false,
    enabled: Boolean = true,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    onChange: ChangeHandler? = null,
): InputRef<Boolean> {
    val elementId = nextId(id)
    add(CheckboxElement(elementId, label, checked, enabled, width, height, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds a checkbox with a plain label next to it.
 *
 * @param label the label
 * @param checked whether the box is initially checked
 * @param enabled whether the player can toggle the box
 * @param width how wide the checkbox is laid out
 * @param height how tall the checkbox is laid out
 * @param id the id of the checkbox, or `null` for a generated one
 * @param onChange the handler run on every validated change, or `null`
 * @return the reference to whether the box is checked
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Checkbox(
    label: String,
    checked: Boolean = false,
    enabled: Boolean = true,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    onChange: ChangeHandler? = null,
): InputRef<Boolean> = Checkbox(Component.text(label), checked, enabled, width, height, id, onChange)

/**
 * Adds a switch that is on or off.
 *
 * @param checked whether the switch is initially on
 * @param size the size of the switch
 * @param enabled whether the player can switch it
 * @param id the id of the switch, or `null` for a generated one
 * @param onChange the handler run on every validated change, or `null`
 * @return the reference to whether the switch is on
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Switch(
    checked: Boolean = false,
    size: SwitchSize = SwitchSize.DEFAULT,
    enabled: Boolean = true,
    id: String? = null,
    onChange: ChangeHandler? = null,
): InputRef<Boolean> {
    val elementId = nextId(id)
    add(SwitchElement(elementId, checked, size, enabled, bindChange(elementId, onChange)))
    return InputRef(elementId, InputParsers.checked)
}

/**
 * Adds a choice of one option, each drawn as a radio button with its label.
 *
 * @param options the options, in order
 * @param selected the value of the initially selected option, or `null` for none
 * @param orientation whether the options run vertically or horizontally
 * @param required whether having no selection is invalid
 * @param enabled whether the player can change the selection
 * @param width how wide the group is laid out
 * @param height how tall the group is laid out
 * @param id the id of the group, or `null` for a generated one
 * @param onChange the handler run on every validated change, or `null`
 * @return the reference to the value of the selected option, which is `null` while nothing is
 *         selected
 * @throws IllegalArgumentException if [id] starts with `_`, option values repeat or the selection
 *         is not an option
 */
fun ComponentScope.RadioGroup(
    options: List<RadioChoice>,
    selected: String? = null,
    orientation: Orientation = Orientation.VERTICAL,
    required: Boolean = false,
    enabled: Boolean = true,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    onChange: ChangeHandler? = null,
): InputRef<String?> {
    val elementId = nextId(id)
    add(RadioGroupElement(elementId, options, selected, orientation, required, enabled, bindChange(elementId, onChange), width, height))
    return InputRef(elementId, InputParsers.selected)
}

/**
 * Adds a slider with one thumb, or two for a range.
 *
 * @param values the initial value of every thumb, ascending; one or two
 * @param min the smallest value
 * @param max the largest value
 * @param step the distance between two selectable values, starting at [min]
 * @param orientation whether the slider runs horizontally or vertically
 * @param enabled whether the player can move the thumbs
 * @param width how wide the slider is laid out
 * @param height how tall the slider is laid out
 * @param id the id of the slider, or `null` for a generated one
 * @param onChange the handler run on every validated change, or `null`
 * @return the reference to the thumb values, ascending
 * @throws IllegalArgumentException if [id] starts with `_`, the range or step is empty, or the
 *         values are not one or two ascending selectable values
 */
fun ComponentScope.Slider(
    values: List<Double>,
    min: Double = 0.0,
    max: Double = 100.0,
    step: Double = 1.0,
    orientation: Orientation = Orientation.HORIZONTAL,
    enabled: Boolean = true,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    onChange: ChangeHandler? = null,
): InputRef<List<Double>> {
    val elementId = nextId(id)
    add(SliderElement(elementId, values, min, max, step, orientation, enabled, bindChange(elementId, onChange), width, height))
    return InputRef(elementId, InputParsers.numbers)
}

/**
 * Adds a choice of one option from a list that opens below the trigger. Options without groups
 * are given as [options]; grouped options as [groups].
 *
 * @param options the options of a select without groups
 * @param groups the option groups; by default one group without heading holding [options]
 * @param selected the value of the initially selected option, or `null` for none
 * @param placeholder the text shown while nothing is selected
 * @param size the size of the trigger
 * @param required whether having no selection is invalid
 * @param enabled whether the player can change the selection
 * @param width how wide the select is laid out
 * @param height how tall the select is laid out
 * @param id the id of the select, or `null` for a generated one
 * @param onChange the handler run on every validated change, or `null`
 * @return the reference to the value of the selected option, which is `null` while nothing is
 *         selected
 * @throws IllegalArgumentException if [id] starts with `_`, option values repeat or the selection
 *         is not an option
 */
fun ComponentScope.Select(
    options: List<SelectChoice> = emptyList(),
    groups: List<SelectChoiceGroup> = listOf(SelectChoiceGroup(null, options)),
    selected: String? = null,
    placeholder: Component = Component.empty(),
    size: SelectSize = SelectSize.DEFAULT,
    required: Boolean = false,
    enabled: Boolean = true,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    onChange: ChangeHandler? = null,
): InputRef<String?> {
    val elementId = nextId(id)
    add(SelectElement(elementId, groups, selected, placeholder, size, required, enabled, bindChange(elementId, onChange), width, height))
    return InputRef(elementId, InputParsers.selected)
}

/**
 * Adds a plain choice of one option, whose selection the arrow keys change without opening the
 * list. Options without groups are given as [options]; grouped options as [groups].
 *
 * @param options the options of a native select without groups
 * @param groups the option groups; by default one group without heading holding [options]
 * @param selected the value of the initially selected option, or `null` for none
 * @param size the size of the trigger
 * @param required whether having no selection is invalid
 * @param enabled whether the player can change the selection
 * @param width how wide the native select is laid out
 * @param height how tall the native select is laid out
 * @param id the id of the native select, or `null` for a generated one
 * @param onChange the handler run on every validated change, or `null`
 * @return the reference to the value of the selected option, which is `null` while nothing is
 *         selected
 * @throws IllegalArgumentException if [id] starts with `_`, option values repeat or the selection
 *         is not an option
 */
fun ComponentScope.NativeSelect(
    options: List<SelectChoice> = emptyList(),
    groups: List<SelectChoiceGroup> = listOf(SelectChoiceGroup(null, options)),
    selected: String? = null,
    size: SelectSize = SelectSize.DEFAULT,
    required: Boolean = false,
    enabled: Boolean = true,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    onChange: ChangeHandler? = null,
): InputRef<String?> {
    val elementId = nextId(id)
    add(NativeSelectElement(elementId, groups, selected, size, required, enabled, bindChange(elementId, onChange), width, height))
    return InputRef(elementId, InputParsers.selected)
}

/**
 * Adds a combobox for one option: a text field that filters an option list as the player types.
 * Options without groups are given as [options]; grouped options as [groups].
 *
 * @param options the options of a combobox without groups
 * @param groups the option groups; by default one group without heading holding [options]
 * @param selected the value of the initially selected option, or `null` for none
 * @param placeholder the hint shown while the field is empty
 * @param emptyText the text shown when no option matches
 * @param showClear whether a button clears the selection
 * @param required whether having no selection is invalid
 * @param enabled whether the player can change the selection
 * @param width how wide the combobox is laid out
 * @param height how tall the combobox is laid out
 * @param id the id of the combobox, or `null` for a generated one
 * @param onSearch the handler run on every change of the typed query, or `null`
 * @param onChange the handler run on every validated change of the selection, or `null`
 * @return the reference to the value of the selected option, which is `null` while nothing is
 *         selected
 * @throws IllegalArgumentException if [id] starts with `_`, option values repeat or the selection
 *         is not an option
 */
fun ComponentScope.Combobox(
    options: List<SelectChoice> = emptyList(),
    groups: List<SelectChoiceGroup> = listOf(SelectChoiceGroup(null, options)),
    selected: String? = null,
    placeholder: Component = Component.empty(),
    emptyText: Component = Component.text("Keine Ergebnisse."),
    showClear: Boolean = false,
    required: Boolean = false,
    enabled: Boolean = true,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    onSearch: SearchHandler? = null,
    onChange: ChangeHandler? = null,
): InputRef<String?> {
    val elementId = nextId(id)
    add(
        ComboboxElement(
            elementId, groups, listOfNotNull(selected), false, placeholder, emptyText, showClear, required, enabled,
            bindChange(elementId, onChange), bindSearch(elementId, onSearch), width, height,
        ),
    )
    return InputRef(elementId, InputParsers.selected)
}

/**
 * Adds a combobox for several options, shown as chips: a text field that filters an option list
 * as the player types. Options without groups are given as [options]; grouped options as
 * [groups].
 *
 * @param options the options of a combobox without groups
 * @param groups the option groups; by default one group without heading holding [options]
 * @param selected the values of the initially selected options
 * @param placeholder the hint shown while the field is empty
 * @param emptyText the text shown when no option matches
 * @param showClear whether a button clears the selection
 * @param required whether having no selection is invalid
 * @param enabled whether the player can change the selection
 * @param width how wide the combobox is laid out
 * @param height how tall the combobox is laid out
 * @param id the id of the combobox, or `null` for a generated one
 * @param onSearch the handler run on every change of the typed query, or `null`
 * @param onChange the handler run on every validated change of the selection, or `null`
 * @return the reference to the selected values, in the order they were selected
 * @throws IllegalArgumentException if [id] starts with `_`, option values repeat, or a selected
 *         value is not an option or repeats
 */
fun ComponentScope.MultiCombobox(
    options: List<SelectChoice> = emptyList(),
    groups: List<SelectChoiceGroup> = listOf(SelectChoiceGroup(null, options)),
    selected: List<String> = emptyList(),
    placeholder: Component = Component.empty(),
    emptyText: Component = Component.text("Keine Ergebnisse."),
    showClear: Boolean = false,
    required: Boolean = false,
    enabled: Boolean = true,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    onSearch: SearchHandler? = null,
    onChange: ChangeHandler? = null,
): InputRef<List<String>> {
    val elementId = nextId(id)
    add(
        ComboboxElement(
            elementId, groups, selected, true, placeholder, emptyText, showClear, required, enabled,
            bindChange(elementId, onChange), bindSearch(elementId, onSearch), width, height,
        ),
    )
    return InputRef(elementId, InputParsers.list)
}

/**
 * Adds a month calendar for picking dates.
 *
 * @param mode how many dates the calendar selects
 * @param selected the initially selected dates; for a range its first and last date
 * @param month the initially displayed month, or `null` for the month of the first selected date
 *        or else the current month
 * @param min the first selectable date, or `null` for no limit
 * @param max the last selectable date, or `null` for no limit
 * @param disabled the dates that cannot be selected
 * @param showOutsideDays whether days of the neighbouring months fill the first and last week
 * @param captionLayout how the month caption is shown
 * @param required whether an empty selection is invalid
 * @param enabled whether the player can select dates
 * @param id the id of the calendar, or `null` for a generated one
 * @param onChange the handler run on every validated change, or `null`
 * @return the reference to the selected dates, ascending; a range as its first and last date
 * @throws IllegalArgumentException if [id] starts with `_`, the limits are reversed, or the
 *         selection does not fit the mode
 */
fun ComponentScope.Calendar(
    mode: CalendarMode = CalendarMode.SINGLE,
    selected: List<LocalDate> = emptyList(),
    month: YearMonth? = null,
    min: LocalDate? = null,
    max: LocalDate? = null,
    disabled: Set<LocalDate> = emptySet(),
    showOutsideDays: Boolean = true,
    captionLayout: CaptionLayout = CaptionLayout.LABEL,
    required: Boolean = false,
    enabled: Boolean = true,
    id: String? = null,
    onChange: ChangeHandler? = null,
): InputRef<List<LocalDate>> {
    val elementId = nextId(id)
    add(
        CalendarElement(
            elementId, mode, selected, month, min, max, disabled, showOutsideDays, captionLayout, required, enabled,
            bindChange(elementId, onChange),
        ),
    )
    return InputRef(elementId, InputParsers.dates)
}

/**
 * Adds a label.
 *
 * @param text the text
 * @param width how wide the label is laid out
 * @param height how tall the label is laid out
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param forId the id of the input that a click on the label focuses, or `null` for none
 * @param id the id of the label, or `null` for a generated one
 * @return the label
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Label(
    text: Component,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    icon: String? = null,
    forId: String? = null,
    id: String? = null,
): LabelElement = add(LabelElement(nextId(id), text, width, height, icon, forId))

/**
 * Adds a label with plain text.
 *
 * @param text the text
 * @param width how wide the label is laid out
 * @param height how tall the label is laid out
 * @param icon the Lucide name of an icon drawn before the text, or `null` for none
 * @param forId the id of the input that a click on the label focuses, or `null` for none
 * @param id the id of the label, or `null` for a generated one
 * @return the label
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Label(
    text: String,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    icon: String? = null,
    forId: String? = null,
    id: String? = null,
): LabelElement = Label(Component.text(text), width, height, icon, forId, id)

/**
 * Adds a form. Enter in one of its single-line inputs clicks its submit button.
 *
 * @param submitId the id of the button that Enter clicks, or `null` for none
 * @param width how wide the form is laid out
 * @param height how tall the form is laid out
 * @param id the id of the form, or `null` for a generated one
 * @param children the builder of the form's content
 * @return the form
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Form(
    submitId: String? = null,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): FormElement {
    val elementId = nextId(id)
    return add(FormElement(elementId, this.children(children), submitId, width, height))
}

/**
 * Adds a group of related fields with a legend.
 *
 * @param width how wide the set is laid out
 * @param height how tall the set is laid out
 * @param id the id of the set, or `null` for a generated one
 * @param children the builder of the set's legend, fields and groups
 * @return the field set
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.FieldSet(
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): FieldSetElement {
    val elementId = nextId(id)
    return add(FieldSetElement(elementId, this.children(children), width, height))
}

/**
 * Adds a stack of fields.
 *
 * @param width how wide the group is laid out
 * @param height how tall the group is laid out
 * @param id the id of the group, or `null` for a generated one
 * @param children the builder of the group's fields and separators
 * @return the field group
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.FieldGroup(
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): FieldGroupElement {
    val elementId = nextId(id)
    return add(FieldGroupElement(elementId, this.children(children), width, height))
}

/**
 * Adds a form field: its input with a label, description and error.
 *
 * @param orientation whether the parts are stacked or placed side by side
 * @param width how wide the field is laid out
 * @param height how tall the field is laid out
 * @param id the id of the field, or `null` for a generated one
 * @param children the builder of the field's parts
 * @return the field
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.Field(
    orientation: Orientation = Orientation.VERTICAL,
    width: ElementSize = ElementSize.FIT,
    height: ElementSize = ElementSize.FIT,
    id: String? = null,
    children: ComponentScope.() -> Unit,
): FieldElement {
    val elementId = nextId(id)
    return add(FieldElement(elementId, this.children(children), orientation, width, height))
}

/**
 * Adds the stack of a field's texts next to its input in a horizontal field.
 *
 * @param width how wide the stack is laid out
 * @param id the id of the stack, or `null` for a generated one
 * @param children the builder of the texts
 * @return the field content
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.FieldContent(
    width: ElementSize = ElementSize.grow(),
    id: String? = null,
    children: ComponentScope.() -> Unit,
): FieldContentElement {
    val elementId = nextId(id)
    return add(FieldContentElement(elementId, this.children(children), width))
}

/**
 * Adds the legend of a field set.
 *
 * @param text the legend
 * @param asLabel whether the legend is drawn like a label
 * @param id the id of the legend, or `null` for a generated one
 * @return the legend
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.FieldLegend(text: Component, asLabel: Boolean = false, id: String? = null): FieldTextElement =
    add(FieldTextElement(nextId(id), if (asLabel) FieldTextKind.LEGEND_LABEL else FieldTextKind.LEGEND, text))

/**
 * Adds the legend of a field set with plain text.
 *
 * @param text the legend
 * @param asLabel whether the legend is drawn like a label
 * @param id the id of the legend, or `null` for a generated one
 * @return the legend
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.FieldLegend(text: String, asLabel: Boolean = false, id: String? = null): FieldTextElement =
    FieldLegend(Component.text(text), asLabel, id)

/**
 * Adds the label of a field; a click on it focuses the input it names.
 *
 * @param text the label
 * @param forId the id of the input a click on the label focuses, or `null` for none
 * @param id the id of the label, or `null` for a generated one
 * @return the label
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.FieldLabel(text: Component, forId: String? = null, id: String? = null): FieldTextElement =
    add(FieldTextElement(nextId(id), FieldTextKind.LABEL, text, forId))

/**
 * Adds the label of a field with plain text; a click on it focuses the input it names.
 *
 * @param text the label
 * @param forId the id of the input a click on the label focuses, or `null` for none
 * @param id the id of the label, or `null` for a generated one
 * @return the label
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.FieldLabel(text: String, forId: String? = null, id: String? = null): FieldTextElement =
    FieldLabel(Component.text(text), forId, id)

/**
 * Adds the title of a field whose input has its own label.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.FieldTitle(text: Component, id: String? = null): FieldTextElement =
    add(FieldTextElement(nextId(id), FieldTextKind.TITLE, text))

/**
 * Adds the title of a field with plain text.
 *
 * @param text the title
 * @param id the id of the title, or `null` for a generated one
 * @return the title
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.FieldTitle(text: String, id: String? = null): FieldTextElement = FieldTitle(Component.text(text), id)

/**
 * Adds the muted description of a field.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.FieldDescription(text: Component, id: String? = null): FieldTextElement =
    add(FieldTextElement(nextId(id), FieldTextKind.DESCRIPTION, text))

/**
 * Adds the muted description of a field with plain text.
 *
 * @param text the description
 * @param id the id of the description, or `null` for a generated one
 * @return the description
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.FieldDescription(text: String, id: String? = null): FieldTextElement = FieldDescription(Component.text(text), id)

/**
 * Adds the error text of a field, which a failed click fills and which is not drawn while empty.
 *
 * @param text the initial error, empty for none
 * @param id the id of the error text, or `null` for a generated one
 * @return the error text
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.FieldError(text: Component = Component.empty(), id: String? = null): FieldTextElement =
    add(FieldTextElement(nextId(id), FieldTextKind.ERROR, text))

/**
 * Adds the error text of a field with a plain initial error.
 *
 * @param text the initial error
 * @param id the id of the error text, or `null` for a generated one
 * @return the error text
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.FieldError(text: String, id: String? = null): FieldTextElement = FieldError(Component.text(text), id)

/**
 * Adds a line between fields that grows across the available width, optionally with a text in
 * its middle.
 *
 * @param text the text, or `null` for a plain line
 * @param id the id of the separator, or `null` for a generated one
 * @return the separator
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.FieldSeparator(text: Component? = null, id: String? = null): FieldSeparatorElement =
    add(FieldSeparatorElement(nextId(id), text, ElementSize.grow()))

/**
 * Adds a line between fields that grows across the available width, with a plain text in its
 * middle.
 *
 * @param text the text
 * @param id the id of the separator, or `null` for a generated one
 * @return the separator
 * @throws IllegalArgumentException if [id] starts with `_`
 */
fun ComponentScope.FieldSeparator(text: String, id: String? = null): FieldSeparatorElement = FieldSeparator(Component.text(text), id)
