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

    /**
     * Changes the rank this identity holds within the search-and-rescue service to [rank].
     *
     * @param rank the new rank
     */
    suspend fun setRank(rank: SarRank)

    /**
     * Grants [qualification] to this identity.
     *
     * @param qualification the qualification to add
     * @return `true` if the qualification was added, `false` if this identity already held it
     */
    suspend fun addQualification(qualification: SarQualification): Boolean

    /**
     * Takes [qualification] away from this identity.
     *
     * @param qualification the qualification to remove
     * @return `true` if the qualification was removed, `false` if this identity did not hold it
     */
    suspend fun removeQualification(qualification: SarQualification): Boolean
}
