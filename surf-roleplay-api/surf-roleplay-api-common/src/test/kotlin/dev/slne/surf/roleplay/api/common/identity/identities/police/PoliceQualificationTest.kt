package dev.slne.surf.roleplay.api.common.identity.identities.police

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [PoliceQualification].
 */
class PoliceQualificationTest {

    /**
     * Verifies that the number of entries matches the expected count.
     */
    @Test
    fun `entries has the size defined by the mapping table`() {
        assertEquals(5, PoliceQualification.entries.size)
    }

    /**
     * Verifies that every entry can be resolved back by its own key.
     */
    @Test
    fun `byKey round-trips every entry`() {
        for (qualification in PoliceQualification.entries) {
            assertEquals(qualification, PoliceQualification.byKey(qualification.key))
        }
    }

    /**
     * Verifies that resolving an unregistered key returns null.
     */
    @Test
    fun `byKey returns null for an unknown key`() {
        assertNull(PoliceQualification.byKey(net.kyori.adventure.key.Key.key("roleplay", "unknown")))
    }

    /**
     * Verifies that no two entries share the same key.
     */
    @Test
    fun `keys are unique`() {
        val keys = PoliceQualification.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }
}
