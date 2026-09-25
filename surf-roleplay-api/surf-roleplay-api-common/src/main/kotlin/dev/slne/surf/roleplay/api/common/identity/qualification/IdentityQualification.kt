package dev.slne.surf.roleplay.api.common.identity.qualification

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

data class IdentityQualification(
    val key: Key,
    val displayName: Component
) : ComponentLike {
    override fun asComponent(): Component {
        return displayName
    }
}