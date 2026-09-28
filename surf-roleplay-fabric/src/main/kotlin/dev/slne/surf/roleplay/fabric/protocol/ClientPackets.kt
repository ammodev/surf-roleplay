package dev.slne.surf.roleplay.fabric.protocol

import dev.slne.surf.roleplay.fabric.RoleplayClient
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketDirection
import dev.slne.surf.roleplay.protocol.PacketType
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

/**
 * Sends serverbound play-phase packets.
 */
object ClientPackets {

    /**
     * Sends a packet to the server if the server receives its channel, and logs and drops it
     * otherwise.
     *
     * @param type the packet type
     * @param packet the packet
     * @return whether the packet was sent
     * @throws IllegalArgumentException if [type] is not serverbound
     */
    fun <P : Packet> send(type: PacketType<P>, packet: P): Boolean {
        require(type.direction == PacketDirection.SERVERBOUND) { "$type is not serverbound" }
        if (!ClientPlayNetworking.canSend(FabricPacketRegistry.payloadType(type))) {
            RoleplayClient.log.debug("Dropped {}: the server does not receive it", type.channel)
            return false
        }
        ClientPlayNetworking.send(RoleplayPayload(type, packet))
        return true
    }
}
