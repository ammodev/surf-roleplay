package dev.slne.surf.roleplay.fabric.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for the mapping of settings widget ids to actions.
 */
class SettingsActionTest {

    /**
     * Verifies that a key button starts capturing its binding.
     */
    @Test
    fun `a key button starts capturing`() =
        assertEquals(SettingsAction.StartCapture("hud_cursor"), SettingsAction.of("binding_hud_cursor"))

    /**
     * Verifies that a reset button resets its binding.
     */
    @Test
    fun `a reset button resets its binding`() =
        assertEquals(SettingsAction.Reset("hud_cursor"), SettingsAction.of("reset_hud_cursor"))

    /**
     * Verifies that the reset-all button resets every binding and is not read as a binding
     * named `all`.
     */
    @Test
    fun `reset all is not a reset of a binding`() =
        assertEquals(SettingsAction.ResetAll, SettingsAction.of("reset_all"))

    /**
     * Verifies that other widgets trigger no action.
     */
    @Test
    fun `other widgets trigger nothing`() {
        assertNull(SettingsAction.of("cursor_mode"))
        assertNull(SettingsAction.of("binding_"))
        assertNull(SettingsAction.of("reset_"))
    }
}
