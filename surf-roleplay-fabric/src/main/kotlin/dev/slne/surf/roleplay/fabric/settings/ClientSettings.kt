package dev.slne.surf.roleplay.fabric.settings

/**
 * The settings of the player on this client.
 *
 * @property cursorKeyMode how the HUD cursor key acts
 * @property radialKeyMode how the radial menu key acts
 */
data class ClientSettings(
    val cursorKeyMode: KeyMode = KeyMode.HOLD,
    val radialKeyMode: KeyMode = KeyMode.HOLD,
)
