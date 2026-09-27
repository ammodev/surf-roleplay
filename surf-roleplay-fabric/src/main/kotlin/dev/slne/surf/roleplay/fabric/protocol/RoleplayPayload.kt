package dev.slne.surf.roleplay.fabric.protocol

import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

/**
 * A roleplay [Packet] wrapped as a Minecraft custom payload, so that Fabric's networking can
 * send and receive it on the packet's channel.
 *
 * @param P the packet class
 * @property packetType the packet type of [packet]
 * @property packet the wrapped packet
 */
class RoleplayPayload<P : Packet>(
    val packetType: PacketType<P>,
    val packet: P,
) : CustomPacketPayload {

    /**
     * Returns the Minecraft payload type registered for [packetType].
     *
     * @return the payload type of this payload's channel
     */
    override fun type(): CustomPacketPayload.Type<RoleplayPayload<P>> = FabricPacketRegistry.payloadType(packetType)
}
