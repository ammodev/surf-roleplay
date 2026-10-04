package dev.slne.surf.roleplay.core.common.user

import dev.slne.surf.api.core.util.freeze
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.core.util.mutableObjectListOf
import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import dev.slne.surf.roleplay.api.common.identity.exceptions.IdentityAlreadyExistsException
import dev.slne.surf.roleplay.api.common.identity.exceptions.UnknownIdentityException
import dev.slne.surf.roleplay.api.common.user.RoleplayUser
import dev.slne.surf.roleplay.core.common.identity.CoreRoleplayIdentity
import dev.slne.surf.roleplay.core.common.identity.account.IdentityAccountResolver
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayUserDto
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import dev.slne.surf.roleplay.core.common.user.rpc.identityTypeOrNull
import dev.slne.surf.roleplay.core.common.user.rpc.toDomain
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.result.AccountDeleteResult
import dev.slne.surf.transaction.api.user.TransactionUser
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

private val log = logger()

/**
 * A roleplay user held in memory together with every identity it owns.
 *
 * The user starts without identities and without an active identity; its identities are filled
 * in by [applyState]. Every write operation of the user and of its identities is sent to
 * [service] one at a time, and the complete state the service returns is applied in place.
 *
 * @param uuid the UUID of the player
 * @param service the remote user service write operations are sent to
 * @param accountResolver finds or creates the transaction account of each identity this user
 *        creates
 * @param accountDeleter deletes the transaction account with the given identifier on behalf of
 *        the given player, returning the result of the deletion or `null` if no such account
 *        exists
 */
