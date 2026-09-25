package dev.slne.surf.roleplay.paper.listener

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.roleplay.core.client.common.user.CoreClientUserManager
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.util.concurrent.ConcurrentHashMap

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
 * Every login whose user is loaded holds the user in the user manager until its connection
 * closes, or until the login is refused after loading. A login is refused if the user cannot be
 * loaded.
 *
 * @param userManager the user manager users are acquired from and released to
 */
class UserConnectionListener(private val userManager: CoreClientUserManager) : Listener {

    /**
     * The pre-login events whose user was loaded and acquired and whose final result has not been
     * seen yet.
     */
    private val acquiredLogins = ConcurrentHashMap.newKeySet<AsyncPlayerPreLoginEvent>()

    /**
     * Loads and acquires the user of the logging-in player, blocking the login thread until the
     * load completes.
     *
     * Logins that are already refused are skipped. If the load fails for any reason, including a
     * cancelled or timed-out load, a warning is logged and the login is refused with a message.
     * If the login thread is interrupted while waiting, the login is refused the same way and the
     * thread's interrupt flag is set again.
     *
     * @param event the pre-login event of the player
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        if (event.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) return

        try {
            runBlocking { userManager.loadAndCache(event.uniqueId) }
            acquiredLogins.add(event)
        } catch (exception: InterruptedException) {
            refuseLogin(event, exception)
            Thread.currentThread().interrupt()
        } catch (exception: Exception) {
            refuseLogin(event, exception)
        }
    }

    /**
     * Logs that the user of the logging-in player could not be loaded and refuses the login with
     * a message.
     *
     * @param event the pre-login event of the player
     * @param cause the failure that prevented the load
     */
    private fun refuseLogin(event: AsyncPlayerPreLoginEvent, cause: Exception) {
        log.atWarning().withCause(cause).log(
            "Could not load roleplay user of %s (%s)", event.name, event.uniqueId
        )
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, LOAD_FAILED_MESSAGE)
    }

    /**
     * Releases the user acquired for the logging-in player if the login was refused after the
     * user was loaded, since no connection close follows a refused login.
     *
     * @param event the pre-login event of the player, with its final result
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onAsyncPlayerPreLoginResult(event: AsyncPlayerPreLoginEvent) {
        val acquired = acquiredLogins.remove(event)
        if (acquired && event.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            userManager.release(event.uniqueId)
        }
    }

    /**
     * Releases the user acquired for the disconnecting player's login.
     *
     * @param event the connection close event of the player
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerConnectionClose(event: PlayerConnectionCloseEvent) {
        userManager.release(event.playerUniqueId)
    }
}
