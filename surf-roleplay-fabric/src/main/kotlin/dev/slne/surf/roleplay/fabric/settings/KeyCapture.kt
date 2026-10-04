package dev.slne.surf.roleplay.fabric.settings

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

/**
 * The key capture state of the settings screen.
 *
 * @property capturing the id of the binding that waits for a new key, or `null` if none does
 * @property heldKey the GLFW code of the key that ended the last capture while it is still held,
 *           or `null`
 */
data class CaptureState(
    val capturing: String? = null,
    val heldKey: Int? = null,
)

/**
 * The result of one input event during key capture.
 *
 * @property state the capture state after the event
 * @property taken whether the event is used by the capture and must not reach the panels
 * @property binding the id of the binding that gets a new key, or `null` if none does
 * @property key the new key of [binding], or `null` if no binding changes
 */
data class CaptureStep(
    val state: CaptureState,
    val taken: Boolean,
    val binding: String? = null,
    val key: InputConstants.Key? = null,
)

/**
 * Captures a new key for a binding of the settings screen.
 *
 * While a binding waits, the next key press or mouse button becomes its key; Escape unbinds it.
 * The key that ended a capture is then kept from the panels, including its repeats, until it is
 * released, so that holding it does not also act on the screen.
 */
object KeyCapture {

    /**
     * Handles a key press or key repeat.
     *
     * @param state the current state
     * @param event the key event
     * @return the result
     */
    fun keyPressed(state: CaptureState, event: KeyEvent): CaptureStep {
        if (state.heldKey == event.key()) return CaptureStep(state, taken = true)
        val id = state.capturing ?: return CaptureStep(state, taken = false)
        val key = if (event.isEscape) InputConstants.UNKNOWN else InputConstants.getKey(event)
        return CaptureStep(CaptureState(capturing = null, heldKey = event.key()), taken = true, binding = id, key = key)
    }

    /**
     * Handles a key release; the release of the key that ended a capture is taken.
     *
     * @param state the current state
     * @param event the key event
     * @return the result
     */
    fun keyReleased(state: CaptureState, event: KeyEvent): CaptureStep =
        if (state.heldKey == event.key()) CaptureStep(state.copy(heldKey = null), taken = true) else CaptureStep(state, taken = false)

    /**
     * Handles a mouse button press.
     *
     * @param state the current state
     * @param event the mouse event
     * @return the result
     */
    fun mousePressed(state: CaptureState, event: MouseButtonEvent): CaptureStep {
        val id = state.capturing ?: return CaptureStep(state, taken = false)
        return CaptureStep(state.copy(capturing = null), taken = true, binding = id, key = InputConstants.Type.MOUSE.getOrCreate(event.button()))
    }
}
