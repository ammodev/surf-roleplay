package dev.slne.surf.roleplay.paper.welcome

import dev.slne.surf.roleplay.paper.protocol.PaperPacketRegistry
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.packets.Welcome
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRegisterChannelEvent

/**
 * Sends the [Welcome] to every joined player whose client registers the welcome channel.
 *
 * The client mod registers its play-phase channels after joining, and the server only delivers
 * payloads on channels the client has registered, so the welcome is sent at that moment.
 *
 * @param registry the registry used to send the welcome
 */
class WelcomeListener(private val registry: PaperPacketRegistry) : Listener {

    /**
     * Sends the welcome when a player's client registers the welcome channel.
     *
     * @param event the channel registration event
     */
    @EventHandler
    fun onRegisterChannel(event: PlayerRegisterChannelEvent) {
        if (event.channel != Packets.WELCOME.channel) return
        registry.send(event.player, Packets.WELCOME, Welcome)
    }
}
