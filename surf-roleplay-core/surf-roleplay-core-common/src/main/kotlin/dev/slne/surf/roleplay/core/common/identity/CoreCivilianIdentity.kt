package dev.slne.surf.roleplay.core.common.identity

import dev.slne.surf.roleplay.api.common.identity.identities.civil.CivilianIdentity
import dev.slne.surf.roleplay.api.common.license.user.UserLicense
import dev.slne.surf.roleplay.core.common.user.CoreRoleplayUser
import java.util.*

/**
 * A civilian identity held in memory.
 *
 * @param uuid the unique identifier of this identity
 * @param owner the in-memory user who owns this identity
 * @param accountId the identifier of the transaction account owned by this identity
 * @param licenses the licenses currently or formerly held by this identity
 */
class CoreCivilianIdentity(
    uuid: UUID,
    owner: CoreRoleplayUser,
    accountId: UUID,
    licenses: Collection<UserLicense>
) : CoreRoleplayIdentity(uuid, owner, accountId, licenses), CivilianIdentity
