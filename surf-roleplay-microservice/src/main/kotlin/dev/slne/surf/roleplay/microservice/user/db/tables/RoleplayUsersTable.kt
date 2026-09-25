package dev.slne.surf.roleplay.microservice.user.db.tables

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.table.AuditableLongIdTable

/**
 * Stores the roleplay users, one row per player.
 */
object RoleplayUsersTable : AuditableLongIdTable("roleplay_users") {
    /** The UUID of the player. */
    val uuid = nativeUuid("uuid").uniqueIndex()
}
