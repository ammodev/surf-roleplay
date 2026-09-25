package dev.slne.surf.roleplay.api.common.license

import dev.slne.surf.roleplay.api.common.license.user.UserLicense

/**
 * The outcome of an attempt to grant a [License] to an identity.
 */
sealed interface LicenseGrantResult {
    /**
     * The license was granted successfully.
     *
     * @property userLicense the license that was granted
     */
    data class Granted(val userLicense: UserLicense) : LicenseGrantResult

    /**
     * The identity already holds the license, so no new license was granted.
     */
    data object AlreadyOwned : LicenseGrantResult

    /**
     * The license was not granted because its requirements were not met.
     *
     * @property calculation the breakdown of which requirements were and were not met
     */
    data class RequirementsNotMet(
        val calculation: License.LicenseRequirementsCalculationResult
    ) : LicenseGrantResult
}
