package dev.slne.surf.roleplay.fabric.server

import dev.slne.surf.roleplay.fabric.RoleplayClient
import dev.slne.surf.roleplay.fabric.protocol.FabricPacketDispatcher
import dev.slne.surf.roleplay.protocol.Packets
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

/**
 * Drives a [RoleplayServerState] from the connection lifecycle.
 *
 * The state becomes active when the server's welcome arrives, and is reset when the client
 * disconnects or enters a new configuration phase, such as when a proxy moves it to another
 * server.
 */
object RoleplayServerDetection {

    /**
     * Registers the welcome handler and the lifecycle listeners that update [state], and logs every
     * change of it.
     *
     * @param state the state to drive
     */
    fun register(state: RoleplayServerState) {
        state.onChange { active -> RoleplayClient.log.info("Roleplay server active: {}", active) }
        FabricPacketDispatcher.on(Packets.WELCOME) { state.activate() }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> state.reset() }
        ClientConfigurationConnectionEvents.START.register { _, _ -> state.reset() }
    }
}
