package dev.slne.surf.roleplay.fabric.settings

import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier

/**
 * One key binding as the settings screen shows it.
 *
 * @property id the id of the binding, unique among the shown bindings
 * @property name the translated name of the binding
 * @property keyLabel the translated name of the bound key
 * @property isDefault whether the binding is on its default key
 * @property unbound whether no key is bound
 */
data class BindingRow(
    val id: String,
    val name: String,
    val keyLabel: String,
    val isDefault: Boolean,
    val unbound: Boolean,
)

/**
 * Rules for the key bindings of the roleplay category.
 */
object KeyBindings {

    /**
     * The saved name of the key that stands for "no key bound".
     */
    const val UNBOUND_KEY: String = "key.keyboard.unknown"

    /**
     * Finds the bindings that share their key with another binding. Unbound bindings never
     * conflict.
     *
     * @param rows pairs of a binding id and the saved name of its key, such as `key.keyboard.r`
     * @return the ids of the conflicting bindings
     */
    fun conflicts(rows: List<Pair<String, String>>): Set<String> =
        rows.filter { it.second != UNBOUND_KEY }
            .groupBy({ it.second }, { it.first })
            .values
            .filter { it.size > 1 }
            .flatten()
            .toSet()
}

/**
 * The key binding category that holds every key binding of the mod.
 */
object RoleplayKeys {

    /**
     * The roleplay category, registered on first use.
     */
    val category: KeyMapping.Category by lazy {
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath("surf-roleplay", "roleplay"))
    }
}
