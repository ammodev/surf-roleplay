package dev.slne.surf.roleplay.core.common.user.rpc

import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceQualification
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceRank
import dev.slne.surf.roleplay.api.common.identity.identities.sar.SarQualification
import dev.slne.surf.roleplay.api.common.identity.identities.sar.SarRank
import dev.slne.surf.roleplay.api.common.license.revoke.LicenseRevokedReason
import dev.slne.surf.roleplay.api.common.license.user.UserLicense
import dev.slne.surf.roleplay.core.common.identity.CoreCivilianIdentity
import dev.slne.surf.roleplay.core.common.identity.CorePoliceIdentity
import dev.slne.surf.roleplay.core.common.identity.CoreRoleplayIdentity
import dev.slne.surf.roleplay.core.common.identity.CoreSarIdentity
import dev.slne.surf.roleplay.core.common.user.CoreRoleplayUser
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import java.util.*

/**
 * Builds the in-memory user described by this DTO.
 *
 * Identities whose type names no known
 * [IdentityType][dev.slne.surf.roleplay.api.common.identity.IdentityType] are left out.
 *
 * @param service the remote user service the user and its identities send write operations to
 * @return the user with every identity of a known type
 */
fun RoleplayUserDto.toDomain(service: UserService): CoreRoleplayUser = CoreRoleplayUser(
    uuid = uuid,
    identities = identities.mapNotNull { it.toDomain(uuid, service) },
    service = service
)

/**
 * Builds the in-memory identity described by this DTO.
 *
 * A missing, unknown or malformed rank key resolves to the lowest rank of the identity's
 * organisation.
 * Unknown or malformed qualification keys are left out.
 *
 * @param userUuid the UUID of the player who owns the identity
 * @param service the remote user service the identity sends write operations to
 * @return the identity, or `null` if [RoleplayIdentityDto.type] names no known identity type
 */
fun RoleplayIdentityDto.toDomain(userUuid: UUID, service: UserService): CoreRoleplayIdentity? {
    val identityType = IdentityType.entries.firstOrNull { it.name == type } ?: return null
    val userLicenses = licenses.map { it.toDomain() }

    return when (identityType) {
        IdentityType.CIVILIAN -> CoreCivilianIdentity(
            uuid = uuid,
            userUuid = userUuid,
            accountId = accountId,
            licenses = userLicenses,
            service = service
        )

        IdentityType.POLICE -> CorePoliceIdentity(
            uuid = uuid,
            userUuid = userUuid,
            accountId = accountId,
            licenses = userLicenses,
            rank = rankKey?.toKeyOrNull()?.let(PoliceRank::byKey) ?: PoliceRank.entries.first(),
            qualifications = qualificationKeys.mapNotNull { it.toKeyOrNull()?.let(PoliceQualification::byKey) },
            service = service
        )

        IdentityType.SAR -> CoreSarIdentity(
            uuid = uuid,
            userUuid = userUuid,
            accountId = accountId,
            licenses = userLicenses,
            rank = rankKey?.toKeyOrNull()?.let(SarRank::byKey) ?: SarRank.entries.first(),
            qualifications = qualificationKeys.mapNotNull { it.toKeyOrNull()?.let(SarQualification::byKey) },
            service = service
        )
    }
}

/**
 * Builds the held license described by this DTO.
 *
 * A revocation reason that names no known [LicenseRevokedReason] resolves to `null`; the license
 * still counts as revoked when [UserLicenseDto.revokedAt] is set.
 *
 * @return the held license with every field of this DTO
 */
fun UserLicenseDto.toDomain(): UserLicense = UserLicense(
    licenseKey = Key.key(licenseKey),
    acquiredAt = acquiredAt,
    grantedByUuid = grantedByUuid,
    revokedByUuid = revokedByUuid,
    revokedReason = revokedReason?.let { name -> LicenseRevokedReason.entries.firstOrNull { it.name == name } },
    revokedAt = revokedAt
)

/**
 * Parses this string as an Adventure [Key].
 *
 * @return the parsed key, or `null` if this string is not a valid key
 */
private fun String.toKeyOrNull(): Key? = try {
    Key.key(this)
} catch (_: InvalidKeyException) {
    null
}
