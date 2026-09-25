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

    @Subscribe
    suspend fun onProxyInitialize(event: ProxyInitializeEvent) {
        ClientInstance.INSTANCE.onEnable()
    }

    @Subscribe
    suspend fun onProxyShutdown(event: ProxyShutdownEvent) {
        ClientInstance.INSTANCE.onDisable()
    }

    companion object {
        lateinit var instance: VelocityMain
            private set
    }
}

val plugin get() = VelocityMain.instance
val container get() = plugin.container
val proxy get() = plugin.proxy