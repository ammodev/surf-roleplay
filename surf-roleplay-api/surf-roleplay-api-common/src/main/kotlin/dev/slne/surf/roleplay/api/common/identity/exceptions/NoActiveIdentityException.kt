package dev.slne.surf.roleplay.api.common.identity.exceptions

import java.util.*

/**
 * Thrown when an operation requires an active identity but the user has none.
 *
 * @property uuid the UUID of the user with no active identity
 */
data class NoActiveIdentityException(
    val uuid: UUID
) : IllegalStateException("User with UUID $uuid has no active identity")
