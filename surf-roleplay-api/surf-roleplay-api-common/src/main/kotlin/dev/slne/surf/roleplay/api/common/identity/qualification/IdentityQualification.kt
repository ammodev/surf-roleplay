package dev.slne.surf.roleplay.api.common.identity.qualification

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

/**
 * A qualification an organisation can grant its members, independent of rank.
 */
abstract class IdentityQualification(
    val key: Key,
    val displayName: Component
) : ComponentLike {
    override fun asComponent(): Component {
        return displayName
    }
}
