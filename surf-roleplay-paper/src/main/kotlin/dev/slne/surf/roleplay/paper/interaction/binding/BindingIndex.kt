package dev.slne.surf.roleplay.paper.interaction.binding

import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType

/**
 * The loaded bindings, looked up by targeted block or entity. Safe to read from any thread.
 */
class BindingIndex {

    /**
     * The current bindings, replaced as a whole on reload.
     */
    @Volatile
    var bindings: Bindings = Bindings.EMPTY

    /**
     * Returns the binding of a block. The upper half of a two-block block resolves to its lower
     * half.
     *
     * @param block the targeted block
     * @return the lower block with its binding, or `null` if the block is unbound
     */
    fun forBlock(block: Block): Pair<Block, TargetBinding>? {
        val base = if ((block.blockData as? Bisected)?.half == Bisected.Half.TOP) block.getRelative(BlockFace.DOWN) else block
        val binding = bindings.blocks[BlockPosition(base.world.name, base.x, base.y, base.z)] ?: return null
        return base to binding
    }

    /**
     * Returns the binding of an entity through its binding key tag. Runs on the entity's thread.
     *
     * @param entity the targeted entity
     * @return the binding, or `null` if the entity has no tag or its key is unbound
     */
    fun forEntity(entity: Entity): TargetBinding? {
        val key = entity.persistentDataContainer.get(ENTITY_KEY, PersistentDataType.STRING) ?: return null
        return bindings.entities[key]
    }

    /**
     * Holds the entity tag key.
     */
    companion object {
        /**
         * The persistent data key that holds an entity's binding key.
         */
        val ENTITY_KEY: NamespacedKey = NamespacedKey("surf-roleplay", "interaction_key")
    }
}
