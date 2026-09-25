import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import java.nio.file.Path

class VelocityMain @Inject constructor(
    val proxy: ProxyServer,
    val container: PluginContainer,
    suspendingContainer: SuspendingPluginContainer,
    @DataDirectory val dataPath: Path,
) {
    init {
        suspendingContainer.initialize(this)
    }

    @Subscribe
    suspend fun onProxyInitialize(event: ProxyInitializeEvent) {
        proxy.consoleCommandSource.sendPlainMessage("surf-roleplay enabled")
    }
}