@file:Suppress("NonExtendableApiUsage")

package dev.slne.surf.roleplay.api.common.user

import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import dev.slne.surf.roleplay.api.common.transaction.RoleplayTransactional
import dev.slne.surf.transaction.api.transactional.Transactional
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.UnmodifiableView
import java.util.*

interface RoleplayUser : RoleplayTransactional {
    val uuid: UUID

    val activeIdentity: RoleplayIdentity?
    val identities: @UnmodifiableView ObjectList<RoleplayIdentity>

    suspend fun createIdentity(identity: RoleplayIdentity)
    suspend fun setActiveIdentity(identity: RoleplayIdentity)

    override val delegate: Transactional?
        get() = activeIdentity

    override val delegateName: String
        get() = uuid.toString()
}