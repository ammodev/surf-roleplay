package dev.slne.surf.roleplay.api.common.identity.identities.sar

import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import it.unimi.dsi.fastutil.objects.ObjectSet

/**
 * A roleplay identity that belongs to the search-and-rescue service.
 */
interface SarIdentity : RoleplayIdentity {
    override val type: IdentityType get() = IdentityType.SAR

    /**
     * The rank this identity currently holds within the search-and-rescue service.
     */
    val rank: SarRank

    /**
     * The search-and-rescue qualifications this identity currently holds.
     */
    val qualifications: ObjectSet<SarQualification>
}
