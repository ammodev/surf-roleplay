package dev.slne.surf.roleplay.core.common.user

import dev.slne.surf.api.core.util.freeze
import dev.slne.surf.api.core.util.mutableObjectListOf
import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import dev.slne.surf.roleplay.api.common.identity.exceptions.UnknownIdentityException
import dev.slne.surf.roleplay.api.common.user.RoleplayUser
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import it.unimi.dsi.fastutil.objects.ObjectList
import java.util.*

/**
 * A roleplay user held in memory together with every identity it owns.
 *
 * No identity is active when a user is constructed.
 *
 * @param uuid the UUID of the player
 * @param identities every identity owned by the player
 * @param service the remote user service write operations are sent to
 */
class CoreRoleplayUser(
    override val uuid: UUID,
    identities: Collection<RoleplayIdentity>,
    private val service: UserService
) : RoleplayUser {
    private val _identities: ObjectList<RoleplayIdentity> = mutableObjectListOf(identities)

    /**
     * A read-only view of every identity owned by this user.
     */
    override val identities: ObjectList<RoleplayIdentity> get() = _identities.freeze()

    private var _activeIdentity: RoleplayIdentity? = null

    /**
     * The identity the player is currently playing as, or `null` if none is active.
     */
    override val activeIdentity: RoleplayIdentity? get() = _activeIdentity

    /**
     * Creates a new identity of [type] for this user through the remote user service, together
     * with the transaction account it owns.
     *
     * @param type the organisation the new identity belongs to
     * @return the created identity
     */
    override suspend fun createIdentity(type: IdentityType): RoleplayIdentity {
        TODO()
    }

    /**
     * Makes the owned identity with the UUID of [identity] the active identity of this user.
     *
     * @param identity the identity to activate
     * @throws UnknownIdentityException if this user owns no identity with the UUID of [identity]
     */
    override suspend fun setActiveIdentity(identity: RoleplayIdentity) {
        _activeIdentity = _identities.firstOrNull { it.uuid == identity.uuid }
            ?: throw UnknownIdentityException(uuid, identity.uuid)
    }

    /**
     * Deactivates the active identity, so that no identity is active afterwards.
     */
    override fun clearActiveIdentity() {
        _activeIdentity = null
    }

    /**
     * Permanently deletes [identity] and its transaction account through the remote user service.
     *
     * @param identity the identity to delete
     */
    override suspend fun deleteIdentity(identity: RoleplayIdentity) {
        TODO()
    }
}
