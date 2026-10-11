package dev.slne.surf.roleplay.paper.interaction

import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionBehaviour
import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionContext
import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionTarget
import dev.slne.surf.roleplay.protocol.interaction.InteractionLimits
import org.bukkit.entity.Player

/**
 * A behaviour bound to a target, with the parameters of its binding.
 *
 * @property behaviour the behaviour
 * @property parameters the binding parameters
 */
data class BoundBehaviour(val behaviour: InteractionBehaviour, val parameters: Map<String, String>) {
    /**
     * Returns the context the behaviour sees for a player and target.
     *
     * @param player the player
     * @param target the target
     * @return the context
     */
    fun context(player: Player, target: InteractionTarget) = InteractionContext(player, target, parameters)

    /**
     * Tells whether the behaviour applies to the target's kind and all its conditions pass.
     *
     * @param player the player
     * @param target the target
     * @return whether it passes
     */
    fun passes(player: Player, target: InteractionTarget): Boolean {
        if (target.kind !in behaviour.kinds) return false
        val context = context(player, target)
        return behaviour.conditions.all { it.test(context) }
    }
}

/**
 * Chooses the options a target offers.
 */
object OptionEvaluator {
    /**
     * Returns the bound behaviours that apply to the target's kind and whose conditions all pass,
     * in order and at most [InteractionLimits.MAX_OPTIONS]. Conditions of behaviours after the cap
     * are not evaluated.
     *
     * @param bound the behaviours bound to the target
     * @param player the player
     * @param target the target
     * @param onError called with the behaviour key and the failure when a condition throws; that
     *        behaviour is not offered
     * @return the passing behaviours
     */
    fun passing(bound: List<BoundBehaviour>, player: Player, target: InteractionTarget, onError: (String, Throwable) -> Unit): List<BoundBehaviour> =
        bound.asSequence().filter {
            try {
                it.passes(player, target)
            } catch (exception: Exception) {
                onError(it.behaviour.key, exception)
                false
            }
        }.take(InteractionLimits.MAX_OPTIONS).toList()
}
