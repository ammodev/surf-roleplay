package dev.slne.surf.roleplay.api.common.identity.rank

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

/**
 * A rank an organisation grants its members.
 *
 * Ranks of the same organisation form an ascending hierarchy expressed through
 * [level], with lower values ranking below higher ones.
 *
 * @property key the identifier this rank is stored and looked up under
 * @property displayName the human-readable name of this rank
 * @property level the seniority of this rank within its organisation
 */
abstract class IdentityRank(
    val key: Key,
    val displayName: Component,
    val level: Int
) : ComponentLike {
    /**
     * Returns [displayName] as the component representation of this rank.
     */
    override fun asComponent(): Component {
        return displayName
    }
}
