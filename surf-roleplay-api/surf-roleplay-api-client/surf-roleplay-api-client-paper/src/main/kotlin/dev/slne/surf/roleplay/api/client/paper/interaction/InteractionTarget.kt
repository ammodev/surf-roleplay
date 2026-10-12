package dev.slne.surf.roleplay.api.client.paper.interaction

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

/**
 * The kind of an interaction target. Kinds are an open set of snake_case keys.
 *
 * @property key the snake_case key
 * @throws IllegalArgumentException if [key] is not snake_case
 */
@JvmInline
value class TargetKind(val key: String) {
    init {
        require(PATTERN.matches(key)) { "Target kind '$key' is not snake_case" }
    }

    /**
     * Holds the built-in kinds.
     */
    companion object {
        /**
         * The pattern of a kind key.
         */
        private val PATTERN = Regex("[a-z0-9_]+")

        /**
         * A block in the world.
         */
        val BLOCK: TargetKind = TargetKind("block")

        /**
         * A Bukkit entity.
         */
        val ENTITY: TargetKind = TargetKind("entity")
    }
}

/**
 * Something a player can interact with through the target eye.
 */
interface InteractionTarget {
    /**
     * The kind of this target.
     */
    val kind: TargetKind
}

/**
 * A block target. For two-block blocks such as doors it is the lower half.
 *
 * @property block the block
 */
data class BlockTarget(val block: Block) : InteractionTarget {
    /**
     * Always [TargetKind.BLOCK].
     */
    override val kind: TargetKind get() = TargetKind.BLOCK
}

/**
 * An entity target.
 *
 * @property entity the entity
 */
data class EntityTarget(val entity: Entity) : InteractionTarget {
    /**
     * Always [TargetKind.ENTITY].
     */
    override val kind: TargetKind get() = TargetKind.ENTITY
}

/**
 * What a condition or action of a behaviour sees.
 *
 * @property player the player who interacts
 * @property target the target
 * @property parameters the parameters of the binding that bound the behaviour to the target
 */
data class InteractionContext(
    val player: Player,
    val target: InteractionTarget,
    val parameters: Map<String, String>,
)
