package dev.slne.surf.roleplay.api.common.identity.identities.police

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [PoliceRank].
 */
class PoliceRankTest {

    /**
     * Verifies that the number of entries matches the expected count.
     */
    @Test
    fun `entries has the size defined by the mapping table`() {
        assertEquals(13, PoliceRank.entries.size)
    }

    /**
     * Verifies that every entry can be resolved back by its own key.
     */
    @Test
    fun `byKey round-trips every entry`() {
        for (rank in PoliceRank.entries) {
            assertEquals(rank, PoliceRank.byKey(rank.key))
        }
    }

    /**
     * Verifies that resolving an unregistered key returns null.
     */
    @Test
    fun `byKey returns null for an unknown key`() {
        assertNull(PoliceRank.byKey(net.kyori.adventure.key.Key.key("roleplay", "unknown")))
    }

    /**
     * Verifies that no two entries share the same key.
     */
    @Test
    fun `keys are unique`() {
        val keys = PoliceRank.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    /**
     * Verifies that entry levels increase strictly in declaration order.
     */
    @Test
    fun `levels are strictly increasing in entries order`() {
        val levels = PoliceRank.entries.map { it.level }
        assertTrue(levels.zipWithNext().all { (a, b) -> a < b })
    }
}
