package dev.slne.surf.roleplay.api.common.identity.exceptions

import java.util.*

/**
 * Thrown when an operation on a user refers to an identity that does not belong to that user.
 *
 * @property userUuid the UUID of the user the operation was performed on
 * @property identityUuid the UUID of the identity that does not belong to the user
 */
data class UnknownIdentityException(
    val userUuid: UUID,
    val identityUuid: UUID
) : IllegalArgumentException("User with UUID $userUuid has no identity with UUID $identityUuid")
