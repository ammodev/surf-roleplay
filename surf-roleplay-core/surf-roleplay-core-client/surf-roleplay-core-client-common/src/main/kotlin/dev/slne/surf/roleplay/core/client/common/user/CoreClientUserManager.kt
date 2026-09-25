package dev.slne.surf.roleplay.core.client.common.user

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.auto.service.AutoService
import dev.slne.surf.roleplay.api.common.user.RoleplayUser
import dev.slne.surf.roleplay.api.common.user.UserManager
import dev.slne.surf.roleplay.core.client.common.proxies.userProxy
import dev.slne.surf.roleplay.core.common.user.CoreRoleplayUser
import java.util.*

@AutoService(UserManager::class)
class CoreClientUserManager : UserManager {
    private val cache = Caffeine.newBuilder()
        .build<UUID, CoreRoleplayUser>()

    override suspend fun findByUuid(uuid: UUID): RoleplayUser? {
        val cacheHit = cache.getIfPresent(uuid)

        if (cacheHit != null) {
            return cacheHit
        }

        return userProxy.findByUuid(uuid)?.let { dto ->
            TODO("")
        }
    }

    override suspend fun findOrCreateByUuid(uuid: UUID): RoleplayUser {
        val cacheHit = cache.getIfPresent(uuid)

        if (cacheHit != null) {
            return cacheHit
        }

        return userProxy.findOrCreateByUuid(uuid).let { dto ->
            throw NotImplementedError("Not implemented yet")
        }
    }
}