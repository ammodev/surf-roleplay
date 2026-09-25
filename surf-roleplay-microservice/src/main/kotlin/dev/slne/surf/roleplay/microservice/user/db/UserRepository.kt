package dev.slne.surf.roleplay.microservice.user.db

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.SortOrder
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.EntityID
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.inList
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.isNull
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.deleteWhere
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insertIgnore
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.select
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.update
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayIdentityDto
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayUserDto
import dev.slne.surf.roleplay.core.common.user.rpc.UserLicenseDto
import dev.slne.surf.roleplay.microservice.user.db.tables.RoleplayIdentitiesTable
import dev.slne.surf.roleplay.microservice.user.db.tables.RoleplayIdentityLicensesTable
import dev.slne.surf.roleplay.microservice.user.db.tables.RoleplayIdentityQualificationsTable
import dev.slne.surf.roleplay.microservice.user.db.tables.RoleplayUsersTable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Reads and writes roleplay users, their identities, qualifications and licenses.
 *
 * Every function must be called inside an open transaction.
 */
object UserRepository {

    /**
     * Finds the database id of the user with [uuid].
     *
     * @param uuid the UUID of the player
     * @return the id of the user, or `null` if no user with [uuid] exists
     */
    suspend fun findUserId(uuid: UUID): EntityID<ULong>? =
        RoleplayUsersTable.select(RoleplayUsersTable.id)
            .where { RoleplayUsersTable.uuid eq uuid }
            .map { it[RoleplayUsersTable.id] }
            .firstOrNull()

    /**
     * Finds the database id of the user with [uuid], inserting a user without identities if none
     * exists.
     *
     * @param uuid the UUID of the player
     * @return the id of the existing or newly inserted user
     */
    suspend fun findOrCreateUserId(uuid: UUID): EntityID<ULong> =
        findUserId(uuid) ?: RoleplayUsersTable.insertAndGetId { it[RoleplayUsersTable.uuid] = uuid }

    /**
     * Builds the complete state of the user with [userId], including every identity with its
     * qualifications and licenses.
     *
     * Identities are ordered by creation, qualifications in the order they were added and licenses in grant order.
     *
     * @param userId the database id of the user
     * @param uuid the UUID of the player the user belongs to
     * @return the user with every identity it owns
     */
    suspend fun loadUser(userId: EntityID<ULong>, uuid: UUID): RoleplayUserDto {
        val identityRows = RoleplayIdentitiesTable.selectAll()
            .where { RoleplayIdentitiesTable.user eq userId }
            .orderBy(RoleplayIdentitiesTable.id, SortOrder.ASC)
            .toList()

        if (identityRows.isEmpty()) return RoleplayUserDto(uuid, emptyList())

        val identityIds = identityRows.map { it[RoleplayIdentitiesTable.id] }
        val qualificationKeys = loadQualificationKeys(identityIds)
        val licenses = loadLicenses(identityIds)

        val identities = identityRows.map { row ->
            val identityId = row[RoleplayIdentitiesTable.id]

            RoleplayIdentityDto(
                uuid = row[RoleplayIdentitiesTable.uuid],
                type = row[RoleplayIdentitiesTable.type],
                accountId = row[RoleplayIdentitiesTable.accountId],
                rankKey = row[RoleplayIdentitiesTable.rankKey],
                qualificationKeys = qualificationKeys[identityId].orEmpty(),
                licenses = licenses[identityId].orEmpty()
            )
        }

        return RoleplayUserDto(uuid, identities)
    }

    /**
     * Loads the qualification keys held by the identities with [identityIds].
     *
     * @param identityIds the database ids of the identities
     * @return the qualification keys of each identity in the order they were added; identities without
     *         qualifications are absent
     */
    private suspend fun loadQualificationKeys(
        identityIds: List<EntityID<ULong>>
    ): Map<EntityID<ULong>, List<String>> =
        RoleplayIdentityQualificationsTable.selectAll()
            .where { RoleplayIdentityQualificationsTable.identity inList identityIds }
            .orderBy(RoleplayIdentityQualificationsTable.id, SortOrder.ASC)
            .toList()
            .groupBy(
                keySelector = { it[RoleplayIdentityQualificationsTable.identity] },
                valueTransform = { it[RoleplayIdentityQualificationsTable.qualificationKey] }
            )

