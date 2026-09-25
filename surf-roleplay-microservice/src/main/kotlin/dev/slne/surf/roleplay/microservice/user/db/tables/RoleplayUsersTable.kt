package dev.slne.surf.roleplay.microservice.user.db.tables

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.table.AuditableLongIdTable

object RoleplayUsersTable : AuditableLongIdTable("roleplay_users") {
    val uuid = nativeUuid("uuid").uniqueIndex()
}