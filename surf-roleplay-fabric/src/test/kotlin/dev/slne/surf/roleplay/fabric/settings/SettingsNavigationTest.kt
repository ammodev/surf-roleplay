package dev.slne.surf.roleplay.fabric.settings

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the navigation state of the settings screen.
 */
class SettingsNavigationTest {

    /**
     * The category ids used by the tests.
     */
    private val ids = listOf("controls", "cursor")

    /**
     * Verifies that a known value selects its category.
     */
    @Test
    fun `a known value selects its category`() {
        assertEquals("cursor", SettingsNavigation.select("cursor", ids))
    }

    /**
     * Verifies that a missing or unknown value falls back to the first category.
     */
    @Test
    fun `a missing or unknown value selects the first category`() {
        assertEquals("controls", SettingsNavigation.select(null, ids))
        assertEquals("controls", SettingsNavigation.select("other", ids))
    }

    /**
     * Verifies that offsets are remembered per area, default to zero and never go negative.
     */
    @Test
    fun `offsets are remembered per area`() {
        val offsets = ScrollOffsets().with("a", 40).with("b", -3)
        assertEquals(40, offsets.of("a"))
        assertEquals(0, offsets.of("b"))
        assertEquals(0, offsets.of("c"))
        assertEquals(10, offsets.with("a", 10).of("a"))
    }
}
