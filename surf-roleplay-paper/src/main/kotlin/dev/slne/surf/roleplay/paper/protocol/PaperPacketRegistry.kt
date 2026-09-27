package dev.slne.surf.roleplay.paper.protocol

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketDirection
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.ProtocolCodec
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.messaging.PluginMessageRecipient

private val log = logger()

/**
 * Registers the payload channel of every packet type in [Packets] with the server's messenger
 * and sends clientbound packets.
 *
 * Serverbound channels are registered as incoming channels that feed [dispatcher]; clientbound
 * channels are registered as outgoing channels.
 *
 * @property plugin the plugin that owns the channels
 * @property dispatcher the dispatcher that receives every serverbound payload
 */
class PaperPacketRegistry(
    private val plugin: Plugin,
    val dispatcher: PaperPacketDispatcher = PaperPacketDispatcher(),
) {

    /**
     * Registers the channel of every packet type with the server's messenger and logs the
     * registered channels.
     */
    fun register() {
        val messenger = plugin.server.messenger
        for (type in Packets.all) {
            when (type.direction) {
                PacketDirection.SERVERBOUND -> messenger.registerIncomingPluginChannel(plugin, type.channel, dispatcher)
                PacketDirection.CLIENTBOUND -> messenger.registerOutgoingPluginChannel(plugin, type.channel)
            }
        }
        log.atInfo().log("Registered roleplay channels: %s", Packets.all.joinToString { it.channel })
    }

    /**
     * Unregisters every channel this plugin registered.
     */
    fun unregister() {
        val messenger = plugin.server.messenger
        messenger.unregisterIncomingPluginChannel(plugin)
        messenger.unregisterOutgoingPluginChannel(plugin)
    }

    /**
     * Encodes a clientbound packet and sends it to a player or connection.
     *
     * @param recipient the joined player or the configuring connection to send to
     * @param type the packet type of [packet]
     * @param packet the packet to send
     * @throws IllegalArgumentException if [type] is not clientbound
     */
    fun <P : Packet> send(recipient: PluginMessageRecipient, type: PacketType<P>, packet: P) {
        require(type.direction == PacketDirection.CLIENTBOUND) { "$type is not clientbound" }
        recipient.sendPluginMessage(plugin, type.channel, ProtocolCodec.encode(type, packet))
    }
}
