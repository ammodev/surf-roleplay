package dev.slne.surf.roleplay.api.client.paper.interaction

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/**
 * A condition of a behaviour. It runs on the thread that owns the target and reads in-memory
 * state only, never the database or the network.
 */
fun interface InteractionCondition {
    /**
     * Tells whether the behaviour is available in a context.
     *
     * @param context the context
     * @return whether the condition passes
     */
    fun test(context: InteractionContext): Boolean
}

/**
 * The action of a behaviour. It runs on the thread that owns the target, after every condition
 * passed again.
 */
fun interface InteractionAction {
    /**
     * Runs the action.
     *
     * @param context the context
     */
    fun run(context: InteractionContext)
}

/**
 * Something a player can do with a bound target, such as opening a door.
 *
 * @property key the stable key bindings refer to, lowercase with dots and underscores
 * @property kinds the target kinds the behaviour applies to
 * @property label returns the caption of the option in a context, German
 * @property icon the Lucide icon name, empty for none
 * @property conditions the conditions that must all pass for the option to be offered and run
 * @property action the action
 * @throws IllegalArgumentException if [key] is malformed or [kinds] is empty
 */
class InteractionBehaviour(
    val key: String,
    val kinds: Set<TargetKind>,
    val label: (InteractionContext) -> Component,
    val icon: String = "",
    val conditions: List<InteractionCondition> = emptyList(),
    val action: InteractionAction,
) {
    init {
        require(KEY_PATTERN.matches(key)) { "Behaviour key '$key' must be lowercase with dots and underscores" }
        require(kinds.isNotEmpty()) { "Behaviour '$key' applies to no target kind" }
    }

    /**
     * Holds the key pattern.
     */
    companion object {
        /**
         * The pattern of behaviour and radial entry keys.
         */
        val KEY_PATTERN: Regex = Regex("[a-z0-9_]+(\\.[a-z0-9_]+)*")
    }
}

/**
 * A contribution to the interaction-blocked predicate, such as being downed or cuffed. It runs on
 * the player's thread.
 */
fun interface InteractionBlocker {
    /**
     * Tells whether the player may not interact right now.
     *
     * @param player the player
     * @return whether interaction is blocked
     */
    fun isBlocked(player: Player): Boolean
}
