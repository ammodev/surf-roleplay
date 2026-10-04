package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The size of a select trigger.
 */
@Serializable
enum class SelectSize {
    /**
     * The regular size.
     */
    @ProtoNumber(0)
    DEFAULT,

    /**
     * A small size.
     */
    @ProtoNumber(1)
    SM,
}

/**
 * One option of a select or combobox.
 *
 * @property value the value submitted while this option is selected
 * @property label the label shown for this option, as component JSON
 * @property enabled whether the option can be selected
 */
@Serializable
data class SelectOption(
    @ProtoNumber(1) val value: String,
    @ProtoNumber(2) val label: String = "",
    @ProtoNumber(3) val enabled: Boolean = true,
)

/**
 * A group of options in an option list. Consecutive groups are drawn apart with a separator.
 *
 * @property label the heading of the group as component JSON, or `null` for none
 * @property options the options of the group, in order
 */
@Serializable
data class SelectGroup(
    @ProtoNumber(1) val label: String? = null,
    @ProtoNumber(2) val options: List<SelectOption> = emptyList(),
)

/**
 * A choice of one option from a list that opens below the trigger. Its value is the value of the
 * selected option, or empty.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property groups the option groups, in display order
 * @property selected the value of the selected option, or `null` if none is selected
 * @property placeholder the text shown while nothing is selected, as component JSON
 * @property size the size of the trigger
 * @property required whether having no selection is invalid
 * @property enabled whether the player can change the selection
 * @property notifyChange whether the mod reports every change of the value at once
 */
@Serializable
@SerialName("select")
data class SelectNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val groups: List<SelectGroup> = emptyList(),
    @ProtoNumber(5) val selected: String? = null,
    @ProtoNumber(6) val placeholder: String = "",
    @ProtoNumber(7) val size: SelectSize = SelectSize.DEFAULT,
    @ProtoNumber(8) val required: Boolean = false,
    @ProtoNumber(9) val enabled: Boolean = true,
    @ProtoNumber(10) val notifyChange: Boolean = false,
) : ScreenNode

/**
 * A text field that filters an option list as the player types, for one option or several shown
 * as chips. The mod filters the options by label, ignoring case. Its value is the values of the
 * selected options joined by commas, in the order they were selected.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property groups the option groups, in display order
 * @property selected the values of the selected options
 * @property multiple whether several options can be selected
 * @property placeholder the hint shown while the field is empty, as component JSON
 * @property emptyText the text shown when no option matches, as component JSON
 * @property showClear whether a button clears the selection
 * @property required whether having no selection is invalid
 * @property enabled whether the player can change the selection
 * @property notifyChange whether the mod reports every change of the value at once
 * @property notifySearch whether the mod reports every change of the typed query
 */
@Serializable
@SerialName("combobox")
data class ComboboxNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val groups: List<SelectGroup> = emptyList(),
    @ProtoNumber(5) val selected: List<String> = emptyList(),
    @ProtoNumber(6) val multiple: Boolean = false,
    @ProtoNumber(7) val placeholder: String = "",
    @ProtoNumber(8) val emptyText: String = "",
    @ProtoNumber(9) val showClear: Boolean = false,
    @ProtoNumber(10) val required: Boolean = false,
    @ProtoNumber(11) val enabled: Boolean = true,
    @ProtoNumber(12) val notifyChange: Boolean = false,
    @ProtoNumber(13) val notifySearch: Boolean = false,
) : ScreenNode

/**
 * Replaces the options of a combobox while keeping its selection and typed query.
 *
 * @property targetId the id of the combobox
 * @property groups the new option groups
 */
@Serializable
@SerialName("set_options")
data class SetOptions(
    @ProtoNumber(1) val targetId: String,
    @ProtoNumber(2) val groups: List<SelectGroup> = emptyList(),
) : PatchOperation
