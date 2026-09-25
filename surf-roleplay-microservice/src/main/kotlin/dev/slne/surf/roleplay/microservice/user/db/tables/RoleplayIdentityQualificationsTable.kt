package dev.slne.surf.roleplay.microservice.user.db.tables

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ReferenceOption
import dev.slne.surf.database.table.AuditableLongIdTable

/**
 * Stores the qualifications held by roleplay identities, one row per identity and qualification.
 *
 * Deleting an identity deletes its qualifications.
 */
object RoleplayIdentityQualificationsTable : AuditableLongIdTable("roleplay_identity_qualifications") {
    /** The identity holding the qualification. */
    val identity = reference("identity_id", RoleplayIdentitiesTable, onDelete = ReferenceOption.CASCADE)

    /** The string form of the key of the qualification. */
    val qualificationKey = varchar("qualification_key", 128)

    init {
        uniqueIndex(identity, qualificationKey)
    }
}
