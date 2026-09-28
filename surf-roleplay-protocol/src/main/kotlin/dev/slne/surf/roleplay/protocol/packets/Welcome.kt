package dev.slne.surf.roleplay.protocol.packets

import dev.slne.surf.roleplay.protocol.Packet
import kotlinx.serialization.Serializable

/**
 * Sent by the roleplay server once a player is in the world, to mark the server as the roleplay
 * server for the client mod.
 *
 * The mod enables its roleplay behaviour only after receiving this packet on the current
 * connection.
 */
@Serializable
data object Welcome : Packet
