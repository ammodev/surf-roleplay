package dev.slne.surf.roleplay.api.common.user

import dev.slne.surf.api.core.util.requiredService
import java.util.*

private val manager = requiredService<UserManager>()

/**
 * Resolves and creates [RoleplayUser] instances by player UUID.
 */
interface UserManager {
    /**
     * Returns the user with [uuid].
     *
     * If the user is held in memory, for example because the player is connected to this
     * server, the held instance is returned. Otherwise the stored user is loaded from the remote
     * user service as a separate instance that is not kept in memory, so its active identity is
     * not retained between calls.
     *
     * @param uuid the UUID of the player
     * @return the user, or `null` if no user with [uuid] is stored
     */
    suspend fun findByUuid(uuid: UUID): RoleplayUser?

    /**
     * Returns the user with [uuid], creating a user without identities if none is stored.
     *
     * If the user is held in memory, for example because the player is connected to this
     * server, the held instance is returned. Otherwise the stored user is loaded, or created,
     * through the remote user service as a separate instance that is not kept in memory, so its
     * active identity is not retained between calls.
     *
     * @param uuid the UUID of the player
     * @return the held, loaded or newly created user
     */
    suspend fun findOrCreateByUuid(uuid: UUID): RoleplayUser

    /** Provides access to the single registered [UserManager] service. */
    companion object : UserManager by manager {
        /** The single registered [UserManager] service instance. */
        val INSTANCE get() = manager
    }
}
