package dev.slne.surf.roleplay.api.common.license.requirement

import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.roleplay.api.common.user.RoleplayUser
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

abstract class LicenseRequirement(
    val key: Key,
    displayName: SurfComponentBuilder.() -> Unit
) : ComponentLike {
    val displayName: Component = SurfComponentBuilder.builder().apply(displayName).build()

    override fun asComponent(): Component {
        return displayName
    }

    abstract fun isMet(user: RoleplayUser): Boolean
}