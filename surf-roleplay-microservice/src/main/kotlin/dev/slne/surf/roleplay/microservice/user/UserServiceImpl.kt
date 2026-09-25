package dev.slne.surf.roleplay.microservice.user

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.EntityID
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.ExposedR2dbcException
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.utils.isDataIntegrityViolation
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayUserDto
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import dev.slne.surf.roleplay.microservice.user.db.UserRepository
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Serves the [UserService] from the roleplay database.
 *
 * Every operation runs in a single transaction. Write operations create the user if it does not
 * exist yet and return the user's state as stored after the change. A write that names an identity
 * the user does not own changes nothing and returns the user's current state.
 */
object UserServiceImpl : UserService {

    /**
     * Loads the user with [uuid] with every identity, qualification and license it holds.
     *
     * @param uuid the UUID of the player
     * @return the user, or `null` if no user with [uuid] exists
     */
    override suspend fun findByUuid(uuid: SerializableUUID): RoleplayUserDto? = suspendTransaction {
        UserRepository.findUserId(uuid)?.let { UserRepository.loadUser(it, uuid) }
    }

    /**
     * Loads the user with [uuid], inserting a user without identities if none exists.
     *
     * @param uuid the UUID of the player
     * @return the existing or newly created user
     */
    override suspend fun findOrCreateByUuid(uuid: SerializableUUID): RoleplayUserDto = suspendTransaction {
        UserRepository.loadUser(UserRepository.findOrCreateUserId(uuid), uuid)
    }

    /**
     * Creates an identity of [type] with a newly generated UUID for the user with [userUuid].
     *
     * @param userUuid the UUID of the player who owns the new identity
     * @param type the name of the identity type
     * @param accountId the identifier of the transaction account the new identity owns
     * @return the updated user, or `null` if the user already owns an identity of [type]
     */
    override suspend fun createIdentity(
        userUuid: SerializableUUID,
        type: String,
        accountId: SerializableUUID
    ): RoleplayUserDto? = try {
        suspendTransaction {
            val userId = UserRepository.findOrCreateUserId(userUuid)
            if (UserRepository.hasIdentityOfType(userId, type)) return@suspendTransaction null

            UserRepository.insertIdentity(userId, UUID.randomUUID(), type, accountId)
            UserRepository.loadUser(userId, userUuid)
        }
    } catch (e: ExposedR2dbcException) {
        if (!e.isDataIntegrityViolation()) throw e
        null
    }

    /**
     * Deletes the identity with [identityUuid] of the user with [userUuid] together with its
     * qualifications and licenses.
     *
     * @param userUuid the UUID of the player who owns the identity
     * @param identityUuid the UUID of the identity to delete
     * @return the updated user
     */
    override suspend fun deleteIdentity(
        userUuid: SerializableUUID,
        identityUuid: SerializableUUID
    ): RoleplayUserDto = changeIdentity(userUuid, identityUuid) { identityId ->
        UserRepository.deleteIdentity(identityId)
    }

    /**
     * Grants the license with [licenseKey] to the identity with [identityUuid], recording the
     * current time as the grant time.
     *
     * Nothing changes if the identity already holds an unrevoked license with [licenseKey].
     *
     * @param userUuid the UUID of the player who owns the identity
     * @param identityUuid the UUID of the identity receiving the license
     * @param licenseKey the string form of the key of the license definition
     * @param grantedByUuid the UUID of the granting player, or `null` to record no granting player
     * @return the updated user
     */
    override suspend fun grantLicense(
        userUuid: SerializableUUID,
        identityUuid: SerializableUUID,
        licenseKey: String,
        grantedByUuid: SerializableUUID?
    ): RoleplayUserDto = changeIdentity(userUuid, identityUuid) { identityId ->
        UserRepository.grantLicense(identityId, licenseKey, grantedByUuid, OffsetDateTime.now())
    }

    /**
     * Revokes the unrevoked license with [licenseKey] of the identity with [identityUuid],
     * recording the current time as the revocation time.
     *
     * Nothing changes if the identity holds no unrevoked license with [licenseKey].
     *
     * @param userUuid the UUID of the player who owns the identity
     * @param identityUuid the UUID of the identity holding the license
     * @param licenseKey the string form of the key of the license definition
     * @param revokedByUuid the UUID of the revoking player
     * @param reason the name of the revocation reason
     * @return the updated user
     */
    override suspend fun revokeLicense(
        userUuid: SerializableUUID,
        identityUuid: SerializableUUID,
        licenseKey: String,
        revokedByUuid: SerializableUUID,
        reason: String
    ): RoleplayUserDto = changeIdentity(userUuid, identityUuid) { identityId ->
        UserRepository.revokeLicense(identityId, licenseKey, revokedByUuid, reason, OffsetDateTime.now())
    }

    /**
     * Sets the rank of the identity with [identityUuid] to [rankKey].
     *
     * @param userUuid the UUID of the player who owns the identity
     * @param identityUuid the UUID of the identity whose rank changes
     * @param rankKey the string form of the key of the new rank
     * @return the updated user
     */
    override suspend fun setRank(
        userUuid: SerializableUUID,
        identityUuid: SerializableUUID,
        rankKey: String
    ): RoleplayUserDto = changeIdentity(userUuid, identityUuid) { identityId ->
        UserRepository.setRank(identityId, rankKey)
    }

    /**
     * Adds the qualification with [qualificationKey] to the identity with [identityUuid].
     *
     * Nothing changes if the identity already holds the qualification.
     *
     * @param userUuid the UUID of the player who owns the identity
     * @param identityUuid the UUID of the identity receiving the qualification
     * @param qualificationKey the string form of the key of the qualification
     * @return the updated user
     */
    override suspend fun addQualification(
        userUuid: SerializableUUID,
        identityUuid: SerializableUUID,
        qualificationKey: String
    ): RoleplayUserDto = changeIdentity(userUuid, identityUuid) { identityId ->
        UserRepository.addQualification(identityId, qualificationKey)
    }

    /**
     * Removes the qualification with [qualificationKey] from the identity with [identityUuid].
     *
     * @param userUuid the UUID of the player who owns the identity
     * @param identityUuid the UUID of the identity losing the qualification
     * @param qualificationKey the string form of the key of the qualification
     * @return the updated user
     */
    override suspend fun removeQualification(
        userUuid: SerializableUUID,
        identityUuid: SerializableUUID,
        qualificationKey: String
    ): RoleplayUserDto = changeIdentity(userUuid, identityUuid) { identityId ->
        UserRepository.removeQualification(identityId, qualificationKey)
    }

    /**
     * Applies [change] to the identity with [identityUuid] of the user with [userUuid] in a single
     * transaction, creating the user if it does not exist.
     *
     * [change] is skipped if the user owns no identity with [identityUuid].
     *
     * @param userUuid the UUID of the player who owns the identity
     * @param identityUuid the UUID of the identity to change
     * @param change the write applied to the identity, given the identity's database id
     * @return the user's state after the change
     */
    private suspend fun changeIdentity(
        userUuid: UUID,
        identityUuid: UUID,
        change: suspend (identityId: EntityID<ULong>) -> Unit
    ): RoleplayUserDto = suspendTransaction {
        val userId = UserRepository.findOrCreateUserId(userUuid)
        UserRepository.findIdentityId(userId, identityUuid)?.let { change(it) }
        UserRepository.loadUser(userId, userUuid)
    }
}
