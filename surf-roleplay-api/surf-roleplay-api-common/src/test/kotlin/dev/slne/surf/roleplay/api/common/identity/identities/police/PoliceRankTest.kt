package dev.slne.surf.roleplay.api.common.identity.identities.police

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [PoliceRank].
 */
class PoliceRankTest {

    @Test
    fun `entries has the size defined by the mapping table`() {
        assertEquals(13, PoliceRank.entries.size)
    }

    @Test
    fun `byKey round-trips every entry`() {
        for (rank in PoliceRank.entries) {
            assertEquals(rank, PoliceRank.byKey(rank.key))
        }
    }

    @Test
    fun `byKey returns null for an unknown key`() {
        assertNull(PoliceRank.byKey(net.kyori.adventure.key.Key.key("roleplay", "unknown")))
    }

    @Test
    fun `keys are unique`() {
        val keys = PoliceRank.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `levels are strictly increasing in entries order`() {
        val levels = PoliceRank.entries.map { it.level }
        assertTrue(levels.zipWithNext().all { (a, b) -> a < b })
    }
}
