package dev.slne.surf.roleplay.core.common.user.rpc

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.rabbitmq.api.rpc.RpcService

/**
 * The remote contract for reading and changing roleplay users and their identities.
 *
 * Every write operation returns the complete state of the affected user after the change.
 * Timestamps of granted and revoked licenses are set by the implementing side.
 */
@RpcService
interface UserService {
    /**
     * Loads the user with [uuid].
     *
     * @param uuid the UUID of the player
     * @return the user, or `null` if no user with [uuid] exists
     */
    suspend fun findByUuid(uuid: SerializableUUID): RoleplayUserDto?

    /**
     * Loads the user with [uuid], creating a user without identities if none exists.
     *
     * @param uuid the UUID of the player
     * @return the existing or newly created user
     */
    suspend fun findOrCreateByUuid(uuid: SerializableUUID): RoleplayUserDto

    /**
     * Creates an identity of [type] for the user with [userUuid], owning the account [accountId].
     *
     * The new identity holds no qualifications and no licenses.
     *
     * @param userUuid the UUID of the player who owns the new identity
     * @param type the name of the identity's
     *        [IdentityType][dev.slne.surf.roleplay.api.common.identity.IdentityType]
     * @param accountId the identifier of the transaction account the new identity owns
     * @return the updated user, or `null` if the user already owns an identity of [type]
     */
    suspend fun createIdentity(
        userUuid: SerializableUUID,
        type: String,
        accountId: SerializableUUID
    ): RoleplayUserDto?

    /**
     * Permanently deletes the identity with [identityUuid] from the user with [userUuid].
     *
     * @param userUuid the UUID of the player who owns the identity
     * @param identityUuid the UUID of the identity to delete
     * @return the updated user
     */
    suspend fun deleteIdentity(
        userUuid: SerializableUUID,
        identityUuid: SerializableUUID
    ): RoleplayUserDto

    /**
     * Records the license with [licenseKey] as granted to the identity with [identityUuid].
     *
     * The grant time is set to the moment the grant is stored.
     *
     * @param userUuid the UUID of the player who owns the identity
     * @param identityUuid the UUID of the identity receiving the license
     * @param licenseKey the string form of the key of the license definition
     * @param grantedByUuid the UUID of the player granting the license, or `null` to record no
     *        granting player
     * @return the updated user
     */
    suspend fun grantLicense(
        userUuid: SerializableUUID,
        identityUuid: SerializableUUID,
        licenseKey: String,
        grantedByUuid: SerializableUUID?
    ): RoleplayUserDto

    /**
     * Marks the unrevoked license with [licenseKey] held by the identity with [identityUuid] as
     * revoked.
     *
     * The revocation time is set to the moment the revocation is stored.
     *
     * @param userUuid the UUID of the player who owns the identity
     * @param identityUuid the UUID of the identity holding the license
     * @param licenseKey the string form of the key of the license definition
     * @param revokedByUuid the UUID of the player revoking the license
     * @param reason the name of the
     *        [LicenseRevokedReason][dev.slne.surf.roleplay.api.common.license.revoke.LicenseRevokedReason]
     *        the license is revoked for
     * @return the updated user
     */
    suspend fun revokeLicense(
        userUuid: SerializableUUID,
        identityUuid: SerializableUUID,
        licenseKey: String,
        revokedByUuid: SerializableUUID,
        reason: String
    ): RoleplayUserDto

    /**
     * Sets the rank of the identity with [identityUuid] to the rank with [rankKey].
     *
     * @param userUuid the UUID of the player who owns the identity
     * @param identityUuid the UUID of the identity whose rank changes
     * @param rankKey the string form of the key of the new rank
     * @return the updated user
     */
    suspend fun setRank(
        userUuid: SerializableUUID,
        identityUuid: SerializableUUID,
        rankKey: String
    ): RoleplayUserDto

    /**
     * Adds the qualification with [qualificationKey] to the identity with [identityUuid].
     *
     * @param userUuid the UUID of the player who owns the identity
     * @param identityUuid the UUID of the identity receiving the qualification
     * @param qualificationKey the string form of the key of the qualification
     * @return the updated user
     */
    suspend fun addQualification(
        userUuid: SerializableUUID,
        identityUuid: SerializableUUID,
        qualificationKey: String
    ): RoleplayUserDto

    /**
     * Removes the qualification with [qualificationKey] from the identity with [identityUuid].
     *
     * @param userUuid the UUID of the player who owns the identity
     * @param identityUuid the UUID of the identity losing the qualification
     * @param qualificationKey the string form of the key of the qualification
     * @return the updated user
     */
    suspend fun removeQualification(
        userUuid: SerializableUUID,
        identityUuid: SerializableUUID,
        qualificationKey: String
    ): RoleplayUserDto
}
