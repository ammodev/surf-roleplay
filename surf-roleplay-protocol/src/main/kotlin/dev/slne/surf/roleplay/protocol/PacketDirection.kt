package dev.slne.surf.roleplay.protocol

/**
 * The side a packet travels to.
 */
enum class PacketDirection {
    /**
     * Sent by the server and received by the client mod.
     */
    CLIENTBOUND,

    /**
     * Sent by the client mod and received by the server.
     */
    SERVERBOUND,
}
