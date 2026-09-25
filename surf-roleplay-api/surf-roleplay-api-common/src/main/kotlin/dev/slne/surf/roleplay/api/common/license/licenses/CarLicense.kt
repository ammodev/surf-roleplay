package dev.slne.surf.roleplay.api.common.license.licenses

import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.util.objectListOf
import dev.slne.surf.roleplay.api.common.license.License

/**
 * Car driver license definition.
 *
 * Represents a car driving license with no requirements.
 */
object CarLicense : License(
    key = key("roleplay", "car_license"),
    displayName = {
        text("Führerschein")
    },
    requirements = objectListOf()
)
