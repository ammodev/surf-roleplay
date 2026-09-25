package dev.slne.surf.roleplay.core.common.user.rpc

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable

@Serializable
data class RoleplayUserDto(
    val uuid: SerializableUUID
)
