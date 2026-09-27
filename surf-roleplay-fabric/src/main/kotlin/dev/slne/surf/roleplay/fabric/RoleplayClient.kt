package dev.slne.surf.roleplay.fabric

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
     * Initialises the mod on the client and logs its version.
     */
    override fun onInitializeClient() {
        log.info("Surf Roleplay {} initialised", modVersion)
    }
}
