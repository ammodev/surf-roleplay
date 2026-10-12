package dev.slne.surf.roleplay.fabric.interaction

import dev.slne.surf.roleplay.fabric.server.RoleplayServerState
import dev.slne.surf.roleplay.fabric.protocol.FabricPacketDispatcher
import dev.slne.surf.roleplay.protocol.Packets
import net.minecraft.client.Minecraft

/**
 * Whether the server allows the target eye and the radial menu right now.
 */
object InteractionClientState {

    /**
     * Whether the target eye and the radial menu may be shown; `false` until the server says so.
     */
    var available: Boolean = false
        private set

    /**
     * Registers the availability packet and resets the state when the roleplay server is left.
     *
     * @param state the roleplay server state
     */
    fun register(state: RoleplayServerState) {
        FabricPacketDispatcher.on(Packets.INTERACTION_AVAILABILITY) { packet -> Minecraft.getInstance().execute { available = packet.available && state.isActive } }
        state.onChange { active -> if (!active) Minecraft.getInstance().execute { available = false } }
    }
}
