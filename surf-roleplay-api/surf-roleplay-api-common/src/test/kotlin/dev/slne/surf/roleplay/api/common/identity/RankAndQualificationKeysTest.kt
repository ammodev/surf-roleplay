package dev.slne.surf.roleplay.api.common.identity

import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceQualification
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceRank
import dev.slne.surf.roleplay.api.common.identity.identities.sar.SarQualification
import dev.slne.surf.roleplay.api.common.identity.identities.sar.SarRank
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that rank and qualification keys are unique across every organisation hierarchy.
 */
class RankAndQualificationKeysTest {

    @Test
    fun `keys are unique across all rank and qualification hierarchies`() {
        val keys = PoliceRank.entries.map { it.key } +
            PoliceQualification.entries.map { it.key } +
            SarRank.entries.map { it.key } +
            SarQualification.entries.map { it.key }

        assertEquals(keys.size, keys.toSet().size)
    }
}
