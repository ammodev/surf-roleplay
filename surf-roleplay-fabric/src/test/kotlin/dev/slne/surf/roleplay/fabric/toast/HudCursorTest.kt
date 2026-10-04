package dev.slne.surf.roleplay.fabric.toast

import com.mojang.blaze3d.platform.InputConstants
import dev.slne.surf.roleplay.fabric.settings.KeyMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the key modes of the HUD cursor.
 */
class HudCursorTest {

    /**
     * Verifies that toggle mode releases on a press and captures on the next press.
     */
    @Test
    fun `toggle mode releases on press and captures on the next press`() {
        assertEquals(HudCursor.Change.RELEASE, HudCursor.change(keyDown = true, keyPressed = true, screenOpen = false, active = false, mode = KeyMode.TOGGLE, ready = true))
        assertNull(HudCursor.change(keyDown = false, keyPressed = false, screenOpen = false, active = true, mode = KeyMode.TOGGLE, ready = true))
        assertEquals(HudCursor.Change.CAPTURE, HudCursor.change(keyDown = true, keyPressed = true, screenOpen = false, active = true, mode = KeyMode.TOGGLE, ready = true))
    }

    /**
     * Verifies that an open screen ends toggle mode.
     */
    @Test
    fun `a screen ends toggle mode`() {
        assertEquals(HudCursor.Change.END, HudCursor.change(keyDown = false, keyPressed = false, screenOpen = true, active = true, mode = KeyMode.TOGGLE, ready = true))
    }

    /**
     * Verifies that hold mode follows the held key.
     */
    @Test
    fun `hold mode is unchanged`() {
        assertEquals(HudCursor.Change.RELEASE, HudCursor.change(true, true, false, false, KeyMode.HOLD, true))
        assertEquals(HudCursor.Change.CAPTURE, HudCursor.change(false, false, false, true, KeyMode.HOLD, true))
    }

    /**
     * Verifies that a press while a screen is open does not release the mouse.
     */
    @Test
    fun `a press under a screen does nothing in toggle mode`() {
        assertNull(HudCursor.change(keyDown = true, keyPressed = true, screenOpen = true, active = false, mode = KeyMode.TOGGLE, ready = true))
    }

    /**
     * Verifies that a held key without a fresh press does not end an active toggle.
     */
    @Test
    fun `a held key without a press keeps toggle mode`() {
        assertNull(HudCursor.change(keyDown = true, keyPressed = false, screenOpen = false, active = true, mode = KeyMode.TOGGLE, ready = true))
    }

    /**
     * Verifies that hold mode does not release without the key being down.
     */
    @Test
    fun `hold mode ignores a press without the key down`() {
        assertNull(HudCursor.change(keyDown = false, keyPressed = true, screenOpen = false, active = false, mode = KeyMode.HOLD, ready = true))
    }

    /**
     * Verifies that an active cursor ends when it may no longer be shown, and that a press does
     * not release it while not ready.
     */
    @Test
    fun `an active cursor is captured when not ready`() {
        assertEquals(HudCursor.Change.CAPTURE, HudCursor.change(keyDown = false, keyPressed = false, screenOpen = false, active = true, mode = KeyMode.TOGGLE, ready = false))
        assertEquals(HudCursor.Change.CAPTURE, HudCursor.change(keyDown = true, keyPressed = false, screenOpen = false, active = true, mode = KeyMode.HOLD, ready = false))
        assertNull(HudCursor.change(keyDown = true, keyPressed = true, screenOpen = false, active = false, mode = KeyMode.TOGGLE, ready = false))
    }

    /**
     * Verifies that a cursor key bound to a mouse button keeps that button's events.
     */
    @Test
    fun `a mouse button bound to the cursor key is not taken`() {
        fun name(button: Int) = InputConstants.Type.MOUSE.getOrCreate(button).name
        assertFalse(HudCursor.takesButton("key.mouse.4", name(3)))
        assertTrue(HudCursor.takesButton("key.mouse.4", name(0)))
        assertTrue(HudCursor.takesButton("key.keyboard.left.alt", name(3)))
        assertFalse(HudCursor.takesButton("key.mouse.left", name(0)))
        assertFalse(HudCursor.takesButton("key.mouse.right", name(1)))
        assertFalse(HudCursor.takesButton("key.mouse.middle", name(2)))
        assertTrue(HudCursor.takesButton("key.mouse.middle", name(0)))
    }
}
