package dev.slne.surf.roleplay.fabric.settings

/**
 * How a key binding acts: active only while the key is held, or switched on and off by each press.
 */
enum class KeyMode {
    /**
     * The effect lasts while the key is held.
     */
    HOLD,

    /**
     * Each key press switches the effect on or off.
     */
    TOGGLE,
}
