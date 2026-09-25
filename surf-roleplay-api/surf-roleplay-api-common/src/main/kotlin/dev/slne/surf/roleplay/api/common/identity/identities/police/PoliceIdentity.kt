package dev.slne.surf.roleplay.api.common.identity.identities.police

import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import it.unimi.dsi.fastutil.objects.ObjectSet

/**
 * A roleplay identity that belongs to the police force.
 */
interface PoliceIdentity : RoleplayIdentity {
    override val type: IdentityType get() = IdentityType.POLICE

    /**
     * The rank this identity currently holds within the police force.
     */
    val rank: PoliceRank

    /**
     * The police qualifications this identity currently holds.
     */
    val qualifications: ObjectSet<PoliceQualification>
}
