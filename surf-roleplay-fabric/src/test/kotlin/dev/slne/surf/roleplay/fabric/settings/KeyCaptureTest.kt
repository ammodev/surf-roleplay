package dev.slne.surf.roleplay.fabric.settings

import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.input.MouseButtonInfo
import org.lwjgl.glfw.GLFW
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for capturing a new key for a binding.
 */
class KeyCaptureTest {

    /**
     * Creates a key event without modifiers.
     *
     * @param key the GLFW key code
     * @return the event
     */
    private fun key(key: Int) = KeyEvent(key, 0, 0)

    /**
     * Verifies that a key pressed while capturing becomes the binding's key and ends the capture.
     */
    @Test
    fun `a pressed key is bound and ends the capture`() {
        val step = KeyCapture.keyPressed(CaptureState(capturing = "hud_cursor"), key(GLFW.GLFW_KEY_R))

        assertTrue(step.taken)
        assertEquals("hud_cursor", step.binding)
        assertEquals("key.keyboard.r", step.key?.name)
        assertNull(step.state.capturing)
    }

    /**
     * Verifies that Escape while capturing unbinds the binding.
     */
    @Test
    fun `escape unbinds`() {
        val step = KeyCapture.keyPressed(CaptureState(capturing = "hud_cursor"), key(GLFW.GLFW_KEY_ESCAPE))

        assertEquals("hud_cursor", step.binding)
        assertEquals(KeyBindings.UNBOUND_KEY, step.key?.name)
    }

    /**
     * Verifies that repeats of the key that ended the capture are taken until it is released,
     * and that the key reaches the panel again after the release.
     */
    @Test
    fun `the key that ended the capture is swallowed until released`() {
        val ended = KeyCapture.keyPressed(CaptureState(capturing = "hud_cursor"), key(GLFW.GLFW_KEY_ESCAPE)).state

        val repeat = KeyCapture.keyPressed(ended, key(GLFW.GLFW_KEY_ESCAPE))
        assertTrue(repeat.taken)
        assertNull(repeat.binding)

        assertFalse(KeyCapture.keyPressed(ended, key(GLFW.GLFW_KEY_TAB)).taken)

        val released = KeyCapture.keyReleased(ended, key(GLFW.GLFW_KEY_ESCAPE))
        assertTrue(released.taken)
        assertFalse(KeyCapture.keyPressed(released.state, key(GLFW.GLFW_KEY_ESCAPE)).taken)
    }

    /**
     * Verifies that a mouse button pressed while capturing becomes the binding's key.
     */
    @Test
    fun `a mouse button is bound`() {
        val step = KeyCapture.mousePressed(CaptureState(capturing = "hud_cursor"), MouseButtonEvent(0.0, 0.0, MouseButtonInfo(GLFW.GLFW_MOUSE_BUTTON_4, 0)))

        assertTrue(step.taken)
        assertEquals("key.mouse.4", step.key?.name)
        assertNull(step.state.capturing)
    }

    /**
     * Verifies that keys, releases and clicks pass to the panel when nothing is captured or held.
     */
    @Test
    fun `nothing is taken without a capture`() {
        val idle = CaptureState()

        assertFalse(KeyCapture.keyPressed(idle, key(GLFW.GLFW_KEY_R)).taken)
        assertFalse(KeyCapture.keyReleased(idle, key(GLFW.GLFW_KEY_R)).taken)
        assertFalse(KeyCapture.mousePressed(idle, MouseButtonEvent(0.0, 0.0, MouseButtonInfo(GLFW.GLFW_MOUSE_BUTTON_LEFT, 0))).taken)
    }
}
