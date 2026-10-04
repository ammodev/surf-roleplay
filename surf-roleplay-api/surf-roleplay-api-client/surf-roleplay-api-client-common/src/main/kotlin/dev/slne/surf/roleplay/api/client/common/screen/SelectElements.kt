package dev.slne.surf.roleplay.api.client.common.screen

import net.kyori.adventure.text.Component

/**
 * The size of a select trigger.
 */
enum class SelectSize {
    /**
     * The regular size.
     */
    DEFAULT,

    /**
     * A small size.
     */
    SM,
}

/**
 * One option of a select or combobox.
 *
 * @property value the value reported while this option is selected
 * @property label the label shown for this option
 * @property enabled whether the option can be selected
 */
data class SelectChoice(val value: String, val label: Component, val enabled: Boolean = true)

/**
 * A group of options, drawn with an optional heading and apart from the neighbouring groups.
 *
 * @property label the heading of the group, or `null` for none
 * @property options the options of the group, in order
 */
data class SelectChoiceGroup(val label: Component? = null, val options: List<SelectChoice>)

/**
 * Checks that the option values of groups are unique and that selected values are options.
 *
 * @param groups the option groups
 * @param selected the selected values
 * @throws IllegalArgumentException if a value repeats or a selected value is not an option
 */
private fun requireOptions(groups: List<SelectChoiceGroup>, selected: Collection<String>) {
    val values = groups.flatMap { group -> group.options.map { it.value } }
    require(values.toSet().size == values.size) { "Option values are not unique" }
    require(selected.all { it in values }) { "The selection is not an option: $selected" }
}

/**
 * A choice of one option from a list that opens below the trigger. Handlers read the value of
 * the selected option with [ScreenValues.selected].
 *
 * @property id the id of this element
 * @property groups the option groups, in display order
 * @property selected the value of the initially selected option, or `null` for none
 * @property placeholder the text shown while nothing is selected
 * @property size the size of the trigger
 * @property required whether having no selection is invalid
 * @property enabled whether the player can change the selection
 * @property onChange the handler run on every validated change, or `null`
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 * @throws IllegalArgumentException if option values repeat or the selection is not an option
 */
data class SelectElement(
    override val id: String,
    val groups: List<SelectChoiceGroup>,
    val selected: String? = null,
    val placeholder: Component = Component.empty(),
    val size: SelectSize = SelectSize.DEFAULT,
    val required: Boolean = false,
    val enabled: Boolean = true,
    val onChange: ChangeHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement {
    init {
        requireOptions(groups, listOfNotNull(selected))
    }
}

/**
 * A text field that filters an option list as the player types, for one option or several shown
 * as chips. The mod filters by label, ignoring case. Handlers read the selected values, in the
 * order they were selected, with [ScreenValues.list].
 *
 * A combobox with a search handler also reports every change of the typed query; the handler
 * can replace the options with [ScreenPatchBuilder.setOptions], for searches the server runs.
 *
 * @property id the id of this element
 * @property groups the option groups, in display order
 * @property selected the values of the initially selected options
 * @property multiple whether several options can be selected
 * @property placeholder the hint shown while the field is empty
 * @property emptyText the text shown when no option matches
 * @property showClear whether a button clears the selection
 * @property required whether having no selection is invalid
 * @property enabled whether the player can change the selection
 * @property onChange the handler run on every validated change of the selection, or `null`
 * @property onSearch the handler run on every change of the typed query, or `null`
 * @property width how wide this element is laid out
 * @property height how tall this element is laid out
 * @throws IllegalArgumentException if option values repeat, a selected value is not an option or
 *         repeats, or a single combobox selects more than one option
 */
data class ComboboxElement(
    override val id: String,
    val groups: List<SelectChoiceGroup>,
    val selected: List<String> = emptyList(),
    val multiple: Boolean = false,
    val placeholder: Component = Component.empty(),
    val emptyText: Component = Component.text("Keine Ergebnisse."),
    val showClear: Boolean = false,
    val required: Boolean = false,
    val enabled: Boolean = true,
    val onChange: ChangeHandler? = null,
    val onSearch: SearchHandler? = null,
    override val width: ElementSize = ElementSize.FIT,
    override val height: ElementSize = ElementSize.FIT,
) : ScreenElement {
    init {
        requireOptions(groups, selected)
        require(selected.toSet().size == selected.size) { "Selected values repeat: $selected" }
        require(multiple || selected.size <= 1) { "A single combobox selects at most one option: $selected" }
    }
}

/**
 * Runs when the player changes the typed query of a combobox that has a search handler.
 */
fun interface SearchHandler {
    /**
     * Handles a changed query.
     *
     * @param search the query and the screen it was typed on
     */
    fun onSearch(search: ScreenSearch)
}

/**
 * A changed query of a combobox.
 *
 * @property screen the screen the combobox is on
 * @property inputId the id of the combobox
 * @property query the text the player typed
 */
data class ScreenSearch(val screen: OpenScreen, val inputId: String, val query: String)
