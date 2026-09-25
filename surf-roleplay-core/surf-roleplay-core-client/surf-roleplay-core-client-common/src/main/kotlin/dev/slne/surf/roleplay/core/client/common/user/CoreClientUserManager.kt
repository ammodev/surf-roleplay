package dev.slne.surf.roleplay.core.client.common.user

import com.google.auto.service.AutoService
import dev.slne.surf.roleplay.api.common.user.RoleplayUser
import dev.slne.surf.roleplay.api.common.user.UserManager
import dev.slne.surf.roleplay.core.client.common.proxies.userProxy
import dev.slne.surf.roleplay.core.common.user.CoreRoleplayUser
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import dev.slne.surf.roleplay.core.common.user.rpc.toDomain
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * The client-side [UserManager], which loads users through the remote user service and keeps
 * the users of players on this server in memory while they are held.
 *
 * Only [loadAndCache] puts users into memory. Every [loadAndCache] call acquires one hold on the
 * user and every [release] call gives one back; the user stays in memory while at least one hold
 * remains. [findByUuid] and [findOrCreateByUuid] return the in-memory instance if one exists and
 * otherwise return a freshly loaded instance that is not kept. At most one in-memory instance
 * exists per player UUID, and it is shared by every holder.
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

    /**
     * An in-memory user together with the number of holds on it.
     *
     * @property user the in-memory user
     * @property holds the number of [loadAndCache] calls not yet matched by a [release] call;
     *           always at least 1
     */
    private data class Entry(val user: CoreRoleplayUser, val holds: Int)

    private val entries = ConcurrentHashMap<UUID, Entry>()

    /**
     * Returns the in-memory user with [uuid], or loads it through the remote user service without
     * keeping it in memory.
     *
     * @param uuid the UUID of the player
     * @return the user, or `null` if no user with [uuid] exists
     */
    override suspend fun findByUuid(uuid: UUID): RoleplayUser? {
        entries[uuid]?.let { return it.user }

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
        entries[uuid]?.let { return it.user }

        return load(uuid)
    }

    /**
     * Acquires one hold on the user with [uuid], loading it through the remote user service if it
     * is not in memory, and keeps it in memory until every hold is released.
     *
     * An in-memory user is reused without contacting the service. Otherwise the user is found or
     * created remotely; if another call put the user into memory while the load was running, that
     * instance is kept and the loaded one is discarded. No hold is acquired if the load fails.
     *
     * @param uuid the UUID of the player
     * @return the user held in memory under [uuid]
     */
    suspend fun loadAndCache(uuid: UUID): RoleplayUser {
        acquireCached(uuid)?.let { return it }

        val loaded = load(uuid)
        return entries.compute(uuid) { _, entry ->
            entry?.copy(holds = entry.holds + 1) ?: Entry(loaded, 1)
        }!!.user
    }

    /**
     * Gives back one hold on the user with [uuid], and removes the user from memory once no hold
     * remains.
     *
     * Does nothing if no user with [uuid] is in memory.
     *
     * @param uuid the UUID of the player
     */
    fun release(uuid: UUID) {
        entries.computeIfPresent(uuid) { _, entry ->
            if (entry.holds > 1) entry.copy(holds = entry.holds - 1) else null
        }
    }

    /**
     * Removes the user with [uuid] from memory regardless of how many holds remain, so that the
     * next lookup loads it again.
     *
     * Does nothing if no user with [uuid] is in memory.
     *
     * @param uuid the UUID of the player
     */
    fun evict(uuid: UUID) {
        entries.remove(uuid)
    }

    /**
     * Acquires one more hold on the in-memory user with [uuid], if there is one.
     *
     * @param uuid the UUID of the player
     * @return the in-memory user, or `null` if no user with [uuid] is in memory
     */
    private fun acquireCached(uuid: UUID): CoreRoleplayUser? =
        entries.computeIfPresent(uuid) { _, entry -> entry.copy(holds = entry.holds + 1) }?.user

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
