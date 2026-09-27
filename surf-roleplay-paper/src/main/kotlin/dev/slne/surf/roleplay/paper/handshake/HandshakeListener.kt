package dev.slne.surf.roleplay.paper.handshake

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.roleplay.paper.protocol.PaperPacketRegistry
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.packets.ClientHello
import io.papermc.paper.connection.PlayerConfigurationConnection
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.UUID
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

private val log = logger()

/**
 * Runs the mod handshake for every connection in the configuration phase.
 *
 * Each configuring connection is held until the client mod's [ClientHello] arrives or the
 * configured timeout expires. The hello is then evaluated: an accepted client continues into the
 * world, and a rejected client is disconnected with a German message. A hello may arrive before
 * or after the connection is held.
 *
 * @param registry the registry used to receive hellos
 * @param config the handshake settings
 * @param evaluator the evaluator that decides whether a client may join
 */
class HandshakeListener(
    private val registry: PaperPacketRegistry,
    private val config: HandshakeConfig,
    private val evaluator: HandshakeEvaluator,
) : Listener {

    /**
     * The hello of every configuring connection, keyed by the profile id of its player. A future
     * is created by whichever comes first: the hello or the configure event.
     */
    private val hellos = ConcurrentHashMap<UUID, CompletableFuture<ClientHello>>()

    init {
        registry.dispatcher.on(Packets.HELLO) { connection, hello ->
            val profileId = (connection as? PlayerConfigurationConnection)?.profile?.id ?: return@on
            helloFuture(profileId).complete(hello)
        }
    }

    /**
     * Holds a configuring connection until its hello arrives or the timeout expires, and then
     * accepts or disconnects it. A connection that closed while waiting is left alone.
     *
     * @param event the configure event of the connection
     */
    @EventHandler
    fun onConfigure(event: AsyncPlayerConnectionConfigureEvent) {
        val connection = event.connection
        val profile = connection.profile
        val profileId = profile.id ?: return

        val hello = try {
            awaitHello(profileId)
        } finally {
            hellos.remove(profileId)
        }
        if (!connection.isConnected) return

        val outcome = evaluator.evaluate(hello)
        val message = HandshakeMessages.disconnectMessage(outcome) ?: return

        log.atInfo().log("Rejected %s (%s) in the mod handshake: %s", profile.name, profileId, outcome)
        connection.disconnect(message)
    }

    /**
     * Drops the pending hello of a connection that closes before its handshake finished.
     *
     * @param event the connection close event
     */
    @EventHandler
    fun onConnectionClose(event: PlayerConnectionCloseEvent) {
        hellos.remove(event.playerUniqueId)?.cancel(false)
    }

    /**
     * Waits for the hello of a player until the configured timeout expires.
     *
     * @param profileId the profile id of the player
     * @return the hello, or `null` if none arrived in time or the wait was cancelled or
     *         interrupted
     */
    private fun awaitHello(profileId: UUID): ClientHello? = try {
        helloFuture(profileId).get(config.timeout.toMillis(), TimeUnit.MILLISECONDS)
    } catch (_: TimeoutException) {
        null
    } catch (_: ExecutionException) {
        null
    } catch (_: CancellationException) {
        null
    } catch (_: InterruptedException) {
        Thread.currentThread().interrupt()
        null
    }

    /**
     * Returns the future of a player's hello, creating it if neither the hello nor the configure
     * event has created it yet.
     *
     * @param profileId the profile id of the player
     * @return the future that completes with the player's hello
     */
    private fun helloFuture(profileId: UUID): CompletableFuture<ClientHello> =
        hellos.computeIfAbsent(profileId) { CompletableFuture() }
}
