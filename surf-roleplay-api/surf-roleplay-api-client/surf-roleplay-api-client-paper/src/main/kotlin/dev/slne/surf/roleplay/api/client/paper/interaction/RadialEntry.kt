package dev.slne.surf.roleplay.api.client.paper.interaction

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/**
 * A top-level section of the radial menu.
 *
 * @property label the caption, German
 */
enum class RadialCategory(val label: String) {
    /**
     * Actions on the player themself.
     */
    SELF("Ich"),

    /**
     * Actions on the vehicle the player sits in or stands next to.
     */
    VEHICLE("Fahrzeug"),

    /**
     * Actions of the player's job.
     */
    JOB("Job"),
}

/**
 * A condition of a radial entry. It runs on the player's thread and reads in-memory state only.
 */
fun interface RadialCondition {
    /**
     * Tells whether the entry is shown and may run.
     *
     * @param player the player
     * @return whether the condition passes
     */
    fun test(player: Player): Boolean
}

/**
 * The action of a radial entry. It runs on the player's thread after every condition on the
 * entry's path passed again.
 */
fun interface RadialHandler {
    /**
     * Runs the action.
     *
     * @param player the player
     */
    fun run(player: Player)
}

/**
 * An entry of the radial menu.
 */
sealed interface RadialEntry {
    /**
     * The stable key, lowercase with dots and underscores.
     */
    val key: String

    /**
     * The caption, German.
     */
    val label: Component

    /**
     * The Lucide icon name, empty for none.
     */
    val icon: String

    /**
     * The conditions that must all pass for the entry to be shown and run.
     */
    val conditions: List<RadialCondition>
}

/**
 * A radial entry that runs an action.
 *
 * @property handler the action
 * @throws IllegalArgumentException if [key] is malformed
 */
data class RadialAction(
    override val key: String,
    override val label: Component,
    override val icon: String = "",
    override val conditions: List<RadialCondition> = emptyList(),
    val handler: RadialHandler,
) : RadialEntry {
    init {
        require(InteractionBehaviour.KEY_PATTERN.matches(key)) { "Radial key '$key' is malformed" }
    }
}

/**
 * A radial entry that opens further entries.
 *
 * @property children the entries inside, in display order
 * @throws IllegalArgumentException if [key] is malformed
 */
data class RadialSubmenu(
    override val key: String,
    override val label: Component,
    override val icon: String = "",
    override val conditions: List<RadialCondition> = emptyList(),
    val children: List<RadialEntry>,
) : RadialEntry {
    init {
        require(InteractionBehaviour.KEY_PATTERN.matches(key)) { "Radial key '$key' is malformed" }
    }
}
