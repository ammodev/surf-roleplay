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

    /**
     * Changes the rank this identity holds within the police force to [rank].
     *
     * @param rank the new rank
     */
    suspend fun setRank(rank: PoliceRank)

    /**
     * Grants [qualification] to this identity.
     *
     * @param qualification the qualification to add
     * @return `true` if the qualification was added, `false` if this identity already held it
     */
    suspend fun addQualification(qualification: PoliceQualification): Boolean

    /**
     * Takes [qualification] away from this identity.
     *
     * @param qualification the qualification to remove
     * @return `true` if the qualification was removed, `false` if this identity did not hold it
     */
    suspend fun removeQualification(qualification: PoliceQualification): Boolean
}