class CoreRoleplayUser(
    override val uuid: UUID,
    internal val service: UserService,
    private val accountResolver: IdentityAccountResolver = IdentityAccountResolver.transactionBacked(),
    private val accountDeleter: suspend (owner: UUID, accountId: UUID) -> AccountDeleteResult? =
        ::deleteTransactionAccount
) : RoleplayUser {
    private val writeMutex = Mutex()

    @Volatile
    private var _identities: ObjectList<RoleplayIdentity> = mutableObjectListOf()

    /**
     * A read-only view of every identity owned by this user.
     */
    override val identities: ObjectList<RoleplayIdentity> get() = _identities.freeze()

    @Volatile
    private var _activeIdentity: RoleplayIdentity? = null

    /**
     * The identity the player is currently playing as, or `null` if none is active.
     */
    override val activeIdentity: RoleplayIdentity? get() = _activeIdentity

    /**
     * Runs [block] while holding this user's write lock, so that write operations of this user
     * and its identities never overlap.
     *
     * The lock is not reentrant: [block] must not start another write operation of this user.
     *
     * @param block the write operation to run
     * @return the result of [block]
     */
    internal suspend fun <T> write(block: suspend () -> T): T = writeMutex.withLock { block() }

    /**
     * Replaces the state of this user with the state described by [dto], keeping identity
     * instances wherever possible.
     *
     * An identity whose UUID and type are already held is updated in place; identities new to
     * [dto] are added and identities missing from it are removed. Identities of an unknown type
     * are left out. The active identity stays active if [dto] still contains its UUID; otherwise
     * no identity is active afterwards. Every [UserStateListener] is notified afterwards.
     *
     * @param dto the complete state of this user as returned by the remote user service
     */
    internal fun applyState(dto: RoleplayUserDto) {
        val current = _identities.filterIsInstance<CoreRoleplayIdentity>().associateBy { it.uuid }

        val next = dto.identities.mapNotNull { identityDto ->
            val type = identityDto.identityTypeOrNull() ?: return@mapNotNull null
            val existing = current[identityDto.uuid]

            if (existing != null && existing.type == type) {
                existing.applyState(identityDto)
                existing
            } else {
                identityDto.toDomain(this)
            }
        }

        _identities = mutableObjectListOf<RoleplayIdentity>(next)
        _activeIdentity = _activeIdentity?.let { active -> next.firstOrNull { it.uuid == active.uuid } }
        UserStateListeners.notifyChanged(uuid)
    }

    /**
     * Creates a new identity of [type] for this user through the remote user service.
     *
     * The transaction account the identity owns is reused or created by the account resolver
     * before the service is called. Nothing is resolved or sent if this user already owns an
     * identity of [type].
     *
     * @param type the organisation the new identity belongs to
     * @return the created identity
     * @throws IdentityAlreadyExistsException if this user already owns an identity of [type],
     *         either locally or according to the remote user service
     * @throws IllegalStateException if no account can be resolved, or if the returned state holds
     *         no identity of [type]
     */
    override suspend fun createIdentity(type: IdentityType): RoleplayIdentity = write {
        if (_identities.any { it.type == type }) throw IdentityAlreadyExistsException(uuid, type)

        val accountId = accountResolver.resolve(uuid, type)
        val dto = service.createIdentity(uuid, type.name, accountId)
            ?: throw IdentityAlreadyExistsException(uuid, type)

        applyState(dto)

        _identities.firstOrNull { it.type == type }
            ?: throw IllegalStateException("User $uuid holds no $type identity after creating it")
    }

    /**
     * Makes the owned identity with the UUID of [identity] the active identity of this user.
     *
     * The owned identity is looked up and activated while holding this user's write lock, so a
     * concurrent write operation cannot remove it in between. Every [UserStateListener] is
     * notified afterwards.
     *
     * @param identity the identity to activate
     * @throws UnknownIdentityException if this user owns no identity with the UUID of [identity]
     */
    override suspend fun setActiveIdentity(identity: RoleplayIdentity): Unit = write {
        _activeIdentity = _identities.firstOrNull { it.uuid == identity.uuid }
            ?: throw UnknownIdentityException(uuid, identity.uuid)
        UserStateListeners.notifyChanged(uuid)
    }

    /**
     * Deactivates the active identity, so that no identity is active afterwards, and notifies
     * every [UserStateListener].
     */
    override fun clearActiveIdentity() {
        _activeIdentity = null
        UserStateListeners.notifyChanged(uuid)
    }

    /**
     * Permanently deletes the owned identity with the UUID of [identity] through the remote user
     * service, then deletes its transaction account.
     *
     * Both steps run while holding this user's write lock, so no other write operation of this
     * user can start before the account is gone. The returned state is applied before the account
     * is deleted, so the identity is no longer active afterwards. A missing account or a failed
     * account deletion is logged as a warning and does not fail the operation.
     *
     * @param identity the identity to delete
     * @throws UnknownIdentityException if this user owns no identity with the UUID of [identity]
     */
    override suspend fun deleteIdentity(identity: RoleplayIdentity): Unit = write {
        val owned = _identities.firstOrNull { it.uuid == identity.uuid }
            ?: throw UnknownIdentityException(uuid, identity.uuid)

        applyState(service.deleteIdentity(uuid, owned.uuid))
        deleteAccount(owned)
    }

    /**
     * Deletes the transaction account of [identity] through the account deleter.
     *
     * A missing account, an unsuccessful deletion or an exception other than a cancellation is
     * logged as a warning instead of being thrown.
     *
     * @param identity the deleted identity whose account is deleted
     */
    private suspend fun deleteAccount(identity: RoleplayIdentity) {
        try {
            val result = accountDeleter(uuid, identity.accountId)
            if (result == null) {
                log.atWarning().log(
                    "Account %s of deleted identity %s of user %s does not exist",
                    identity.accountId, identity.uuid, uuid
                )
                return
            }

            if (result != AccountDeleteResult.SUCCESS) {
                log.atWarning().log(
                    "Could not delete account %s of deleted identity %s of user %s: %s",
                    identity.accountId, identity.uuid, uuid, result
                )
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            log.atWarning().withCause(exception).log(
                "Could not delete account %s of deleted identity %s of user %s",
                identity.accountId, identity.uuid, uuid
            )
        }
    }
}

/**
 * Deletes the transaction account with [accountId] through the transaction user of [owner].
 *
 * @param owner the UUID of the player on whose behalf the account is deleted
 * @param accountId the identifier of the account to delete
 * @return the result of the deletion, or `null` if no account with [accountId] exists
 */
private suspend fun deleteTransactionAccount(owner: UUID, accountId: UUID): AccountDeleteResult? {
    val account = Account.byId(accountId) ?: return null
    return TransactionUser.byUuid(owner).deleteAccount(account)
}
