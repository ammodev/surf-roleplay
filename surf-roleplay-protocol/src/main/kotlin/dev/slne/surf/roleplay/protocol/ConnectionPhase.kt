package dev.slne.surf.roleplay.protocol

/**
 * A phase of a Minecraft connection in which custom payloads can be exchanged.
 */
enum class ConnectionPhase {
    /**
     * The configuration phase, before the player enters the world.
     */
    CONFIGURATION,

    /**
     * The play phase, while the player is in the world.
     */
    PLAY,
}
