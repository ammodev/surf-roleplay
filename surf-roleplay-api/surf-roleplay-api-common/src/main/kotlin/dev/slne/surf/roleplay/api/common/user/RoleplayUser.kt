@file:Suppress("NonExtendableApiUsage")

package dev.slne.surf.roleplay.api.common.user

import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import dev.slne.surf.roleplay.api.common.identity.exceptions.IdentityAlreadyExistsException
import dev.slne.surf.roleplay.api.common.identity.exceptions.UnknownIdentityException
import dev.slne.surf.roleplay.api.common.transaction.RoleplayTransactional
import dev.slne.surf.transaction.api.transactional.Transactional
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.UnmodifiableView
import java.util.*

/**
 * A player taking part in roleplay.
 *
 * A user owns at most one identity per [IdentityType] and may have one of them active.
 * Transactional operations performed on the user are carried out by the active identity.
 */
interface RoleplayUser : RoleplayTransactional {
    /**
     * The UUID of the player.
     */
    val uuid: UUID

    /**
     * The identity the player is currently playing as, or `null` if none is active.
     */
    val activeIdentity: RoleplayIdentity?

    /**
     * Every identity owned by this user.
     */
    val identities: @UnmodifiableView ObjectList<RoleplayIdentity>

    /**
     * Creates a new identity of [type] for this user.
     *
     * The new identity is not made active.
     *
     * @param type the organisation the new identity belongs to
     * @return the created identity
     * @throws IdentityAlreadyExistsException if this user already owns an identity of [type]
     */
    suspend fun createIdentity(type: IdentityType): RoleplayIdentity

    /**
     * Makes [identity] the active identity of this user.
     *
     * @param identity the identity to activate
     * @throws UnknownIdentityException if [identity] does not belong to this user
     */
    suspend fun setActiveIdentity(identity: RoleplayIdentity)

    /**
     * Deactivates the active identity, so that no identity is active afterwards.
     *
     * Does nothing if no identity is active.
     */
    fun clearActiveIdentity()

    /**
     * Permanently deletes [identity] from this user, together with its transaction account.
     *
     * If [identity] was the active identity, no identity is active afterwards.
     *
     * @param identity the identity to delete
     * @throws UnknownIdentityException if [identity] does not belong to this user
     */
    suspend fun deleteIdentity(identity: RoleplayIdentity)

    /**
     * The active identity, which carries out every transactional operation on this user.
     */
    override val delegate: Transactional?
        get() = activeIdentity

    /**
     * The player's UUID in string form, used to name this user in error messages.
     */
    override val delegateName: String
        get() = uuid.toString()
}
