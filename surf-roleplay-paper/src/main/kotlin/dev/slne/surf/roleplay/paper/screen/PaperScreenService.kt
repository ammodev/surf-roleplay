package dev.slne.surf.roleplay.paper.screen

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.roleplay.api.client.common.screen.OpenScreen
import dev.slne.surf.roleplay.api.client.common.screen.ScreenDefinition
import dev.slne.surf.roleplay.api.client.common.screen.ScreenPresentation
import dev.slne.surf.roleplay.api.client.common.screen.SheetSide
import dev.slne.surf.roleplay.api.client.paper.screen.ScreenService
import dev.slne.surf.roleplay.paper.protocol.PacketHandler
import dev.slne.surf.roleplay.paper.protocol.PaperPacketRegistry
import dev.slne.surf.roleplay.protocol.Packet
import dev.slne.surf.roleplay.protocol.PacketType
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.screen.ScreenType
import io.papermc.paper.connection.PlayerGameConnection
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
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
 * It keeps a [PlayerScreenState] per player and routes the players' screen packets to their state
 * on the player's own thread. Every call must happen on the thread that owns the player. Screens
 * opened before the player's client is ready are sent once it is. It must be [started][start] by
 * the plugin before use.
 */
@AutoService(ScreenService::class)
class PaperScreenService : ScreenService, Listener {

    /**
     * The screen state of every player who opened a screen since joining.
     */
    private val states = ConcurrentHashMap<UUID, PlayerScreenState>()

    /**
     * The queueing sender of every player's screen state.
     */
    private val senders = ConcurrentHashMap<UUID, QueueingScreenSender>()

    /**
     * The players whose client can receive screen packets.
     */
    private val ready: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

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
     * The limiter of rejection log entries.
     */
    private val rejections = RejectionLog(REJECTIONS_LOGGED_PER_MINUTE)

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
        registry.dispatcher.on(Packets.SCREEN_CLOSED, onPlayerThread { state, packet -> report(state, state.handleClosed(packet)) })
        registry.dispatcher.on(Packets.SCREEN_INPUT_CHANGE, onPlayerThread { state, packet -> report(state, state.handleInputChange(packet)) })
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    /**
     * Marks a player's client as ready for screen packets and sends the screens opened for the
     * player before.
     *
     * @param player the player
     */
    fun markReady(player: Player) {
        ready += player.uniqueId
        senders[player.uniqueId]?.markReady()
    }

