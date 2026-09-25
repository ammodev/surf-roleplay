package dev.slne.surf.roleplay.api.common.identity.identities.police

import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import dev.slne.surf.roleplay.api.common.identity.qualification.IdentityQualification
import dev.slne.surf.roleplay.api.common.identity.rank.IdentityRank
import it.unimi.dsi.fastutil.objects.ObjectSet

interface PoliceIdentity : RoleplayIdentity {
    override val name: String get() = "police"

    val rank: IdentityRank
    val qualifications: ObjectSet<IdentityQualification>
}