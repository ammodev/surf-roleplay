@file:Suppress("NonExtendableApiUsage")

package dev.slne.surf.roleplay.api.common.identity

import dev.slne.surf.roleplay.api.common.license.License
import dev.slne.surf.roleplay.api.common.license.user.UserLicense
import dev.slne.surf.transaction.api.transactional.Transactional
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

/**
 * A single roleplay persona owned by a user.
 *
 * An identity holds its own transactional account, licenses and organisation
 * membership, independent of every other identity the same user may own.
 */
interface RoleplayIdentity : Transactional {
    /**
     * The unique identifier of this identity.
     */
    val uuid: UUID

    /**
     * The organisation this identity belongs to.
     */
    val type: IdentityType

    /**
     * The licenses currently or formerly held by this identity.
     */
    val licenses: ObjectSet<UserLicense>

    /**
     * Checks whether this identity currently holds [license].
     *
     * A revoked license is never considered held, even if it was granted at some point.
     */
    fun hasLicense(license: License): Boolean = licenses.any { userLicense ->
        !userLicense.isRevoked && userLicense.licenseKey == license.key
    }
}