    /**
     * Loads every license, revoked or not, granted to the identities with [identityIds].
     *
     * @param identityIds the database ids of the identities
     * @return the licenses of each identity in grant order; identities without licenses are absent
     */
    private suspend fun loadLicenses(
        identityIds: List<EntityID<ULong>>
    ): Map<EntityID<ULong>, List<UserLicenseDto>> =
        RoleplayIdentityLicensesTable.selectAll()
            .where { RoleplayIdentityLicensesTable.identity inList identityIds }
            .orderBy(RoleplayIdentityLicensesTable.id, SortOrder.ASC)
            .toList()
            .groupBy(
                keySelector = { it[RoleplayIdentityLicensesTable.identity] },
                valueTransform = { it.toLicenseDto() }
            )

    /**
     * Builds the license described by this row of [RoleplayIdentityLicensesTable].
     *
     * @return the license with every stored field
     */
    private fun ResultRow.toLicenseDto(): UserLicenseDto = UserLicenseDto(
        licenseKey = this[RoleplayIdentityLicensesTable.licenseKey],
        acquiredAt = this[RoleplayIdentityLicensesTable.acquiredAt],
        grantedByUuid = this[RoleplayIdentityLicensesTable.grantedBy],
        revokedByUuid = this[RoleplayIdentityLicensesTable.revokedBy],
        revokedReason = this[RoleplayIdentityLicensesTable.revokedReason],
        revokedAt = this[RoleplayIdentityLicensesTable.revokedAt]
    )

    /**
     * Finds the database id of the identity with [identityUuid] owned by the user with [userId].
     *
     * @param userId the database id of the owning user
     * @param identityUuid the UUID of the identity
     * @return the id of the identity, or `null` if no identity with [identityUuid] is owned by the
     *         user
     */
    suspend fun findIdentityId(userId: EntityID<ULong>, identityUuid: UUID): EntityID<ULong>? =
        RoleplayIdentitiesTable.select(RoleplayIdentitiesTable.id)
            .where { (RoleplayIdentitiesTable.uuid eq identityUuid) and (RoleplayIdentitiesTable.user eq userId) }
            .map { it[RoleplayIdentitiesTable.id] }
            .firstOrNull()

    /**
     * Checks whether the user with [userId] owns an identity of [type].
     *
     * @param userId the database id of the user
     * @param type the name of the identity type
     * @return `true` if the user owns an identity of [type]
     */
    suspend fun hasIdentityOfType(userId: EntityID<ULong>, type: String): Boolean =
        RoleplayIdentitiesTable.select(RoleplayIdentitiesTable.id)
            .where { (RoleplayIdentitiesTable.user eq userId) and (RoleplayIdentitiesTable.type eq type) }
            .limit(1)
            .firstOrNull() != null

    /**
     * Inserts an identity of [type] without rank, qualifications or licenses for the user with
     * [userId].
     *
     * @param userId the database id of the owning user
     * @param identityUuid the UUID of the new identity
     * @param type the name of the identity type
     * @param accountId the identifier of the transaction account the identity owns
     */
    suspend fun insertIdentity(userId: EntityID<ULong>, identityUuid: UUID, type: String, accountId: UUID) {
        RoleplayIdentitiesTable.insert {
            it[RoleplayIdentitiesTable.uuid] = identityUuid
            it[RoleplayIdentitiesTable.user] = userId
            it[RoleplayIdentitiesTable.type] = type
            it[RoleplayIdentitiesTable.accountId] = accountId
        }
    }

    /**
     * Deletes the identity with [identityId] together with its qualifications and licenses.
     *
     * @param identityId the database id of the identity
     */
    suspend fun deleteIdentity(identityId: EntityID<ULong>) {
        RoleplayIdentityQualificationsTable.deleteWhere { identity eq identityId }
        RoleplayIdentityLicensesTable.deleteWhere { identity eq identityId }
        RoleplayIdentitiesTable.deleteWhere { id eq identityId }
    }

