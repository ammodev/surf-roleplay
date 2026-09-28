package dev.slne.surf.roleplay.fabric

import dev.slne.surf.roleplay.fabric.handshake.ClientHandshake
import dev.slne.surf.roleplay.fabric.protocol.FabricPacketRegistry
import dev.slne.surf.roleplay.fabric.screen.ClientScreenManager
import dev.slne.surf.roleplay.fabric.server.RoleplayServerDetection
import dev.slne.surf.roleplay.fabric.server.RoleplayServerState
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory

/**
 * The client entrypoint of the surf roleplay mod.
 */
object RoleplayClient : ClientModInitializer {

    /**
     * The mod id of the surf roleplay mod.
     */
    const val MOD_ID: String = "surf-roleplay"

    /**
     * The logger of the surf roleplay mod.
     */
    val log = LoggerFactory.getLogger(MOD_ID)

    /**
     * The version of this mod, as declared in its `fabric.mod.json`.
     */
    val modVersion: String by lazy {
        FabricLoader.getInstance().getModContainer(MOD_ID)
            .map { it.metadata.version.friendlyString }
            .orElse("unknown")
    }

    /**
     * Whether the current connection is to the roleplay server.
     */
    val serverState: RoleplayServerState = RoleplayServerState()

    /**
     * Initialises the mod on the client: registers the roleplay payload channels, the mod
     * handshake, the roleplay server detection and the server-driven screens, and logs the mod
     * version.
     */
    override fun onInitializeClient() {
        FabricPacketRegistry.register()
        ClientHandshake.register()
        RoleplayServerDetection.register(serverState)
        ClientScreenManager.register(serverState)
        log.info("Surf Roleplay {} initialised", modVersion)
    }
}
