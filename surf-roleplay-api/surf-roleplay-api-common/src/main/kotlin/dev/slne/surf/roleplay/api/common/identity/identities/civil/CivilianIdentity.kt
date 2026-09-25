package dev.slne.surf.roleplay.api.common.identity.identities.civil

import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity

/**
 * A roleplay identity without membership in a uniformed organisation.
 */
interface CivilianIdentity : RoleplayIdentity {
    /**
     * The identity type of every civilian identity, [IdentityType.CIVILIAN].
     */
    override val type: IdentityType get() = IdentityType.CIVILIAN
}
