package dev.slne.surf.roleplay.api.common.identity.identities.civil

import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity

interface CivilianIdentity : RoleplayIdentity {
    override val name: String get() = "civilian"
}