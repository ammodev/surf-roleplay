package dev.slne.surf.roleplay.core.common.identity

import dev.slne.surf.api.core.util.freeze
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceIdentity
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceQualification
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceRank
import dev.slne.surf.roleplay.api.common.license.user.UserLicense
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

/**
 * A police identity held in memory.
 *
 * @param uuid the unique identifier of this identity
 * @param userUuid the UUID of the player who owns this identity
 * @param accountId the identifier of the transaction account owned by this identity
 * @param licenses the licenses currently or formerly held by this identity
 * @param rank the rank this identity holds within the police force
 * @param qualifications the police qualifications this identity holds
 * @param service the remote user service write operations are sent to
 */
class CorePoliceIdentity(
    uuid: UUID,
    userUuid: UUID,
    accountId: UUID,
    licenses: Collection<UserLicense>,
    override val rank: PoliceRank,
    qualifications: Collection<PoliceQualification>,
    service: UserService
) : CoreRoleplayIdentity(uuid, userUuid, accountId, licenses, service), PoliceIdentity {
    private val _qualifications: ObjectSet<PoliceQualification> = mutableObjectSetOf(qualifications)

    /**
     * A read-only view of the police qualifications this identity holds.
     */
    override val qualifications: ObjectSet<PoliceQualification> get() = _qualifications.freeze()

    /**
     * Changes the rank of this identity to [rank] through the remote user service.
     *
     * @param rank the new rank
     */
    override suspend fun setRank(rank: PoliceRank) {
        TODO()
    }

    /**
     * Adds [qualification] to this identity through the remote user service.
     *
     * @param qualification the qualification to add
     * @return `true` if the qualification was added, `false` if it was already held
     */
    override suspend fun addQualification(qualification: PoliceQualification): Boolean {
        TODO()
    }

    /**
     * Removes [qualification] from this identity through the remote user service.
     *
     * @param qualification the qualification to remove
     * @return `true` if the qualification was removed, `false` if it was not held
     */
    override suspend fun removeQualification(qualification: PoliceQualification): Boolean {
        TODO()
    }
}
