package dev.slne.surf.roleplay.api.common.license.revoke

import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

/**
 * The reason a held license was revoked.
 */
enum class LicenseRevokedReason(
    displayName: SurfComponentBuilder.() -> Unit
) : ComponentLike {
    /**
     * The license was revoked for administrative reasons.
     */
    ADMINISTRATIVE({
        text("Administrative Gründe")
    }),

    /**
     * The license was revoked because of criminal conduct.
     */
    CRIMINAL({
        text("Kriminelle Gründe")
    }),

    /**
     * The license was revoked for a reason not covered by the other entries.
     */
    OTHER({
        text("Andere Gründe")
    });

    /**
     * The human-readable description of this revocation reason.
     */
    val displayName = SurfComponentBuilder.builder().apply(displayName).build()

    /**
     * Returns [displayName] as the component representation of this revocation reason.
     */
    override fun asComponent(): Component {
        return displayName
    }
}
