package dev.slne.surf.roleplay.api.common.identity.identities.sar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [SarQualification].
 */
class SarQualificationTest {

    @Test
    fun `entries has the size defined by the mapping table`() {
        assertEquals(4, SarQualification.entries.size)
    }

    @Test
    fun `byKey round-trips every entry`() {
        for (qualification in SarQualification.entries) {
            assertEquals(qualification, SarQualification.byKey(qualification.key))
        }
    }

    @Test
    fun `byKey returns null for an unknown key`() {
        assertNull(SarQualification.byKey(net.kyori.adventure.key.Key.key("roleplay", "unknown")))
    }

    @Test
    fun `keys are unique`() {
        val keys = SarQualification.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }
}
