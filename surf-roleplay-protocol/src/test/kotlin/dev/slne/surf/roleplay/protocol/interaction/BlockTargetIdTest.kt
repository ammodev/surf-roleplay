package dev.slne.surf.roleplay.protocol.interaction

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for the text form of block target ids.
 */
class BlockTargetIdTest {

    /**
     * Verifies that a formatted id parses back to its position, including negative values.
     */
    @Test
    fun `format and parse are inverse`() {
        assertEquals(BlockTargetId.Position(10, -64, -3), BlockTargetId.parse(BlockTargetId.format(10, -64, -3)))
    }

    /**
     * Verifies that malformed ids are rejected instead of throwing.
     */
    @Test
    fun `malformed ids parse to null`() {
        listOf("", "1,2", "1,2,3,4", "a,b,c", "1, 2,3", "1.5,2,3", "99999999999,0,0", "1".repeat(70), "30000001,0,0", "0,9999,0")
            .forEach { assertNull(BlockTargetId.parse(it), it) }
    }
}
