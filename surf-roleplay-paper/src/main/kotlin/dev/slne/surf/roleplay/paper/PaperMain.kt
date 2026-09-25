package dev.slne.surf.roleplay.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.roleplay.core.client.common.ClientInstance
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        ClientInstance.INSTANCE.onLoad()
    }

    override suspend fun onEnableAsync() {
        ClientInstance.INSTANCE.onEnable()
    }

    override suspend fun onDisableAsync() {
        ClientInstance.INSTANCE.onDisable()
    }
}

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)