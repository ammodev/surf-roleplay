package dev.slne.surf.roleplay.fabric.interaction

import dev.slne.surf.roleplay.protocol.interaction.InteractionLimits
import dev.slne.surf.roleplay.protocol.interaction.RadialNode

/**
 * Moves through the levels of a radial tree without asking the server.
 *
 * @property nodes the nodes of the tree, each after its parent
 */
class RadialNavigation(private val nodes: List<RadialNode>) {

    /**
     * The levels entered so far, each as the node indices it lists; the top level first.
     */
    private val levels = ArrayDeque<List<Int>>().apply { addLast(childrenOf(-1)) }

    /**
     * The items of the current ring.
     */
    val items: List<RingItem> get() = RadialLayout.ring(levels.last())

    /**
     * Returns the children of a node in order.
     *
     * @param parent the node index, or `-1` for the top level
     * @return the child indices
     */
    private fun childrenOf(parent: Int): List<Int> = nodes.indices.filter { nodes[it].parent == parent }

    /**
     * Picks an item: a submenu or "Mehr…" opens its entries, an action is returned.
     *
     * @param item the picked item
     * @return the node index of a picked action, or `null` after opening a level
     */
    fun enter(item: RingItem): Int? = when (item) {
        is RingItem.More -> null.also { levels.addLast(item.rest) }
        is RingItem.Node -> if (nodes[item.index].submenu) null.also { levels.addLast(childrenOf(item.index)) } else item.index
    }

    /**
     * Returns to the previous level.
     *
     * @return whether there was a previous level
     */
    fun back(): Boolean {
        if (levels.size <= 1) return false
        levels.removeLast()
        return true
    }

    companion object {

        /**
         * Tells whether a tree received from the server is acceptable: it has at most
         * [InteractionLimits.MAX_RADIAL_NODES] nodes, and every node's parent is either `-1` or
         * the index of an earlier node.
         *
         * @param nodes the received nodes
         * @return `true` if the tree can be shown, `false` if it must be ignored
         */
        fun isValid(nodes: List<RadialNode>): Boolean =
            nodes.size <= InteractionLimits.MAX_RADIAL_NODES &&
                nodes.withIndex().all { (index, node) -> node.parent == -1 || node.parent in 0 until index }
    }
}
