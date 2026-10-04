package dev.slne.surf.roleplay.fabric

import dev.slne.surf.roleplay.fabric.handshake.ClientHandshake
import dev.slne.surf.roleplay.fabric.protocol.FabricPacketRegistry
import dev.slne.surf.roleplay.fabric.screen.ClientScreenManager
import dev.slne.surf.roleplay.fabric.server.RoleplayServerDetection
import dev.slne.surf.roleplay.fabric.server.RoleplayServerState
import dev.slne.surf.roleplay.fabric.settings.SettingsEntry
import dev.slne.surf.roleplay.fabric.settings.SettingsScreen
import dev.slne.surf.roleplay.fabric.settings.SettingsStore
import dev.slne.surf.roleplay.fabric.toast.ToastLayer
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
     * The client settings, stored in `surf-roleplay.json` in the config directory.
     */
    val settings: SettingsStore by lazy {
        SettingsStore(FabricLoader.getInstance().configDir.resolve("surf-roleplay.json"))
    }

    /**
     * Initialises the mod on the client: loads the client settings, registers the roleplay payload channels, the mod
     * handshake, the roleplay server detection, the server-driven screens, the toasts and the
     * settings screen with its key binding and pause menu button, and logs the mod version.
     */
    override fun onInitializeClient() {
        settings.load()
        FabricPacketRegistry.register()
        ClientHandshake.register()
        RoleplayServerDetection.register(serverState)
        ClientScreenManager.register(serverState)
        ToastLayer.register(serverState)
        SettingsScreen.register(serverState)
        SettingsEntry.register(serverState)
        log.info("Surf Roleplay {} initialised", modVersion)
    }
}
