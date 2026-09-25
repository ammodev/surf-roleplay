package dev.slne.surf.roleplay.core.common.user.rpc

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable

/**
 * The wire representation of a single roleplay identity.
 *
 * @property uuid the unique identifier of the identity
 * @property type the name of the identity's
 *           [IdentityType][dev.slne.surf.roleplay.api.common.identity.IdentityType]
 * @property accountId the identifier of the transaction account owned by the identity
 * @property rankKey the string form of the key of the rank the identity holds, or `null` if
 *           the identity holds no rank
 * @property qualificationKeys the string forms of the keys of the qualifications the identity holds
 * @property licenses the licenses currently or formerly held by the identity
 */
@Serializable
data class RoleplayIdentityDto(
    val uuid: SerializableUUID,
    val type: String,
    val accountId: SerializableUUID,
    val rankKey: String?,
    val qualificationKeys: List<String>,
    val licenses: List<UserLicenseDto>
)
