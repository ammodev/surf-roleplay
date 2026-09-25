package dev.slne.surf.roleplay.core.client.common.user

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.auto.service.AutoService
import dev.slne.surf.roleplay.api.common.user.RoleplayUser
import dev.slne.surf.roleplay.api.common.user.UserManager
import dev.slne.surf.roleplay.core.client.common.proxies.userProxy
import dev.slne.surf.roleplay.core.common.user.CoreRoleplayUser
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import dev.slne.surf.roleplay.core.common.user.rpc.toDomain
import java.util.*

/**
 * The client-side [UserManager], which loads users through the remote user service and keeps
 * the users of players on this server in memory until they are evicted.
 *
 * Only [loadAndCache] puts users into memory. [findByUuid] and [findOrCreateByUuid] return the
 * in-memory instance if one exists and otherwise return a freshly loaded instance that is not
 * kept. At most one in-memory instance exists per player UUID: when two [loadAndCache] calls for
 * the same user race, the instance cached first is returned to both callers.
 *
 * @param serviceProvider supplies the remote user service users are loaded from and that loaded
 *        users send their write operations to; it is called on every load
 */
@AutoService(UserManager::class)
class CoreClientUserManager internal constructor(
    private val serviceProvider: () -> UserService
) : UserManager {

    /**
     * Creates a user manager backed by the RabbitMQ user service proxy of this client.
     */
    constructor() : this({ userProxy })

    private val cache = Caffeine.newBuilder()
        .build<UUID, CoreRoleplayUser>()

    /**
     * Returns the in-memory user with [uuid], or loads it through the remote user service without
     * keeping it in memory.
     *
     * @param uuid the UUID of the player
     * @return the user, or `null` if no user with [uuid] exists
     */
    override suspend fun findByUuid(uuid: UUID): RoleplayUser? {
        cache.getIfPresent(uuid)?.let { return it }

        val service = serviceProvider()
        return service.findByUuid(uuid)?.toDomain(service)
    }

    /**
     * Returns the in-memory user with [uuid], or loads it through the remote user service without
     * keeping it in memory, creating a user without identities if none exists.
     *
     * @param uuid the UUID of the player
     * @return the existing or newly created user
     */
    override suspend fun findOrCreateByUuid(uuid: UUID): RoleplayUser {
        cache.getIfPresent(uuid)?.let { return it }

        return load(uuid)
    }

    /**
     * Loads the user with [uuid] through the remote user service, creating a user without
     * identities if none exists, and keeps it in memory until [evict] is called for [uuid].
     *
     * The user is always loaded, even if one is already in memory; if a user is in memory by the
     * time the load completes, that instance is kept and returned instead of the loaded one.
     *
     * @param uuid the UUID of the player
     * @return the user held in memory under [uuid] afterwards
     */
    suspend fun loadAndCache(uuid: UUID): RoleplayUser {
        val loaded = load(uuid)
        return cache.asMap().putIfAbsent(uuid, loaded) ?: loaded
    }

    /**
     * Removes the user with [uuid] from memory, so that the next lookup loads it again.
     *
     * Does nothing if no user with [uuid] is in memory.
     *
     * @param uuid the UUID of the player
     */
    fun evict(uuid: UUID) {
        cache.invalidate(uuid)
    }

    /**
     * Finds or creates the user with [uuid] through the remote user service and maps it to its
     * in-memory form, without keeping it in memory.
     *
     * @param uuid the UUID of the player
     * @return the loaded user
     */
    private suspend fun load(uuid: UUID): CoreRoleplayUser {
        val service = serviceProvider()
        return service.findOrCreateByUuid(uuid).toDomain(service)
    }
}
