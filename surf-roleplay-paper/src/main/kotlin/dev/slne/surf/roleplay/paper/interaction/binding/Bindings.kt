package dev.slne.surf.roleplay.paper.interaction.binding

/**
 * The position of a bound block.
 *
 * @property world the world name
 * @property x the x coordinate
 * @property y the y coordinate
 * @property z the z coordinate
 */
data class BlockPosition(val world: String, val x: Int, val y: Int, val z: Int)

/**
 * One behaviour bound to a target.
 *
 * @property key the behaviour key
 * @property parameters the parameters passed to the behaviour
 */
data class BehaviourBinding(val key: String, val parameters: Map<String, String>)

/**
 * The behaviours bound to one target.
 *
 * @property behaviours the behaviours in display order
 * @property range the range override in blocks, or `null` for the default range
 */
data class TargetBinding(val behaviours: List<BehaviourBinding>, val range: Double?)

/**
 * Every loaded binding.
 *
 * @property blocks the block bindings by position of the lower block
 * @property entities the entity bindings by entity binding key
 */
data class Bindings(val blocks: Map<BlockPosition, TargetBinding>, val entities: Map<String, TargetBinding>) {
    /**
     * Holds the empty bindings.
     */
    companion object {
        /**
         * No bindings at all.
         */
        val EMPTY: Bindings = Bindings(emptyMap(), emptyMap())
    }
}
