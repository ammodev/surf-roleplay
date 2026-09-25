package dev.slne.surf.roleplay.api.common.user

import dev.slne.surf.api.core.util.requiredService
import java.util.*

private val manager = requiredService<UserManager>()

/**
 * Resolves and creates [RoleplayUser] instances by player UUID.
 */
interface UserManager {
    /**
     * Resolves the user with [uuid], or `null` if none is currently loaded.
     */
    suspend fun findByUuid(uuid: UUID): RoleplayUser?

    /**
     * Resolves the user with [uuid], creating it if it does not yet exist.
     */
    suspend fun findOrCreateByUuid(uuid: UUID): RoleplayUser

    /** Provides access to the single registered [UserManager] service. */
    companion object : UserManager by manager {
        /** The single registered [UserManager] service instance. */
        val INSTANCE get() = manager
    }
}
