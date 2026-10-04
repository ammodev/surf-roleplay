package dev.slne.surf.roleplay.fabric.settings

/**
 * The settings of the player on this client.
 *
 * @property cursorKeyMode how the HUD cursor key acts
 */
data class ClientSettings(
    val cursorKeyMode: KeyMode = KeyMode.HOLD,
)
