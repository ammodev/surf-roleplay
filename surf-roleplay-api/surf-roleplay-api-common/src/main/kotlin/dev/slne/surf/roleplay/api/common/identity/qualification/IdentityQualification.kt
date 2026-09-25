package dev.slne.surf.roleplay.api.common.identity.qualification

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

/**
 * A qualification an organisation can grant its members, independent of rank.
 *
 * @property key the identifier this qualification is stored and looked up under
 * @property displayName the human-readable name of this qualification
 */
abstract class IdentityQualification(
    val key: Key,
    val displayName: Component
) : ComponentLike {
    /**
     * Returns [displayName] as the component representation of this qualification.
     */
    override fun asComponent(): Component {
        return displayName
    }
}
