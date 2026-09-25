package dev.slne.surf.roleplay.api.common.license.requirement.requirements

import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.roleplay.api.common.identity.exceptions.NoActiveIdentityException
import dev.slne.surf.roleplay.api.common.license.License
import dev.slne.surf.roleplay.api.common.license.requirement.LicenseRequirement
import dev.slne.surf.roleplay.api.common.user.RoleplayUser

data class HasOtherLicenseRequirement(
    val license: License
) : LicenseRequirement(
    key = key("roleplay", "has_other_license"),
    displayName = {
        text("Lizenz im Besitz: ")
        append(license.displayName)
    }
) {
    override fun isMet(user: RoleplayUser): Boolean {
        val activeIdentity = user.activeIdentity
            ?: throw NoActiveIdentityException(user.uuid)

        return activeIdentity.hasLicense(license)
    }
}
