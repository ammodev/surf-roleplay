package dev.slne.surf.roleplay.microservice.user.db.tables

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.columns.time.offsetDateTime
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ReferenceOption
import dev.slne.surf.database.table.AuditableLongIdTable

/**
 * Stores the licenses granted to roleplay identities.
 *
 * Revoked licenses stay in the table as history; granting a license again adds a new row.
 * An identity holds at most one unrevoked row per license key. Deleting an identity deletes its
 * licenses.
 */
object RoleplayIdentityLicensesTable : AuditableLongIdTable("roleplay_identity_licenses") {
    /** The identity the license was granted to. */
    val identity = reference("identity_id", RoleplayIdentitiesTable, onDelete = ReferenceOption.CASCADE)

    /** The string form of the key of the license definition. */
    val licenseKey = varchar("license_key", 128)

    /** The point in time the license was granted. */
    val acquiredAt = offsetDateTime("acquired_at")

    /** The UUID of the player who granted the license, or `null` if no granting player is recorded. */
    val grantedBy = nativeUuid("granted_by").nullable()

    /** The UUID of the player who revoked the license, or `null` if it has not been revoked. */
    val revokedBy = nativeUuid("revoked_by").nullable()

    /**
     * The name of the
     * [LicenseRevokedReason][dev.slne.surf.roleplay.api.common.license.revoke.LicenseRevokedReason]
     * the license was revoked for, or `null` if it has not been revoked.
     */
    val revokedReason = varchar("revoked_reason", 32).nullable()

    /** The point in time the license was revoked, or `null` if it has not been revoked. */
    val revokedAt = offsetDateTime("revoked_at").nullable()
}
