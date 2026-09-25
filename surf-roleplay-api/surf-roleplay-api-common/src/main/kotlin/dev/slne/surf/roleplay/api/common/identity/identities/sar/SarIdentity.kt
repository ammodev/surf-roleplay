package dev.slne.surf.roleplay.api.common.identity.identities.sar

import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import dev.slne.surf.roleplay.api.common.identity.qualification.IdentityQualification
import dev.slne.surf.roleplay.api.common.identity.rank.IdentityRank
import it.unimi.dsi.fastutil.objects.ObjectSet

interface SarIdentity : RoleplayIdentity {
    override val name: String get() = "sar"

    val rank: IdentityRank
    val qualifications: ObjectSet<IdentityQualification>
}