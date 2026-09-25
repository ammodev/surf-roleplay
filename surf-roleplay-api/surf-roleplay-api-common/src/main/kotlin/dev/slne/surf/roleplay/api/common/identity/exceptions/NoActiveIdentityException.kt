package dev.slne.surf.roleplay.api.common.identity.exceptions

import java.util.*

data class NoActiveIdentityException(
    val uuid: UUID
) : IllegalStateException("User with UUID $uuid has no active identity") 