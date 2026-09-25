package dev.slne.surf.roleplay.api.common.identity.identities.police

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [PoliceQualification].
 */
class PoliceQualificationTest {

    @Test
    fun `entries has the size defined by the mapping table`() {
        assertEquals(5, PoliceQualification.entries.size)
    }

    @Test
    fun `byKey round-trips every entry`() {
        for (qualification in PoliceQualification.entries) {
            assertEquals(qualification, PoliceQualification.byKey(qualification.key))
        }
    }

    @Test
    fun `byKey returns null for an unknown key`() {
        assertNull(PoliceQualification.byKey(net.kyori.adventure.key.Key.key("roleplay", "unknown")))
    }

    @Test
    fun `keys are unique`() {
        val keys = PoliceQualification.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }
}
