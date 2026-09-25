package dev.slne.surf.roleplay.api.common.user

import dev.slne.surf.api.core.util.requiredService
import java.util.*

private val manager = requiredService<UserManager>()

interface UserManager {
    suspend fun findByUuid(uuid: UUID): RoleplayUser?
    suspend fun findOrCreateByUuid(uuid: UUID): RoleplayUser

    companion object : UserManager by manager {
        val INSTANCE get() = manager
    }
}