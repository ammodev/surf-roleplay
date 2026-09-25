package dev.slne.surf.roleplay.api.common.license.licenses

import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.api.core.util.objectListOf
import dev.slne.surf.roleplay.api.common.license.License
import dev.slne.surf.roleplay.api.common.license.requirement.requirements.HasOtherLicenseRequirement

/**
 * Truck driver license definition.
 *
 * Represents a truck driving license with a requirement for the car license.
 */
object TruckLicense : License(
    key = key("roleplay", "truck_license"),
    displayName = {
        text("LKW-Führerschein")
    },
    requirements = objectListOf(
        HasOtherLicenseRequirement(CarLicense)
    )
)
