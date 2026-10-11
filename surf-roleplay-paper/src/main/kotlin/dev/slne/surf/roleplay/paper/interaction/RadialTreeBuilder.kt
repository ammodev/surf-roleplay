package dev.slne.surf.roleplay.paper.interaction

import dev.slne.surf.roleplay.api.client.paper.interaction.RadialAction
import dev.slne.surf.roleplay.api.client.paper.interaction.RadialCategory
import dev.slne.surf.roleplay.api.client.paper.interaction.RadialEntry
import dev.slne.surf.roleplay.api.client.paper.interaction.RadialSubmenu
import dev.slne.surf.roleplay.protocol.interaction.InteractionLimits
import dev.slne.surf.roleplay.protocol.interaction.RadialNode
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/**
 * Builds the radial tree of a player from the registered entries.
 */
object RadialTreeBuilder {

    /**
     * A built tree.
     *
     * @property nodes the nodes in breadth-first order, each after its parent
     * @property actions for every action node, the entries from its top entry down to the action
     * @property dropped how many visible nodes did not fit under the node limit
     */
    data class Built(val nodes: List<RadialNode>, val actions: Map<Int, List<RadialEntry>>, val dropped: Int)

    /**
     * A visible node before flattening.
     *
     * @property label the caption
     * @property icon the icon
     * @property path the entries from the top entry to this node, empty for a category
     * @property children the visible children
     */
    private class Visible(val label: Component, val icon: String, val path: List<RadialEntry>, val children: List<Visible>)

    /**
     * Builds the tree. Entries whose conditions fail, submenus without visible children and
     * categories without visible entries are left out. Categories become top-level submenus in
     * the order of [RadialCategory]. The tree is cut at [InteractionLimits.MAX_RADIAL_NODES].
     *
     * @param entries the registered entries with their categories, in registration order
     * @param player the player, on whose thread this runs
     * @param text converts a component to the JSON sent to the client
     * @param onError called with the entry key and the failure when a condition throws; that
     *        entry is hidden
     * @return the tree
     */
    fun build(entries: List<Pair<RadialCategory, RadialEntry>>, player: Player, text: (Component) -> String, onError: (String, Throwable) -> Unit): Built {
        /**
         * Tells whether an entry is shown to the player; an entry whose condition throws is hidden.
         *
         * @param entry the entry
         * @return whether all conditions pass
         */
        fun shown(entry: RadialEntry): Boolean = try {
            entry.conditions.all { it.test(player) }
        } catch (exception: Exception) {
            onError(entry.key, exception)
            false
        }

        /**
         * Returns the visible form of an entry, or `null` if the entry is hidden or is a submenu
         * without visible children.
         *
         * @param entry the entry
         * @param path the entries from the top entry down to the entry's parent
         * @return the visible node, or `null`
         */
        fun visible(entry: RadialEntry, path: List<RadialEntry>): Visible? {
            if (!shown(entry)) return null
            val own = path + entry
            return when (entry) {
                is RadialAction -> Visible(entry.label, entry.icon, own, emptyList())
                is RadialSubmenu -> entry.children.mapNotNull { visible(it, own) }.takeIf { it.isNotEmpty() }?.let { Visible(entry.label, entry.icon, own, it) }
            }
        }

        val roots = RadialCategory.entries.mapNotNull { category ->
            val children = entries.filter { it.first == category }.mapNotNull { visible(it.second, emptyList()) }
            if (children.isEmpty()) null else Visible(Component.text(category.label), "", emptyList(), children)
        }

        val nodes = mutableListOf<RadialNode>()
        val actions = mutableMapOf<Int, List<RadialEntry>>()
        var dropped = 0
        val queue = ArrayDeque(roots.map { -1 to it })
        while (queue.isNotEmpty()) {
            val (parent, node) = queue.removeFirst()
            if (nodes.size >= InteractionLimits.MAX_RADIAL_NODES) {
                dropped++
                node.children.forEach { queue.addLast(-1 to it) }
                continue
            }
            val index = nodes.size
            val submenu = node.path.isEmpty() || node.path.last() is RadialSubmenu
            nodes += RadialNode(parent = parent, label = text(node.label), icon = node.icon, submenu = submenu)
            if (!submenu) actions[index] = node.path
            node.children.forEach { queue.addLast(index to it) }
        }
        return Built(nodes, actions, dropped)
    }
}
