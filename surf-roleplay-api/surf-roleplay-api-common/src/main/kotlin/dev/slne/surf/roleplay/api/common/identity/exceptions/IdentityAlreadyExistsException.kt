package dev.slne.surf.roleplay.api.common.identity.exceptions

import dev.slne.surf.roleplay.api.common.identity.IdentityType
import java.util.*

/**
 * Thrown when an identity is created for a user that already owns an identity of the same type.
 *
 * @property userUuid the UUID of the user that already owns an identity of [type]
 * @property type the identity type that already exists for the user
 */
data class IdentityAlreadyExistsException(
    val userUuid: UUID,
    val type: IdentityType
) : IllegalStateException("User with UUID $userUuid already has an identity of type $type")