    /**
     * Checks whether a player's client can receive roleplay packets.
     *
     * @param player the player
     * @return whether the client was welcomed and has not left since
     */
    fun isReady(player: Player): Boolean = player.uniqueId in ready

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
                handler(state, packet)
            }, null)
        }

    /**
     * Logs a rejected action: suspicious rejections as warnings, within the player's log budget,
     * and rejections caused by ordinary latency at the fine level.
     *
     * @param state the state of the player who sent it
     * @param outcome the outcome of the action
     */
    private fun report(state: PlayerScreenState, outcome: PlayerScreenState.Outcome) {
        if (outcome !is PlayerScreenState.Outcome.Rejected) return
        if (!outcome.suspicious) {
            log.atFine().log("Dropped a stale screen action of %s: %s", state.viewer, outcome.reason)
            return
        }
        when (rejections.decide(state.viewer)) {
            RejectionLog.Decision.LOG -> log.atWarning().log("Rejected a screen action of %s: %s", state.viewer, outcome.reason)
            RejectionLog.Decision.SUPPRESS_NOTICE ->
                log.atWarning().log("Suppressing further rejected screen actions of %s for this minute", state.viewer)

            RejectionLog.Decision.SILENT -> Unit
        }
    }

    /**
     * Returns the screen state of a player, creating it if needed.
     *
     * @param player the player
     * @return the state
     * @throws IllegalStateException if the calling thread does not own the player
     */
    private fun state(player: Player): PlayerScreenState {
        checkOwned(player)
        return states.computeIfAbsent(player.uniqueId) { id ->
            val sender = QueueingScreenSender(registrySender(player), ready = id in ready)
            senders[id] = sender
            PlayerScreenState(id, sender, limiter) { checkOwned(player) }
        }
    }

    /**
     * Checks that the calling thread owns a player.
     *
     * @param player the player
     * @throws IllegalStateException if it does not
     */
    private fun checkOwned(player: Player) {
        check(Bukkit.isOwnedByCurrentRegion(player)) { "Screens of ${player.name} must be used on the player's region thread" }
    }

    /**
     * Creates the sender that delivers packets to a player through the packet registry.
     *
     * @param player the player
     * @return the sender
     */
    private fun registrySender(player: Player) = object : ScreenPacketSender {
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
     * @param presentation how the screen is shown relative to the screens below it
     * @param sheetSide the window edge a sheet is attached to
     * @return the open screen
     * @throws IllegalArgumentException if [parent] is not an open screen of [player]
     * @throws IllegalStateException if the calling thread does not own the player, or the player
     *         is leaving
     */
    override fun open(
        player: Player,
        definition: ScreenDefinition,
        parent: OpenScreen?,
        presentation: ScreenPresentation,
        sheetSide: SheetSide,
    ): OpenScreen {
        val state = state(player)
        return state.open(definition, parentSession(state, player, parent), presentation, sheetSide)
    }

    /**
     * Opens a confirmation dialog for a player over a parent screen.
     *
     * @param player the player
     * @param parent the open screen to show the dialog over, or `null`
     * @param title the dialog's title
     * @param text the question
     * @param confirmLabel the caption of the confirm button
     * @param cancelLabel the caption of the cancel button
     * @param destructive whether confirming destroys something
     * @param onConfirm the handler run on confirmation
     * @param onCancel the handler run when the dialog closes without confirmation
     * @return the open dialog
     * @throws IllegalArgumentException if [parent] is not an open screen of [player]
     * @throws IllegalStateException if the calling thread does not own the player, or the player
     *         is leaving
     */
    override fun confirm(
        player: Player,
        parent: OpenScreen?,
        title: Component,
        text: Component,
        confirmLabel: Component,
        cancelLabel: Component,
        destructive: Boolean,
        onConfirm: () -> Unit,
        onCancel: () -> Unit,
    ): OpenScreen {
        val state = state(player)
        return ConfirmDialog.open(state, parentSession(state, player, parent), title, text, confirmLabel, cancelLabel, destructive, onConfirm, onCancel)
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
     * @throws IllegalStateException if the calling thread does not own the player, or the player
     *         is leaving
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
        return state.openTyped(type, title, initial, closable, parentSession(state, player, parent), onAction)
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
        require(state.openScreens.any { it === parent }) { "The parent screen is not open for ${player.name}" }
        return parent.sessionId
    }

    /**
     * Returns the screens a player has open.
     *
     * @param player the player
     * @return the open screens, from bottom to top
     * @throws IllegalStateException if the calling thread does not own the player
     */
    override fun openScreens(player: Player): List<OpenScreen> {
        checkOwned(player)
        return states[player.uniqueId]?.openScreens ?: emptyList()
    }

    /**
     * Closes every screen a player has open.
     *
     * @param player the player
     * @throws IllegalStateException if the calling thread does not own the player
     */
    override fun closeAll(player: Player) {
        checkOwned(player)
        states[player.uniqueId]?.closeAll(notifyClient = true)
    }

    /**
     * Disposes the screen state of a player who quits, running the close handlers of their
     * screens, and forgets the player.
     *
     * @param event the quit event
     */
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val id = event.player.uniqueId
        states[id]?.dispose()
        states.remove(id)
        senders.remove(id)
        ready -= id
        limiter.forget(id)
        rejections.forget(id)
    }

    /**
     * Provides the service instance registered for [ScreenService].
     */
    companion object {
        /**
         * The number of suspicious rejected actions logged per player and minute.
         */
        private const val REJECTIONS_LOGGED_PER_MINUTE = 5

        /**
         * The registered service.
         */
        val INSTANCE: PaperScreenService get() = ScreenService.INSTANCE as PaperScreenService
    }
}
