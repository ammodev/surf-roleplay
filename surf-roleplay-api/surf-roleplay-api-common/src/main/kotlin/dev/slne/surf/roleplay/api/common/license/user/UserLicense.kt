package dev.slne.surf.roleplay.api.common.license.user

import dev.slne.surf.roleplay.api.common.license.License
import dev.slne.surf.roleplay.api.common.license.LicenseRegistry
import dev.slne.surf.roleplay.api.common.license.revoke.LicenseRevokedReason
import dev.slne.surf.roleplay.api.common.user.UserManager
import net.kyori.adventure.key.Key
import java.time.OffsetDateTime
import java.util.*

/**
 * A license held by a [RoleplayIdentity][dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity],
 * optionally revoked.
 *
 * @property licenseKey the key of the [License] definition this held license was granted for
 * @property acquiredAt the point in time the license was granted
 * @property grantedByUuid the UUID of the player who granted the license, or `null` if no granting
 *           player is recorded
 * @property revokedByUuid the UUID of the user who revoked the license, or `null` if it has not been revoked
 * @property revokedReason the reason the license was revoked, or `null` if it has not been revoked
 * @property revokedAt the point in time the license was revoked, or `null` if it has not been revoked
 */
data class UserLicense(
    val licenseKey: Key,
    val acquiredAt: OffsetDateTime,
    val grantedByUuid: UUID? = null,

    val revokedByUuid: UUID? = null,
    val revokedReason: LicenseRevokedReason? = null,
    val revokedAt: OffsetDateTime? = null
) {
    /** Whether this license has been revoked. */
    val isRevoked: Boolean get() = revokedAt != null

    /** Resolves the [License] definition this held license was granted for. */
    fun license(): License? = LicenseRegistry.getByKey(licenseKey)

    /** Resolves the user who revoked this license, or `null` if it has not been revoked. */
    suspend fun revokedBy() = revokedByUuid?.let { UserManager.findByUuid(it) }
}
