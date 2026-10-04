package dev.slne.surf.roleplay.fabric.settings

import com.mojang.blaze3d.platform.InputConstants
import dev.slne.surf.roleplay.fabric.server.RoleplayServerState
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.PauseScreen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

/**
 * The ways into the [SettingsScreen]: an unbound key binding in the roleplay category and a
 * button in the pause menu. Both work only while the roleplay server is active.
 */
object SettingsEntry {

    /**
     * The x position of the pause menu button, in GUI pixels.
     */
    private const val BUTTON_X = 4

    /**
     * The distance of the pause menu button's top edge from the bottom of the window, in GUI pixels.
     */
    private const val BUTTON_BOTTOM_OFFSET = 24

    /**
     * The width of the pause menu button, in GUI pixels.
     */
    private const val BUTTON_WIDTH = 120

    /**
     * The height of the pause menu button, in GUI pixels.
     */
    private const val BUTTON_HEIGHT = 20

    /**
     * The key binding that opens the settings screen, unbound by default.
     */
    val key: KeyMapping by lazy {
        KeyMappingHelper.registerKeyMapping(
            KeyMapping("key.surf-roleplay.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, RoleplayKeys.category),
        )
    }

    /**
     * Registers the key binding, opens the settings screen when it is pressed, and adds the
     * settings button to the pause menu.
     *
     * @param state the roleplay server state
     */
    fun register(state: RoleplayServerState) {
        key
        ClientTickEvents.END_CLIENT_TICK.register {
            while (key.consumeClick()) {
                if (state.isActive) SettingsScreen.open()
            }
        }
        ScreenEvents.AFTER_INIT.register { _, screen, _, height ->
            if (screen is PauseScreen && screen.showsPauseMenu() && state.isActive) {
                Screens.getWidgets(screen).add(
                    Button.builder(Component.literal(SettingsView.TITLE)) { SettingsScreen.open() }
                        .bounds(BUTTON_X, height - BUTTON_BOTTOM_OFFSET, BUTTON_WIDTH, BUTTON_HEIGHT)
                        .build(),
                )
            }
        }
    }
}
