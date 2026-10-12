package dev.slne.surf.roleplay.paper.interaction

import dev.slne.surf.roleplay.protocol.interaction.InteractionLimits
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for the range and line-of-sight decision.
 */
class TargetCheckTest {

    /**
     * Verifies that a target within range and in sight passes, including exactly at the range.
     */
    @Test
    fun `in range and in sight passes`() {
        assertEquals(TargetCheck.Result.Passed, TargetCheck.check(distance = 2.9, range = 3.0, inSight = true))
        assertEquals(TargetCheck.Result.Passed, TargetCheck.check(distance = 3.0, range = 3.0, inSight = true))
    }

    /**
     * Verifies that out of range, out of sight and invalid distances fail.
     */
    @Test
    fun `out of range or sight fails`() {
        assertIs<TargetCheck.Result.Failed>(TargetCheck.check(distance = 3.01, range = 3.0, inSight = true))
        assertIs<TargetCheck.Result.Failed>(TargetCheck.check(distance = 1.0, range = 3.0, inSight = false))
        assertIs<TargetCheck.Result.Failed>(TargetCheck.check(distance = Double.NaN, range = 3.0, inSight = true))
        assertIs<TargetCheck.Result.Failed>(TargetCheck.check(distance = 1.0, range = Double.NaN, inSight = true))
    }

    /**
     * Verifies that only distances beyond the largest range a normal client raycasts, plus one
     * block of slack, are implausible.
     */
    @Test
    fun `only distances beyond the client raycast are implausible`() {
        assertEquals(false, TargetCheck.isImplausible(3.5))
        assertEquals(false, TargetCheck.isImplausible(InteractionLimits.MAX_RANGE + 1))
        assertEquals(true, TargetCheck.isImplausible(InteractionLimits.MAX_RANGE + 1.01))
        assertEquals(true, TargetCheck.isImplausible(Double.NaN))
    }
}
