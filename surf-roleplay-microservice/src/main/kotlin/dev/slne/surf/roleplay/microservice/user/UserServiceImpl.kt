package dev.slne.surf.roleplay.microservice.user

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayUserDto
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import dev.slne.surf.roleplay.microservice.user.db.tables.RoleplayUsersTable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

object UserServiceImpl : UserService {
    override suspend fun findByUuid(
        uuid: SerializableUUID
    ): RoleplayUserDto? = suspendTransaction {
        return@suspendTransaction RoleplayUsersTable.selectAll()
            .where { RoleplayUsersTable.uuid eq uuid }
            .map { RoleplayUserDto(it[RoleplayUsersTable.uuid]) }
            .firstOrNull()
    }

    override suspend fun findOrCreateByUuid(
        uuid: SerializableUUID
    ): RoleplayUserDto = suspendTransaction {
        return@suspendTransaction findByUuid(uuid) ?: run {
            RoleplayUsersTable.insert {
                it[RoleplayUsersTable.uuid] = uuid
            }

            RoleplayUserDto(uuid)
        }
    }
}