package dev.slne.surf.roleplay.microservice.user.db.tables

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ReferenceOption
import dev.slne.surf.database.table.AuditableLongIdTable

/**
 * Stores the roleplay identities of users.
 *
 * A user owns at most one identity per identity type. Deleting a user deletes its identities.
 */
object RoleplayIdentitiesTable : AuditableLongIdTable("roleplay_identities") {
    /** The unique identifier of the identity. */
    val uuid = nativeUuid("uuid").uniqueIndex()

    /** The user who owns the identity. */
    val user = reference("user_id", RoleplayUsersTable, onDelete = ReferenceOption.CASCADE)

    /** The name of the identity's [IdentityType][dev.slne.surf.roleplay.api.common.identity.IdentityType]. */
    val type = varchar("type", 16)

    /** The identifier of the transaction account owned by the identity. */
    val accountId = nativeUuid("account_id")

    /** The string form of the key of the rank the identity holds, or `null` if it holds no rank. */
    val rankKey = varchar("rank_key", 128).nullable()

    init {
        uniqueIndex(user, type)
    }
}
