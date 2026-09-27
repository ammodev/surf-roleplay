package dev.slne.surf.roleplay.fabric.protocol

import dev.slne.surf.roleplay.fabric.RoleplayClient
import dev.slne.surf.roleplay.protocol.ConnectionPhase
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketDirection
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier

/**
 * Registers every packet type in [Packets] with Fabric's payload registry and routes received
 * clientbound packets to [FabricPacketDispatcher].
 *
 * Every packet type is registered for each of its connection phases, in its direction. The
 * payload body is the packet's ProtoBuf encoding without any framing.
 */
object FabricPacketRegistry {

    /**
     * The Minecraft payload type of every packet type, keyed by its channel.
     */
    private val payloadTypes: Map<String, CustomPacketPayload.Type<RoleplayPayload<*>>> =
        Packets.all.associate { it.channel to CustomPacketPayload.Type(Identifier.parse(it.channel)) }

    /**
     * Returns the Minecraft payload type of a packet type.
     *
     * @param type the packet type
     * @return the payload type of [type]'s channel
     */
    @Suppress("UNCHECKED_CAST")
    fun <P : Packet> payloadType(type: PacketType<P>): CustomPacketPayload.Type<RoleplayPayload<P>> =
        payloadTypes.getValue(type.channel) as CustomPacketPayload.Type<RoleplayPayload<P>>

    /**
     * Registers the payload type and codec of every packet type in each of its phases, registers
     * the receivers of every clientbound packet type, and logs the registered channels.
     */
    fun register() {
        for (type in Packets.all) {
            register(type)
        }
        RoleplayClient.log.info("Registered roleplay channels: {}", Packets.all.joinToString { it.channel })
    }

    /**
     * Registers one packet type with Fabric's payload registry for each of its phases and, if it
     * is clientbound, registers a receiver that passes it to [FabricPacketDispatcher].
     *
     * @param type the packet type to register
     */
    private fun <P : Packet> register(type: PacketType<P>) {
        val payloadType = payloadType(type)
        val codec = codec(type)
        for (phase in type.phases) {
            when (type.direction to phase) {
                PacketDirection.SERVERBOUND to ConnectionPhase.CONFIGURATION ->
                    PayloadTypeRegistry.serverboundConfiguration().register(payloadType, codec)

                PacketDirection.CLIENTBOUND to ConnectionPhase.CONFIGURATION -> {
                    PayloadTypeRegistry.clientboundConfiguration().register(payloadType, codec)
                    ClientConfigurationNetworking.registerGlobalReceiver(payloadType) { payload, _ ->
                        FabricPacketDispatcher.dispatch(payload)
                    }
                }

                PacketDirection.SERVERBOUND to ConnectionPhase.PLAY ->
                    PayloadTypeRegistry.serverboundPlay().register(payloadType, codec)

                PacketDirection.CLIENTBOUND to ConnectionPhase.PLAY -> {
                    PayloadTypeRegistry.clientboundPlay().register(payloadType, codec)
                    ClientPlayNetworking.registerGlobalReceiver(payloadType) { payload, _ ->
                        FabricPacketDispatcher.dispatch(payload)
                    }
                }
            }
        }
    }

    /**
     * Creates the stream codec that writes a packet's ProtoBuf encoding as the whole payload body
     * and reads the whole remaining body back as a packet.
     *
     * @param type the packet type the codec handles
     * @return the codec
     */
    private fun <P : Packet> codec(type: PacketType<P>): StreamCodec<FriendlyByteBuf, RoleplayPayload<P>> =
        StreamCodec.of(
            { buf, payload -> buf.writeBytes(ProtocolCodec.encode(type, payload.packet)) },
            { buf ->
                val bytes = ByteArray(buf.readableBytes())
                buf.readBytes(bytes)
                RoleplayPayload(type, ProtocolCodec.decode(type, bytes))
            },
        )
}
