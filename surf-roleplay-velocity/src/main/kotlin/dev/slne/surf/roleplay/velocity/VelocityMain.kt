package dev.slne.surf.roleplay.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import dev.slne.surf.roleplay.core.client.common.ClientInstance
import kotlinx.coroutines.runBlocking
import java.nio.file.Path

/**
 * The Velocity plugin entry point of surf-roleplay, which forwards the proxy lifecycle to the
 * client instance.
 *
 * @property proxy the Velocity proxy server this plugin is loaded into
 * @property container the plugin container Velocity created for this plugin
 * @property dataPath the data directory injected for this plugin
 */
class VelocityMain @Inject constructor(
    val proxy: ProxyServer,
    val container: PluginContainer,
    suspendingContainer: SuspendingPluginContainer,
    @DataDirectory val dataPath: Path,
) {
    init {
        instance = this
        suspendingContainer.initialize(this)

        runBlocking {
            ClientInstance.INSTANCE.onLoad()
        }
    }

    /**
     * Enables the client instance once the proxy has initialized.
     */
    @Subscribe
    suspend fun onProxyInitialize(event: ProxyInitializeEvent) {
        ClientInstance.INSTANCE.onEnable()
    }

    /**
     * Disables the client instance as the proxy shuts down.
     */
    @Subscribe
    suspend fun onProxyShutdown(event: ProxyShutdownEvent) {
        ClientInstance.INSTANCE.onDisable()
    }

    /**
     * Holds the loaded instance of this plugin.
     */
    companion object {
        /**
         * The loaded instance of this plugin.
         */
        lateinit var instance: VelocityMain
            private set
    }
}

/**
 * The loaded instance of this plugin.
 */
val plugin get() = VelocityMain.instance

/**
 * The plugin container Velocity created for this plugin.
 */
val container get() = plugin.container

/**
 * The Velocity proxy server this plugin is loaded into.
 */
val proxy get() = plugin.proxy
