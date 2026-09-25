package dev.slne.surf.roleplay.api.common.license.requirement.requirements

import dev.slne.surf.api.core.messages.adventure.key
import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import dev.slne.surf.roleplay.api.common.license.License
import dev.slne.surf.roleplay.api.common.license.requirement.LicenseRequirement

/**
 * Requires the identity to already hold another, unrevoked license.
 *
 * @property license the license that must already be held
 */
data class HasOtherLicenseRequirement(
    val license: License
) : LicenseRequirement(
    key = key("roleplay", "has_other_license"),
    displayName = {
        text("Lizenz im Besitz: ")
        append(license.displayName)
    }
) {
    /**
     * Checks whether [identity] currently holds [license].
     *
     * A revoked license does not satisfy this requirement.
     *
     * @param identity the identity the license would be granted to
     * @return `true` if [identity] holds [license] and it is not revoked
     */
    override fun isMet(identity: RoleplayIdentity): Boolean = identity.hasLicense(license)
}
