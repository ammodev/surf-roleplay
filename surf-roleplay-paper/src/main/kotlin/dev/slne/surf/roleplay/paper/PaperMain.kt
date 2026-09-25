package dev.slne.surf.roleplay.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.roleplay.api.common.user.UserManager
import dev.slne.surf.roleplay.core.client.common.ClientInstance
import dev.slne.surf.roleplay.core.client.common.user.CoreClientUserManager
import dev.slne.surf.roleplay.paper.listener.UserConnectionListener
import org.bukkit.plugin.java.JavaPlugin

/**
 * The Paper plugin entry point of surf-roleplay, which forwards the plugin lifecycle to the
 * client instance and registers the plugin's listeners.
 */
class PaperMain : SuspendingJavaPlugin() {

    /**
     * Loads the client instance.
     */
    override suspend fun onLoadAsync() {
        ClientInstance.INSTANCE.onLoad()
    }

    /**
     * Enables the client instance and registers the listener that acquires a hold on the roleplay
     * user of every player logging in and releases it when the player's connection closes.
     *
     * @throws IllegalStateException if the registered user manager is not the client user manager
     */
    override suspend fun onEnableAsync() {
        ClientInstance.INSTANCE.onEnable()

        val userManager = UserManager.INSTANCE as? CoreClientUserManager
            ?: error(
                "The registered UserManager is ${UserManager.INSTANCE.javaClass.name}, " +
                        "expected ${CoreClientUserManager::class.java.name}"
            )
        server.pluginManager.registerEvents(UserConnectionListener(userManager), this)
    }

    /**
     * Disables the client instance.
     */
    override suspend fun onDisableAsync() {
        ClientInstance.INSTANCE.onDisable()
    }
}

/**
 * The loaded instance of this plugin.
 */
val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)