    /**
     * Sets the rank of the identity with [identityId].
     *
     * @param identityId the database id of the identity
     * @param rankKey the string form of the key of the new rank
     */
    suspend fun setRank(identityId: EntityID<ULong>, rankKey: String) {
        RoleplayIdentitiesTable.update({ RoleplayIdentitiesTable.id eq identityId }) {
            it[RoleplayIdentitiesTable.rankKey] = rankKey
        }
    }

    /**
     * Adds the qualification with [qualificationKey] to the identity with [identityId], doing
     * nothing if the identity already holds it.
     *
     * @param identityId the database id of the identity
     * @param qualificationKey the string form of the key of the qualification
     */
    suspend fun addQualification(identityId: EntityID<ULong>, qualificationKey: String) {
        RoleplayIdentityQualificationsTable.insertIgnore {
            it[RoleplayIdentityQualificationsTable.identity] = identityId
            it[RoleplayIdentityQualificationsTable.qualificationKey] = qualificationKey
        }
    }

    /**
     * Removes the qualification with [qualificationKey] from the identity with [identityId].
     *
     * @param identityId the database id of the identity
     * @param qualificationKey the string form of the key of the qualification
     */
    suspend fun removeQualification(identityId: EntityID<ULong>, qualificationKey: String) {
        RoleplayIdentityQualificationsTable.deleteWhere {
            (identity eq identityId) and (RoleplayIdentityQualificationsTable.qualificationKey eq qualificationKey)
        }
    }

    /**
     * Records the license with [licenseKey] as granted to the identity with [identityId] at
     * [acquiredAt], doing nothing if the identity already holds an unrevoked license with that key.
     *
     * @param identityId the database id of the identity
     * @param licenseKey the string form of the key of the license definition
     * @param grantedBy the UUID of the granting player, or `null` to record no granting player
     * @param acquiredAt the point in time the license is granted
     */
    suspend fun grantLicense(
        identityId: EntityID<ULong>,
        licenseKey: String,
        grantedBy: UUID?,
        acquiredAt: OffsetDateTime
    ) {
        val alreadyHeld = RoleplayIdentityLicensesTable.select(RoleplayIdentityLicensesTable.id)
            .where { unrevokedLicense(identityId, licenseKey) }
            .limit(1)
            .firstOrNull() != null

        if (alreadyHeld) return

        RoleplayIdentityLicensesTable.insert {
            it[RoleplayIdentityLicensesTable.identity] = identityId
            it[RoleplayIdentityLicensesTable.licenseKey] = licenseKey
            it[RoleplayIdentityLicensesTable.acquiredAt] = acquiredAt
            it[RoleplayIdentityLicensesTable.grantedBy] = grantedBy
        }
    }

    /**
     * Marks the unrevoked license with [licenseKey] of the identity with [identityId] as revoked,
     * doing nothing if the identity holds no such license.
     *
     * @param identityId the database id of the identity
     * @param licenseKey the string form of the key of the license definition
     * @param revokedBy the UUID of the revoking player
     * @param reason the name of the revocation reason
     * @param revokedAt the point in time the license is revoked
     */
    suspend fun revokeLicense(
        identityId: EntityID<ULong>,
        licenseKey: String,
        revokedBy: UUID,
        reason: String,
        revokedAt: OffsetDateTime
    ) {
        RoleplayIdentityLicensesTable.update({ unrevokedLicense(identityId, licenseKey) }) {
            it[RoleplayIdentityLicensesTable.revokedBy] = revokedBy
            it[RoleplayIdentityLicensesTable.revokedReason] = reason
            it[RoleplayIdentityLicensesTable.revokedAt] = revokedAt
        }
    }

    /**
     * Builds the condition matching the unrevoked license rows with [licenseKey] of the identity
     * with [identityId].
     *
     * @param identityId the database id of the identity
     * @param licenseKey the string form of the key of the license definition
     * @return the condition
     */
    private fun unrevokedLicense(identityId: EntityID<ULong>, licenseKey: String) =
        (RoleplayIdentityLicensesTable.identity eq identityId) and
                (RoleplayIdentityLicensesTable.licenseKey eq licenseKey) and
                RoleplayIdentityLicensesTable.revokedAt.isNull()
}
