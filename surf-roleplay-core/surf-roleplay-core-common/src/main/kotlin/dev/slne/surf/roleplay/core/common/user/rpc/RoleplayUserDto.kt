package dev.slne.surf.roleplay.core.common.user.rpc

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable

/**
 * The wire representation of a roleplay user together with every identity it owns.
 *
 * @property uuid the UUID of the player
 * @property identities every identity owned by the player
 */
@Serializable
data class RoleplayUserDto(
    val uuid: SerializableUUID,
    val identities: List<RoleplayIdentityDto>
)
