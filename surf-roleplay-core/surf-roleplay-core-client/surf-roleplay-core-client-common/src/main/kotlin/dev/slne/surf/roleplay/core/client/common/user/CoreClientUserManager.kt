package dev.slne.surf.roleplay.core.client.common.user

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.auto.service.AutoService
import dev.slne.surf.roleplay.api.common.user.RoleplayUser
import dev.slne.surf.roleplay.api.common.user.UserManager
import dev.slne.surf.roleplay.core.client.common.proxies.userProxy
import dev.slne.surf.roleplay.core.common.user.CoreRoleplayUser
import dev.slne.surf.roleplay.core.common.user.rpc.toDomain
import java.util.*

/**
 * The client-side [UserManager], which loads users through the remote user service and keeps
 * every loaded user in memory until it is evicted.
 *
 * At most one in-memory instance exists per player UUID: when two loads of the same user race,
 * the instance cached first is returned to both callers.
 */
@AutoService(UserManager::class)
class CoreClientUserManager : UserManager {
    private val cache = Caffeine.newBuilder()
        .build<UUID, CoreRoleplayUser>()

    /**
     * Returns the cached user with [uuid], or loads it through the remote user service and caches
     * it.
     *
     * @param uuid the UUID of the player
     * @return the user, or `null` if no user with [uuid] exists
     */
    override suspend fun findByUuid(uuid: UUID): RoleplayUser? {
        cache.getIfPresent(uuid)?.let { return it }

        val loaded = userProxy.findByUuid(uuid)?.toDomain(userProxy) ?: return null
        return cacheLoaded(uuid, loaded)
    }

    /**
     * Returns the cached user with [uuid], or loads it through the remote user service, creating
     * a user without identities if none exists, and caches it.
     *
     * @param uuid the UUID of the player
     * @return the existing or newly created user
     */
    override suspend fun findOrCreateByUuid(uuid: UUID): RoleplayUser {
        cache.getIfPresent(uuid)?.let { return it }

        return cacheLoaded(uuid, userProxy.findOrCreateByUuid(uuid).toDomain(userProxy))
    }

    /**
     * Removes the user with [uuid] from memory, so that the next lookup loads it again.
     *
     * Does nothing if no user with [uuid] is cached.
     *
     * @param uuid the UUID of the player
     */
    fun evict(uuid: UUID) {
        cache.invalidate(uuid)
    }

    /**
     * Caches [loaded] under [uuid] unless a user was cached under [uuid] in the meantime.
     *
     * @param uuid the UUID of the player
     * @param loaded the freshly loaded user
     * @return the user cached under [uuid] afterwards
     */
    private fun cacheLoaded(uuid: UUID, loaded: CoreRoleplayUser): CoreRoleplayUser =
        cache.asMap().putIfAbsent(uuid, loaded) ?: loaded
}
