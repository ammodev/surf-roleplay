package dev.slne.surf.roleplay.api.common.license.requirement

import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

/**
 * A condition an identity must satisfy before a license can be granted to it.
 *
 * @property key the identifier of this requirement kind
 * @property displayName the player-facing description of this requirement
 */
abstract class LicenseRequirement(
    val key: Key,
    displayName: SurfComponentBuilder.() -> Unit
) : ComponentLike {
    val displayName: Component = SurfComponentBuilder.builder().apply(displayName).build()

    /**
     * Returns the player-facing description of this requirement.
     */
    override fun asComponent(): Component {
        return displayName
    }

    /**
     * Checks whether [identity] satisfies this requirement.
     *
     * @param identity the identity the license would be granted to
     * @return `true` if the requirement is satisfied
     */
    abstract fun isMet(identity: RoleplayIdentity): Boolean
}
