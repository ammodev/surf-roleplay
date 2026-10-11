package dev.slne.surf.roleplay.paper.interaction

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for parsing entity target ids.
 */
class EntityTargetIdTest {

    /**
     * Verifies that a UUID in text form parses and malformed ids do not throw.
     */
    @Test
    fun `parses uuids and rejects the rest`() {
        val uuid = UUID.randomUUID()
        assertEquals(uuid, EntityTargetId.parse(uuid.toString()))
        listOf("", "not-a-uuid", "1-1-1-1-1", "x".repeat(70)).forEach { assertNull(EntityTargetId.parse(it), it) }
    }
}
