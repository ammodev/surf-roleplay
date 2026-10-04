package dev.slne.surf.roleplay.fabric.settings

import dev.slne.surf.roleplay.protocol.screen.ScreenNode

/**
 * What the settings screen shows, handed to the content builder of a category.
 *
 * @property rows the key bindings, in display order
 * @property conflicts the ids of the bindings whose key is also bound by another binding
 * @property capturing the id of the binding that waits for a new key, or `null`
 * @property settings the current client settings
 */
data class SettingsState(
    val rows: List<BindingRow>,
    val conflicts: Set<String>,
    val capturing: String?,
    val settings: ClientSettings,
)

/**
 * One category of the settings screen: a tab trigger and the content shown beside it.
 *
 * @property id the id of the category, which is the value of its tab
 * @property title the German title on the trigger
 * @property icon the Lucide name of the icon on the trigger
 * @property content builds the nodes of the category from the current state
 */
data class SettingsCategory(
    val id: String,
    val title: String,
    val icon: String,
    val content: (SettingsState) -> List<ScreenNode>,
)
