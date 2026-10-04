package dev.slne.surf.roleplay.paper.tablist

import dev.slne.surf.roleplay.protocol.tablist.OnlineLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for the mapping of online counts to coarse levels.
 */
class OnlineLevelsTest {

    /**
     * Verifies the default thresholds.
     */
    @Test
    fun `default thresholds`() {
        assertEquals(OnlineLevel.NONE, OnlineLevels.level(0, listOf(1, 3)))
        assertEquals(OnlineLevel.FEW, OnlineLevels.level(1, listOf(1, 3)))
        assertEquals(OnlineLevel.FEW, OnlineLevels.level(2, listOf(1, 3)))
        assertEquals(OnlineLevel.MANY, OnlineLevels.level(3, listOf(1, 3)))
        assertEquals(OnlineLevel.MANY, OnlineLevels.level(50, listOf(1, 3)))
    }

    /**
     * Verifies that the thresholds default to one and three.
     */
    @Test
    fun `thresholds default`() {
        assertEquals(OnlineLevel.FEW, OnlineLevels.level(2))
        assertEquals(OnlineLevel.MANY, OnlineLevels.level(3))
    }

    /**
     * Verifies custom thresholds.
     */
    @Test
    fun `custom thresholds`() {
        assertEquals(OnlineLevel.NONE, OnlineLevels.level(4, listOf(5, 10)))
        assertEquals(OnlineLevel.FEW, OnlineLevels.level(9, listOf(5, 10)))
        assertEquals(OnlineLevel.MANY, OnlineLevels.level(10, listOf(5, 10)))
    }

    /**
     * Verifies that malformed thresholds are rejected.
     */
    @Test
    fun `invalid thresholds are rejected`() {
        assertFailsWith<IllegalArgumentException> { OnlineLevels.level(1, listOf(1)) }
        assertFailsWith<IllegalArgumentException> { OnlineLevels.level(1, listOf(1, 2, 3)) }
        assertFailsWith<IllegalArgumentException> { OnlineLevels.level(1, listOf(3, 3)) }
        assertFailsWith<IllegalArgumentException> { OnlineLevels.level(1, listOf(4, 2)) }
        assertFailsWith<IllegalArgumentException> { OnlineLevels.level(1, listOf(-1, 2)) }
        assertFailsWith<IllegalArgumentException> { OnlineLevels.level(1, listOf(0, 2)) }
    }
}
