package dev.slne.surf.roleplay.api.common.license.user

import dev.slne.surf.roleplay.api.common.license.LicenseRegistry
import dev.slne.surf.roleplay.api.common.license.revoke.LicenseRevokedReason
import dev.slne.surf.roleplay.api.common.user.UserManager
import java.time.OffsetDateTime
import java.util.*

data class UserLicense(
    val licenseUuid: UUID,
    val acquiredAt: OffsetDateTime,

    val revokedByUuid: UUID? = null,
    val revokedReason: LicenseRevokedReason? = null,
    val revokedAt: OffsetDateTime? = null
) {
    fun license() = LicenseRegistry.getByUuid(licenseUuid)
    suspend fun revokedBy() = revokedByUuid?.let { UserManager.findByUuid(it) }
}
