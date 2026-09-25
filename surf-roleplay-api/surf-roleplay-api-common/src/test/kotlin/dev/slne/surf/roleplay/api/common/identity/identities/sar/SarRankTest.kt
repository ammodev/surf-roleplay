package dev.slne.surf.roleplay.api.common.identity.identities.sar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [SarRank].
 */
class SarRankTest {

    @Test
    fun `entries has the size defined by the mapping table`() {
        assertEquals(6, SarRank.entries.size)
    }

    @Test
    fun `byKey round-trips every entry`() {
        for (rank in SarRank.entries) {
            assertEquals(rank, SarRank.byKey(rank.key))
        }
    }

    @Test
    fun `byKey returns null for an unknown key`() {
        assertNull(SarRank.byKey(net.kyori.adventure.key.Key.key("roleplay", "unknown")))
    }

    @Test
    fun `keys are unique`() {
        val keys = SarRank.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `levels are strictly increasing in entries order`() {
        val levels = SarRank.entries.map { it.level }
        assertTrue(levels.zipWithNext().all { (a, b) -> a < b })
    }
}
