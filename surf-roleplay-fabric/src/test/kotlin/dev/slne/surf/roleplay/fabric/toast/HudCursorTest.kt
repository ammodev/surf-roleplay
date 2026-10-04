package dev.slne.surf.roleplay.fabric.toast

import dev.slne.surf.roleplay.fabric.settings.KeyMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for the key modes of the HUD cursor.
 */
class HudCursorTest {

    /**
     * Verifies that toggle mode releases on a press and captures on the next press.
     */
    @Test
    fun `toggle mode releases on press and captures on the next press`() {
        assertEquals(HudCursor.Change.RELEASE, HudCursor.change(keyDown = true, keyPressed = true, screenOpen = false, active = false, mode = KeyMode.TOGGLE))
        assertNull(HudCursor.change(keyDown = false, keyPressed = false, screenOpen = false, active = true, mode = KeyMode.TOGGLE))
        assertEquals(HudCursor.Change.CAPTURE, HudCursor.change(keyDown = true, keyPressed = true, screenOpen = false, active = true, mode = KeyMode.TOGGLE))
    }

    /**
     * Verifies that an open screen ends toggle mode.
     */
    @Test
    fun `a screen ends toggle mode`() {
        assertEquals(HudCursor.Change.END, HudCursor.change(keyDown = false, keyPressed = false, screenOpen = true, active = true, mode = KeyMode.TOGGLE))
    }

    /**
     * Verifies that hold mode follows the held key.
     */
    @Test
    fun `hold mode is unchanged`() {
        assertEquals(HudCursor.Change.RELEASE, HudCursor.change(true, true, false, false, KeyMode.HOLD))
        assertEquals(HudCursor.Change.CAPTURE, HudCursor.change(false, false, false, true, KeyMode.HOLD))
    }
}
