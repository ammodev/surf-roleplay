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
import dev.slne.surf.roleplay.core.common.identity.account.IdentityAccountResolver
import dev.slne.surf.roleplay.core.common.user.CoreRoleplayUser
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key

/**
 * Builds the in-memory user described by this DTO.
 *
 * Identities whose type names no known
 * [IdentityType][dev.slne.surf.roleplay.api.common.identity.IdentityType] are left out.
 *
 * @param service the remote user service the user and its identities send write operations to
 * @param accountResolver finds or creates the transaction accounts of identities the user creates
 * @return the user with every identity of a known type
 */
fun RoleplayUserDto.toDomain(
    service: UserService,
    accountResolver: IdentityAccountResolver = IdentityAccountResolver.transactionBacked()
): CoreRoleplayUser = CoreRoleplayUser(uuid, service, accountResolver).also { it.applyState(this) }

/**
 * Builds the in-memory identity described by this DTO, owned by [owner].
 *
 * A missing, unknown or malformed rank key resolves to the lowest rank of the identity's
 * organisation.
 * Unknown or malformed qualification keys are left out, and so are licenses whose key is
 * malformed.
 *
 * @param owner the user who owns the identity
 * @return the identity, or `null` if [RoleplayIdentityDto.type] names no known identity type
 */
fun RoleplayIdentityDto.toDomain(owner: CoreRoleplayUser): CoreRoleplayIdentity? =
    when (identityTypeOrNull() ?: return null) {
        IdentityType.CIVILIAN -> CoreCivilianIdentity(
            uuid = uuid,
            owner = owner,
            accountId = accountId,
            licenses = userLicenses()
        )

        IdentityType.POLICE -> CorePoliceIdentity(
            uuid = uuid,
            owner = owner,
            accountId = accountId,
            licenses = userLicenses(),
            rank = policeRank(),
            qualifications = policeQualifications()
        )

        IdentityType.SAR -> CoreSarIdentity(
            uuid = uuid,
            owner = owner,
            accountId = accountId,
            licenses = userLicenses(),
            rank = sarRank(),
            qualifications = sarQualifications()
        )
    }

/**
 * Resolves the identity type named by [RoleplayIdentityDto.type].
 *
 * @return the identity type, or `null` if the name matches no known identity type
 */
fun RoleplayIdentityDto.identityTypeOrNull(): IdentityType? =
    IdentityType.entries.firstOrNull { it.name == type }

/**
 * Builds the held licenses of this DTO, leaving out licenses whose key is malformed.
 *
 * @return the held licenses in the order of [RoleplayIdentityDto.licenses]
 */
fun RoleplayIdentityDto.userLicenses(): List<UserLicense> = licenses.mapNotNull { it.toDomain() }

/**
 * Resolves the police rank named by [RoleplayIdentityDto.rankKey].
 *
 * @return the rank, or the lowest police rank if the key is missing, unknown or malformed
 */
fun RoleplayIdentityDto.policeRank(): PoliceRank =
    rankKey?.toKeyOrNull()?.let(PoliceRank::byKey) ?: PoliceRank.entries.first()

/**
 * Resolves the police qualifications named by [RoleplayIdentityDto.qualificationKeys].
 *
 * @return the known qualifications, leaving out unknown or malformed keys
 */
fun RoleplayIdentityDto.policeQualifications(): List<PoliceQualification> =
    qualificationKeys.mapNotNull { it.toKeyOrNull()?.let(PoliceQualification::byKey) }

/**
 * Resolves the search-and-rescue rank named by [RoleplayIdentityDto.rankKey].
 *
 * @return the rank, or the lowest search-and-rescue rank if the key is missing, unknown or
 *         malformed
 */
fun RoleplayIdentityDto.sarRank(): SarRank =
    rankKey?.toKeyOrNull()?.let(SarRank::byKey) ?: SarRank.entries.first()

/**
 * Resolves the search-and-rescue qualifications named by [RoleplayIdentityDto.qualificationKeys].
 *
 * @return the known qualifications, leaving out unknown or malformed keys
 */
fun RoleplayIdentityDto.sarQualifications(): List<SarQualification> =
    qualificationKeys.mapNotNull { it.toKeyOrNull()?.let(SarQualification::byKey) }

/**
 * Builds the held license described by this DTO.
 *
 * A revocation reason that names no known [LicenseRevokedReason] resolves to `null`; the license
 * still counts as revoked when [UserLicenseDto.revokedAt] is set.
 *
 * @return the held license with every field of this DTO, or `null` if
 *         [UserLicenseDto.licenseKey] is not a valid key
 */
fun UserLicenseDto.toDomain(): UserLicense? {
    val key = licenseKey.toKeyOrNull() ?: return null

    return UserLicense(
        licenseKey = key,
        acquiredAt = acquiredAt,
        grantedByUuid = grantedByUuid,
        revokedByUuid = revokedByUuid,
        revokedReason = revokedReason?.let { name -> LicenseRevokedReason.entries.firstOrNull { it.name == name } },
        revokedAt = revokedAt
    )
}

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
