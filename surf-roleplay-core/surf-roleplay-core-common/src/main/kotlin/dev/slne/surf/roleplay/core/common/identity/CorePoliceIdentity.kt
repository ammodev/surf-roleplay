package dev.slne.surf.roleplay.core.common.identity

import dev.slne.surf.api.core.util.freeze
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceIdentity
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceQualification
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceRank
import dev.slne.surf.roleplay.api.common.license.user.UserLicense
import dev.slne.surf.roleplay.core.common.user.CoreRoleplayUser
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayIdentityDto
import dev.slne.surf.roleplay.core.common.user.rpc.policeQualifications
import dev.slne.surf.roleplay.core.common.user.rpc.policeRank
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

/**
 * A police identity held in memory.
 *
 * Rank and qualification changes are sent to the remote user service through the owner, which
 * applies the returned state to this identity.
 *
 * @param uuid the unique identifier of this identity
 * @param owner the in-memory user who owns this identity
 * @param accountId the identifier of the transaction account owned by this identity
 * @param licenses the licenses currently or formerly held by this identity
 * @param rank the rank this identity holds within the police force
 * @param qualifications the police qualifications this identity holds
 */
class CorePoliceIdentity(
    uuid: UUID,
    owner: CoreRoleplayUser,
    accountId: UUID,
    licenses: Collection<UserLicense>,
    rank: PoliceRank,
    qualifications: Collection<PoliceQualification>
) : CoreRoleplayIdentity(uuid, owner, accountId, licenses), PoliceIdentity {
    @Volatile
    private var _rank: PoliceRank = rank

    @Volatile
    private var _qualifications: ObjectSet<PoliceQualification> = mutableObjectSetOf(qualifications)

    /**
     * The rank this identity currently holds within the police force.
     */
    override val rank: PoliceRank get() = _rank

    /**
     * A read-only view of the police qualifications this identity holds.
     */
    override val qualifications: ObjectSet<PoliceQualification> get() = _qualifications.freeze()

    /**
     * Replaces the licenses, the rank and the qualifications of this identity with the state
     * described by [dto].
     *
     * @param dto the state of this identity as returned by the remote user service
     */
    override fun applyState(dto: RoleplayIdentityDto) {
        super.applyState(dto)
        _rank = dto.policeRank()
        _qualifications = mutableObjectSetOf(dto.policeQualifications())
    }

    /**
     * Changes the rank of this identity to [rank] through the remote user service.
     *
     * Nothing is sent if this identity already holds [rank].
     *
     * @param rank the new rank
     */
    override suspend fun setRank(rank: PoliceRank) = owner.write {
        if (rank == _rank) return@write

        owner.applyState(service.setRank(userUuid, uuid, rank.key.asString()))
    }

    /**
     * Adds [qualification] to this identity through the remote user service.
     *
     * Nothing is sent if this identity already holds [qualification].
     *
     * @param qualification the qualification to add
     * @return `true` if the qualification was added, `false` if it was already held
     */
    override suspend fun addQualification(qualification: PoliceQualification): Boolean = owner.write {
        if (qualification in _qualifications) return@write false

        owner.applyState(service.addQualification(userUuid, uuid, qualification.key.asString()))
        true
    }

    /**
     * Removes [qualification] from this identity through the remote user service.
     *
     * Nothing is sent if this identity does not hold [qualification].
     *
     * @param qualification the qualification to remove
     * @return `true` if the qualification was removed, `false` if it was not held
     */
    override suspend fun removeQualification(qualification: PoliceQualification): Boolean = owner.write {
        if (qualification !in _qualifications) return@write false

        owner.applyState(service.removeQualification(userUuid, uuid, qualification.key.asString()))
        true
    }
}
