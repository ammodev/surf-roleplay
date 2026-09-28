package dev.slne.surf.roleplay.paper.screen

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.paper.screen.ScreenService
import dev.slne.surf.roleplay.paper.protocol.PacketHandler
import dev.slne.surf.roleplay.paper.protocol.PaperPacketRegistry
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.screen.ScreenType
import io.papermc.paper.connection.PlayerGameConnection
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val log = logger()

/**
 * The Paper implementation of [ScreenService].
 *
 * It keeps a [PlayerScreenState] per player, sends screen packets through the packet registry,
 * and routes the players' screen packets to their state on the player's own thread. It must be
 * [started][start] by the plugin before use.
 */
@AutoService(ScreenService::class)
class PaperScreenService : ScreenService, Listener {

    /**
     * The screen state of every player who has or had a screen open since joining.
     */
    private val states = ConcurrentHashMap<UUID, PlayerScreenState>()

    /**
     * The plugin that owns the screens, set by [start].
     */
    private lateinit var plugin: Plugin

    /**
     * The packet registry used to send and receive screen packets, set by [start].
     */
    private lateinit var registry: PaperPacketRegistry

    /**
     * The rate limiter shared by every player's actions, set by [start].
     */
    private lateinit var limiter: ActionRateLimiter

    /**
     * Starts the service: registers the handlers of the serverbound screen packets and the quit
     * listener.
     *
     * @param plugin the plugin that owns the screens
     * @param registry the packet registry
     * @param maxActionsPerSecond the largest number of screen actions a player may send per second
     */
    fun start(plugin: Plugin, registry: PaperPacketRegistry, maxActionsPerSecond: Int) {
        this.plugin = plugin
        this.registry = registry
        limiter = ActionRateLimiter(maxActionsPerSecond)
        registry.dispatcher.on(Packets.SCREEN_WIDGET_ACTION, onPlayerThread { state, packet -> report(state, state.handleWidgetAction(packet)) })
        registry.dispatcher.on(Packets.SCREEN_TYPED_ACTION, onPlayerThread { state, packet -> report(state, state.handleTypedAction(packet)) })
        registry.dispatcher.on(Packets.SCREEN_CLOSED, onPlayerThread { state, packet -> state.handleClosed(packet) })
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    /**
     * Creates a packet handler that runs on the sending player's thread with the player's screen
     * state. Packets from players without screen state, or from connections outside the play
     * phase, are dropped.
     *
     * @param P the packet class
     * @param handler the handler
     * @return the packet handler
     */
    private fun <P : Packet> onPlayerThread(handler: (PlayerScreenState, P) -> Unit): PacketHandler<P> =
        PacketHandler { connection, packet ->
            val player = (connection as? PlayerGameConnection)?.player ?: return@PacketHandler
            player.scheduler.run(plugin, {
                val state = states[player.uniqueId] ?: return@run
                synchronized(state) { handler(state, packet) }
            }, null)
        }

    /**
     * Logs a rejected action.
     *
     * @param state the state of the player who sent it
     * @param outcome the outcome of the action
     */
    private fun report(state: PlayerScreenState, outcome: PlayerScreenState.Outcome) {
        if (outcome is PlayerScreenState.Outcome.Rejected) {
            log.atWarning().log("Rejected a screen action of %s: %s", state.viewer, outcome.reason)
        }
    }

    /**
     * Returns the screen state of a player, creating it if needed.
     *
     * @param player the player
     * @return the state
     */
    private fun state(player: Player): PlayerScreenState = states.computeIfAbsent(player.uniqueId) {
        PlayerScreenState(player.uniqueId, sender(player), limiter)
    }

    /**
     * Creates the packet sender of a player.
     *
     * @param player the player
     * @return the sender
     */
    private fun sender(player: Player) = object : ScreenPacketSender {
        /**
         * Sends a packet to the player.
         *
         * @param type the packet type
         * @param packet the packet
         */
        override fun <P : Packet> send(type: PacketType<P>, packet: P) = registry.send(player, type, packet)
    }

    /**
     * Opens a generic screen for a player.
     *
     * @param player the player
     * @param definition the screen
     * @param parent the open screen to open on top of, or `null`
     * @return the open screen
     * @throws IllegalArgumentException if [parent] is not an open screen of [player]
     */
    override fun open(player: Player, definition: ScreenDefinition, parent: OpenScreen?): OpenScreen {
        val state = state(player)
        synchronized(state) {
            return state.open(definition, parentSession(state, player, parent))
        }
    }

    /**
     * Opens a typed screen for a player.
     *
     * @param S the state class
     * @param A the action class
     * @param player the player
     * @param type the screen type
     * @param title the title
     * @param initial the initial state
     * @param closable whether the player can close the screen with Escape
     * @param parent the open screen to open on top of, or `null`
     * @param onAction the handler of the screen's actions
     * @return the open screen
     * @throws IllegalArgumentException if [parent] is not an open screen of [player]
     */
    fun <S, A> openTyped(
        player: Player,
        type: ScreenType<S, A>,
        title: Component,
        initial: S,
        closable: Boolean = true,
        parent: OpenScreen? = null,
        onAction: (TypedOpenScreen<S>, A) -> Unit,
    ): TypedOpenScreen<S> {
        val state = state(player)
        synchronized(state) {
            return state.openTyped(type, title, initial, closable, parentSession(state, player, parent), onAction)
        }
    }

    /**
     * Resolves the parent session of a screen to open.
     *
     * @param state the player's state
     * @param player the player
     * @param parent the parent screen, or `null`
     * @return the parent's session id, or `null`
     * @throws IllegalArgumentException if [parent] is not an open screen of [player]
     */
    private fun parentSession(state: PlayerScreenState, player: Player, parent: OpenScreen?): Int? {
        if (parent == null) return null
        require(parent.isOpen && state.openScreens.any { it === parent }) { "The parent screen is not open for ${player.name}" }
        return parent.sessionId
    }

    /**
     * Returns the screens a player has open.
     *
     * @param player the player
     * @return the open screens, from bottom to top
     */
    override fun openScreens(player: Player): List<OpenScreen> {
        val state = states[player.uniqueId] ?: return emptyList()
        synchronized(state) { return state.openScreens }
    }

    /**
     * Closes every screen a player has open.
     *
     * @param player the player
     */
    override fun closeAll(player: Player) {
        val state = states[player.uniqueId] ?: return
        synchronized(state) { state.closeAll(notifyClient = true) }
    }

    /**
     * Drops the screen state of a player who quits, running the close handlers of their screens.
     *
     * @param event the quit event
     */
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val id = event.player.uniqueId
        limiter.forget(id)
        val state = states.remove(id) ?: return
        synchronized(state) { state.closeAll(notifyClient = false) }
    }

    /**
     * Provides the service instance registered for [ScreenService].
     */
    companion object {
        /**
         * The registered service.
         */
        val INSTANCE: PaperScreenService get() = ScreenService.INSTANCE as PaperScreenService
    }
}
