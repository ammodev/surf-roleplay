package dev.slne.surf.roleplay.api.common.license.revoke

import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

enum class LicenseRevokedReason(
    displayName: SurfComponentBuilder.() -> Unit
) : ComponentLike {
    ADMINISTRATIVE({
        text("Administrative Gründe")
    }),
    CRIMINAL({
        text("Kriminelle Gründe")
    }),
    OTHER({
        text("Andere Gründe")
    });

    val displayName = SurfComponentBuilder.builder().apply(displayName).build()

    override fun asComponent(): Component {
        return displayName
    }
}