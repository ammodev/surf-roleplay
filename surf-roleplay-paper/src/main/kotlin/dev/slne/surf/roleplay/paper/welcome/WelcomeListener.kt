package dev.slne.surf.roleplay.paper.welcome

import dev.slne.surf.roleplay.paper.protocol.PaperPacketRegistry
import dev.slne.surf.roleplay.paper.screen.PaperScreenService
import dev.slne.surf.roleplay.protocol.PacketDirection
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.packets.Welcome
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRegisterChannelEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Sends the [Welcome] to every joined player whose client has registered every clientbound
 * roleplay channel, and then marks the player as ready for screens.
 *
 * The client mod registers its play-phase channels after joining, and the server only delivers
 * payloads on channels the client has registered, so the welcome waits for the last of them.
 *
 * @param registry the registry used to send the welcome
 */
class WelcomeListener(private val registry: PaperPacketRegistry) : Listener {

    /**
     * The channels the client must register before it is welcomed.
     */
    private val clientbound: Set<String> = Packets.all.filter { it.direction == PacketDirection.CLIENTBOUND }.map { it.channel }.toSet()

    /**
     * The players who were welcomed since they joined.
     */
    private val welcomed: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    /**
     * Welcomes a player once their client has registered every clientbound roleplay channel.
     *
     * @param event the channel registration event
     */
    @EventHandler
    fun onRegisterChannel(event: PlayerRegisterChannelEvent) {
        val player = event.player
        if (event.channel !in clientbound || !player.listeningPluginChannels.containsAll(clientbound)) return
        if (!welcomed.add(player.uniqueId)) return
        registry.send(player, Packets.WELCOME, Welcome)
        PaperScreenService.INSTANCE.markReady(player)
    }

    /**
     * Forgets a player who quits.
     *
     * @param event the quit event
     */
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        welcomed -= event.player.uniqueId
    }
}
