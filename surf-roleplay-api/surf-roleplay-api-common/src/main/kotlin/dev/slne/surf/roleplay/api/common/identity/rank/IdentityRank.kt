package dev.slne.surf.roleplay.api.common.identity.rank

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

/**
 * A rank an organisation grants its members.
 *
 * Ranks of the same organisation form an ascending hierarchy expressed through
 * [level], with lower values ranking below higher ones.
 */
abstract class IdentityRank(
    val key: Key,
    val displayName: Component,
    val level: Int
) : ComponentLike {
    override fun asComponent(): Component {
        return displayName
    }
}
