package dev.slne.surf.roleplay.core.common.user.rpc

import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable

/**
 * The wire representation of a license held by an identity, optionally revoked.
 *
 * Timestamps are transmitted with a precision of whole seconds.
 *
 * @property licenseKey the string form of the key of the license definition
 * @property acquiredAt the point in time the license was granted
 * @property grantedByUuid the UUID of the player who granted the license, or `null` if no granting
 *           player is recorded
 * @property revokedByUuid the UUID of the player who revoked the license, or `null` if it has not
 *           been revoked
 * @property revokedReason the name of the
 *           [LicenseRevokedReason][dev.slne.surf.roleplay.api.common.license.revoke.LicenseRevokedReason]
 *           the license was revoked for, or `null` if it has not been revoked
 * @property revokedAt the point in time the license was revoked, or `null` if it has not been revoked
 */
@Serializable
data class UserLicenseDto(
    val licenseKey: String,
    val acquiredAt: SerializableOffsetDateTime,
    val grantedByUuid: SerializableUUID?,
    val revokedByUuid: SerializableUUID?,
    val revokedReason: String?,
    val revokedAt: SerializableOffsetDateTime?
)
