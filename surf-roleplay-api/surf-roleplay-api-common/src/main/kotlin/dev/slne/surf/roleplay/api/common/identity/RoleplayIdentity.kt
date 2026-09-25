@file:Suppress("NonExtendableApiUsage")

package dev.slne.surf.roleplay.api.common.identity

import dev.slne.surf.roleplay.api.common.license.License
import dev.slne.surf.roleplay.api.common.license.user.UserLicense
import dev.slne.surf.transaction.api.transactional.Transactional
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

interface RoleplayIdentity : Transactional {
    val uuid: UUID
    val name: String

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