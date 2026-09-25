package dev.slne.surf.roleplay.core.common.user.rpc

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.rabbitmq.api.rpc.RpcService

@RpcService
interface UserService {
    suspend fun findByUuid(uuid: SerializableUUID): RoleplayUserDto?
    suspend fun findOrCreateByUuid(uuid: SerializableUUID): RoleplayUserDto
}