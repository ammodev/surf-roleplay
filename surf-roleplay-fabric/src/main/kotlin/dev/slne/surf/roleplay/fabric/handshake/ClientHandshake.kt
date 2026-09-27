package dev.slne.surf.roleplay.fabric.handshake

import dev.slne.surf.roleplay.fabric.RoleplayClient
import dev.slne.surf.roleplay.fabric.protocol.RoleplayPayload
import dev.slne.surf.roleplay.protocol.PROTOCOL_VERSION
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.packets.ClientHello
import dev.slne.surf.roleplay.protocol.packets.ModInfo
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking
import net.fabricmc.loader.api.FabricLoader

/**
 * The client side of the mod handshake.
 *
 * At the start of every configuration phase the mod sends a [ClientHello] with its protocol
 * version, its mod version and every top-level mod it has loaded. A server that does not know
 * the hello channel ignores it. A server that rejects the client disconnects it with the reason.
 */
object ClientHandshake {

    /**
     * Registers the listener that sends the hello at the start of every configuration phase.
     */
    fun register() {
        ClientConfigurationConnectionEvents.START.register { _, _ ->
            ClientConfigurationNetworking.send(RoleplayPayload(Packets.HELLO, createHello()))
            RoleplayClient.log.info("Sent roleplay hello (protocol {})", PROTOCOL_VERSION)
        }
    }

    /**
     * Creates the hello for the current client.
     *
     * @return the hello with this build's protocol version, the mod version and the loaded mods
     */
    fun createHello(): ClientHello = ClientHello(
        protocolVersion = PROTOCOL_VERSION,
        modVersion = RoleplayClient.modVersion,
        loadedMods = loadedMods(),
    )

    /**
     * Lists every mod loaded by Fabric that is not nested inside another mod.
     *
     * @return the id and version of each top-level mod, sorted by id
     */
    private fun loadedMods(): List<ModInfo> = FabricLoader.getInstance().allMods
        .filter { it.containingMod.isEmpty }
        .map { ModInfo(it.metadata.id, it.metadata.version.friendlyString) }
        .sortedBy { it.id }
}
