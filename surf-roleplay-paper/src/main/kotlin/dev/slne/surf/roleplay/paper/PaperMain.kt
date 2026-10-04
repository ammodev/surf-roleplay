package dev.slne.surf.roleplay.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.roleplay.api.common.user.UserManager
import dev.slne.surf.roleplay.core.client.common.ClientInstance
import dev.slne.surf.roleplay.core.client.common.user.CoreClientUserManager
import dev.slne.surf.roleplay.paper.crafting.CraftingBlocker
import dev.slne.surf.roleplay.paper.handshake.HandshakeConfig
import dev.slne.surf.roleplay.paper.handshake.HandshakeEvaluator
import dev.slne.surf.roleplay.paper.handshake.HandshakeListener
import dev.slne.surf.roleplay.paper.listener.UserConnectionListener
import dev.slne.surf.roleplay.paper.protocol.PaperPacketRegistry
import dev.slne.surf.roleplay.paper.screen.PaperScreenService
import dev.slne.surf.roleplay.paper.tablist.IdentityProviders
import dev.slne.surf.roleplay.paper.tablist.PaperTabListService
import dev.slne.surf.roleplay.paper.tablist.TabListConfig
import dev.slne.surf.roleplay.paper.tablist.TabListCommand
import dev.slne.surf.roleplay.paper.toast.PaperToastService
import dev.slne.surf.roleplay.paper.storybook.StorybookCommand
import dev.slne.surf.roleplay.paper.welcome.WelcomeListener
import org.bukkit.plugin.java.JavaPlugin

/**
 * The Paper plugin entry point of surf-roleplay, which forwards the plugin lifecycle to the
 * client instance and registers the plugin's listeners.
 */
class PaperMain : SuspendingJavaPlugin() {

    /**
     * The registry of the roleplay payload channels, created when the plugin is enabled.
     */
    lateinit var packetRegistry: PaperPacketRegistry
        private set

    /**
     * Unregisters the user state listener of the built-in tab list providers when closed, set
     * when the plugin is enabled.
     */
    private var identityProviders: AutoCloseable? = null

    /**
     * Loads the client instance.
     */
    override suspend fun onLoadAsync() {
        ClientInstance.INSTANCE.onLoad()
    }

    /**
     * Enables the client instance, registers the roleplay payload channels, the mod handshake, the
     * welcome sender, the screen, toast and tab list services, the storybook command and the vanilla crafting block,
     * registers the listener that acquires a hold on the roleplay user of every player logging in and releases it when the
     * player's connection closes, and registers the built-in tab list providers based on the active identity.
     *
     * @throws IllegalStateException if the registered user manager is not the client user manager
     */
    override suspend fun onEnableAsync() {
        ClientInstance.INSTANCE.onEnable()

        packetRegistry = PaperPacketRegistry(this).also { it.register() }

        saveDefaultConfig()
        val handshakeConfig = HandshakeConfig.from(config)
        server.pluginManager.registerEvents(
            HandshakeListener(packetRegistry, handshakeConfig, HandshakeEvaluator(handshakeConfig.allowedMods)),
            this,
        )
        server.pluginManager.registerEvents(WelcomeListener(packetRegistry), this)
        PaperScreenService.INSTANCE.start(this, packetRegistry, config.getInt("screens.max-actions-per-second", 20))
        PaperToastService.INSTANCE.start(this, packetRegistry, PaperScreenService.INSTANCE.actionLimiter)
        PaperTabListService.INSTANCE.start(this, packetRegistry, TabListConfig.from(config) { logger.warning(it) })
        StorybookCommand.register(this)
        TabListCommand.register(this)
        server.pluginManager.registerEvents(CraftingBlocker(), this)

        val userManager = UserManager.INSTANCE as? CoreClientUserManager
            ?: error(
                "The registered UserManager is ${UserManager.INSTANCE.javaClass.name}, " +
                        "expected ${CoreClientUserManager::class.java.name}"
            )
        server.pluginManager.registerEvents(UserConnectionListener(userManager), this)
        identityProviders = IdentityProviders(userManager::cached).register(PaperTabListService.INSTANCE)
    }

    /**
     * Unregisters the user state listener of the built-in tab list providers and the roleplay
     * payload channels, and disables the client instance.
     */
    override suspend fun onDisableAsync() {
        identityProviders?.close()
        if (::packetRegistry.isInitialized) packetRegistry.unregister()
        ClientInstance.INSTANCE.onDisable()
    }
}

/**
 * The loaded instance of this plugin.
 */
val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)
