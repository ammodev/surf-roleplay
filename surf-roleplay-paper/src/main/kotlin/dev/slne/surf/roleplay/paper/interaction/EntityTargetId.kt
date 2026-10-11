package dev.slne.surf.roleplay.paper.interaction

import java.util.UUID

/**
 * Parses the ids of entity targets.
 */
object EntityTargetId {
    /**
     * The canonical text form of a UUID.
     */
    private val PATTERN = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")

    /**
     * Parses an entity target id.
     *
     * @param id the id, a UUID in canonical text form
     * @return the UUID, or `null` if the id is malformed
     */
    fun parse(id: String): UUID? = if (PATTERN.matches(id)) UUID.fromString(id) else null
}
