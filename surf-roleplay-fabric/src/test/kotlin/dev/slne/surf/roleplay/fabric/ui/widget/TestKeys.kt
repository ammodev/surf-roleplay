package dev.slne.surf.roleplay.fabric.ui.widget

import net.minecraft.client.input.InputQuirks
import net.minecraft.client.input.KeyEvent
import org.lwjgl.glfw.GLFW

/**
 * Builds key events for tests, with the editing shortcut modifier of the platform the tests run
 * on: Command on macOS, Control everywhere else.
 */
object TestKeys {

    /**
     * Returns a key event.
     *
     * @param key the GLFW key code
     * @param shortcut whether the editing shortcut modifier is held
     * @param shift whether Shift is held
     * @return the event
     */
    fun key(key: Int, shortcut: Boolean = false, shift: Boolean = false): KeyEvent =
        KeyEvent(key, 0, (if (shortcut) InputQuirks.EDIT_SHORTCUT_KEY_MODIFIER else 0) or (if (shift) GLFW.GLFW_MOD_SHIFT else 0))
}
