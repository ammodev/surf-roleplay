package dev.slne.surf.roleplay.core.common.identity

import dev.slne.surf.api.core.util.freeze
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.roleplay.api.common.identity.identities.sar.SarIdentity
import dev.slne.surf.roleplay.api.common.identity.identities.sar.SarQualification
import dev.slne.surf.roleplay.api.common.identity.identities.sar.SarRank
import dev.slne.surf.roleplay.api.common.license.user.UserLicense
import dev.slne.surf.roleplay.core.common.user.CoreRoleplayUser
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayIdentityDto
import dev.slne.surf.roleplay.core.common.user.rpc.sarQualifications
import dev.slne.surf.roleplay.core.common.user.rpc.sarRank
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

/**
 * A search-and-rescue identity held in memory.
 *
 * Rank and qualification changes are sent to the remote user service through the owner, which
 * applies the returned state to this identity.
 *
 * @param uuid the unique identifier of this identity
 * @param owner the in-memory user who owns this identity
 * @param accountId the identifier of the transaction account owned by this identity
 * @param licenses the licenses currently or formerly held by this identity
 * @param rank the rank this identity holds within the search-and-rescue service
 * @param qualifications the search-and-rescue qualifications this identity holds
 */
class CoreSarIdentity(
    uuid: UUID,
    owner: CoreRoleplayUser,
    accountId: UUID,
    licenses: Collection<UserLicense>,
    rank: SarRank,
    qualifications: Collection<SarQualification>
) : CoreRoleplayIdentity(uuid, owner, accountId, licenses), SarIdentity {
    @Volatile
    private var _rank: SarRank = rank

    @Volatile
    private var _qualifications: ObjectSet<SarQualification> = mutableObjectSetOf(qualifications)

    /**
     * The rank this identity currently holds within the search-and-rescue service.
     */
    override val rank: SarRank get() = _rank

    /**
     * A read-only view of the search-and-rescue qualifications this identity holds.
     */
    override val qualifications: ObjectSet<SarQualification> get() = _qualifications.freeze()

    /**
     * Replaces the licenses, the rank and the qualifications of this identity with the state
     * described by [dto].
     *
     * @param dto the state of this identity as returned by the remote user service
     */
    override fun applyState(dto: RoleplayIdentityDto) {
        super.applyState(dto)
        _rank = dto.sarRank()
        _qualifications = mutableObjectSetOf(dto.sarQualifications())
    }

    /**
     * Changes the rank of this identity to [rank] through the remote user service.
     *
     * Nothing is sent if this identity already holds [rank].
     *
     * @param rank the new rank
     */
    override suspend fun setRank(rank: SarRank) = owner.write {
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
    override suspend fun addQualification(qualification: SarQualification): Boolean = owner.write {
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
    override suspend fun removeQualification(qualification: SarQualification): Boolean = owner.write {
        if (qualification !in _qualifications) return@write false

        owner.applyState(service.removeQualification(userUuid, uuid, qualification.key.asString()))
        true
    }
}
