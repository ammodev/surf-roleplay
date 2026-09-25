package dev.slne.surf.roleplay.core.common.identity

import dev.slne.surf.roleplay.api.common.identity.identities.civil.CivilianIdentity
import dev.slne.surf.roleplay.api.common.license.user.UserLicense
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import java.util.*

/**
 * A civilian identity held in memory.
 *
 * @param uuid the unique identifier of this identity
 * @param userUuid the UUID of the player who owns this identity
 * @param accountId the identifier of the transaction account owned by this identity
 * @param licenses the licenses currently or formerly held by this identity
 * @param service the remote user service write operations are sent to
 */
class CoreCivilianIdentity(
    uuid: UUID,
    userUuid: UUID,
    accountId: UUID,
    licenses: Collection<UserLicense>,
    service: UserService
) : CoreRoleplayIdentity(uuid, userUuid, accountId, licenses, service), CivilianIdentity
