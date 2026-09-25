package dev.slne.surf.roleplay.paper.listener

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.roleplay.core.client.common.user.CoreClientUserManager
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import kotlin.coroutines.cancellation.CancellationException

private val log = logger()

/**
 * The message shown to a player whose login is refused because their roleplay user could not be
 * loaded.
 */
private val LOAD_FAILED_MESSAGE =
    Component.text("Deine Roleplay-Daten konnten nicht geladen werden. Bitte versuche es später erneut.")

/**
 * Keeps the roleplay user of every connected player in memory.
 *
 * The user is loaded while the player logs in and removed from memory when the connection
 * closes. A login is refused if the user cannot be loaded.
 *
 * @param userManager the user manager users are loaded into and evicted from
 */
class UserConnectionListener(private val userManager: CoreClientUserManager) : Listener {

    /**
     * Loads the user of the logging-in player and keeps it in memory, blocking the login thread
     * until the load completes.
     *
     * Logins that are already refused are skipped. If the load fails, a warning is logged and the
     * login is refused with a message.
     *
     * @param event the pre-login event of the player
     * @throws InterruptedException if the login thread is interrupted while waiting for the load
     * @throws CancellationException if the load is cancelled
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        if (event.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) return

        try {
            runBlocking { userManager.loadAndCache(event.uniqueId) }
        } catch (exception: InterruptedException) {
            throw exception
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            log.atWarning().withCause(exception).log(
                "Could not load roleplay user of %s (%s)", event.name, event.uniqueId
            )
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, LOAD_FAILED_MESSAGE)
        }
    }

    /**
     * Removes the user of the logging-in player from memory if the login was refused after the
     * user was loaded, since no connection close follows a refused login.
     *
     * Nothing is removed while a player with the same UUID is still online on this server.
     *
     * @param event the pre-login event of the player, with its final result
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onAsyncPlayerPreLoginResult(event: AsyncPlayerPreLoginEvent) {
        if (event.loginResult == AsyncPlayerPreLoginEvent.Result.ALLOWED) return
        if (Bukkit.getPlayer(event.uniqueId) != null) return

        userManager.evict(event.uniqueId)
    }

    /**
     * Removes the user of the disconnecting player from memory.
     *
     * @param event the connection close event of the player
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerConnectionClose(event: PlayerConnectionCloseEvent) {
        userManager.evict(event.playerUniqueId)
    }
}
