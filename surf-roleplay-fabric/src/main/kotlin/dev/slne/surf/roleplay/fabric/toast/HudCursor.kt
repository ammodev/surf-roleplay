package dev.slne.surf.roleplay.fabric.toast

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.MouseHandler
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW

/**
 * The HUD cursor: while its key is held and no screen is open, the mouse is released so that its
 * cursor can click toasts, and the player keeps walking with the movement keys.
 */
object HudCursor {

    /**
     * A change of the mouse capture.
     */
    enum class Change {
        /**
         * Release the mouse and show the cursor.
         */
        RELEASE,

        /**
         * Capture the mouse again.
         */
        CAPTURE,

        /**
         * End the cursor mode without capturing the mouse, because a screen took over.
         */
        END,
    }

    /**
     * The key binding that shows the cursor, left Alt by default, in the roleplay category.
     */
    val key: KeyMapping by lazy {
        KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.surf-roleplay.hud_cursor",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("surf-roleplay", "roleplay")),
            ),
        )
    }

    /**
     * Whether the cursor mode is on.
     */
    var active: Boolean = false
        private set

    /**
     * Decides how the mouse capture changes.
     *
     * @param keyDown whether the key is held
     * @param screenOpen whether a screen is open
     * @param active whether the cursor mode is on
     * @return the change, or `null` for none
     */
    fun change(keyDown: Boolean, screenOpen: Boolean, active: Boolean): Change? = when {
        active && screenOpen -> Change.END
        active && !keyDown -> Change.CAPTURE
        !active && keyDown && !screenOpen -> Change.RELEASE
        else -> null
    }

    /**
     * Applies the key and screen state of the current tick.
     *
     * @param mc the Minecraft client
     * @param enabled whether the cursor may be shown, as while the roleplay server is active
     */
    fun tick(mc: Minecraft, enabled: Boolean) {
        val keyDown = enabled && mc.player != null && key.isDown
        when (change(keyDown, mc.gui.screen() != null, active)) {
            Change.RELEASE -> {
                active = true
                mc.mouseHandler.releaseMouse()
            }
            Change.CAPTURE -> {
                active = false
                mc.mouseHandler.grabMouse()
            }
            Change.END -> active = false
            null -> Unit
        }
    }

    /**
     * Returns the cursor position in GUI pixels.
     *
     * @param mc the Minecraft client
     * @return the x and y position
     */
    fun position(mc: Minecraft): Pair<Double, Double> =
        MouseHandler.getScaledXPos(mc.window, mc.mouseHandler.xpos()) to MouseHandler.getScaledYPos(mc.window, mc.mouseHandler.ypos())

    /**
     * Handles a mouse button while the cursor mode is on and no screen is open: a press is passed
     * to the toasts, and every button event is kept from the world.
     *
     * @param button the mouse button
     * @param action the GLFW action
     * @return whether the event was taken
     */
    @JvmStatic
    fun handleButton(button: Int, action: Int): Boolean {
        val mc = Minecraft.getInstance()
        if (!active || mc.gui.screen() != null) return false
        if (action == GLFW.GLFW_PRESS) {
            val (x, y) = position(mc)
            ToastLayer.click(x, y, button)
        }
        return true
    }
}
