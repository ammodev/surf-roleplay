package dev.slne.surf.roleplay.paper.toast

import com.google.auto.service.AutoService
import dev.slne.surf.roleplay.api.client.common.toast.Toast
import dev.slne.surf.roleplay.api.client.common.toast.ToastType
import dev.slne.surf.roleplay.api.client.paper.toast.ToastService
import dev.slne.surf.roleplay.paper.protocol.PacketHandler
import dev.slne.surf.roleplay.paper.protocol.PaperPacketRegistry
import io.papermc.paper.connection.PlayerGameConnection
import dev.slne.surf.roleplay.paper.screen.ActionRateLimiter
import dev.slne.surf.roleplay.paper.screen.PaperScreenService
import dev.slne.surf.roleplay.paper.screen.ScreenMapper
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.toast.ToastDismiss
import dev.slne.surf.roleplay.protocol.toast.ToastShow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import dev.slne.surf.roleplay.protocol.toast.ToastType as PacketToastType

/**
 * The Paper implementation of [ToastService].
 *
 * It keeps a [ToastRegistry] per player and runs the handlers of valid toast button clicks on the
 * player's region thread. It must be [started][start] by the plugin before use.
 */
@AutoService(ToastService::class)
class PaperToastService : ToastService, Listener {

    /**
     * The active toasts of every player.
     */
    private val registries = ConcurrentHashMap<UUID, ToastRegistry>()

    /**
     * The plugin that owns the toasts, set by [start].
     */
    private lateinit var plugin: Plugin

    /**
     * The packet registry toasts are sent through, set by [start].
     */
    private lateinit var registry: PaperPacketRegistry

    /**
     * The rate limiter of toast button clicks, set by [start].
     */
    private lateinit var limiter: ActionRateLimiter

    /**
     * Starts the service: registers the handler of toast button clicks and the quit listener.
     *
     * @param plugin the plugin that owns the toasts
     * @param registry the packet registry
     * @param maxActionsPerSecond the largest number of toast clicks a player may send per second
     */
    fun start(plugin: Plugin, registry: PaperPacketRegistry, maxActionsPerSecond: Int) {
        this.plugin = plugin
        this.registry = registry
        limiter = ActionRateLimiter(maxActionsPerSecond)
        registry.dispatcher.on(Packets.TOAST_ACTION, PacketHandler { connection, packet ->
            val player = (connection as? PlayerGameConnection)?.player ?: return@PacketHandler
            player.scheduler.run(plugin, {
                if (!limiter.tryAcquire(player.uniqueId)) return@run
                val outcome = registries[player.uniqueId]?.handle(packet.id, packet.button, System.currentTimeMillis()) ?: return@run
                if (outcome is ToastRegistry.Outcome.Accepted) outcome.run()
            }, null)
        })
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    /**
     * Shows a toast to a player whose client is ready, and records it for its button clicks. A
     * toast for a player whose client is not ready is dropped and accepts no clicks.
     *
     * @param player the player
     * @param toast the toast
     * @return the id of the toast
     */
    override fun show(player: Player, toast: Toast): String {
        val ready = PaperScreenService.INSTANCE.isReady(player)
        val id = registries.computeIfAbsent(player.uniqueId) { ToastRegistry(it) }.record(toast, System.currentTimeMillis(), shown = ready)
        if (ready) registry.send(player, Packets.TOAST_SHOW, packet(id, toast))
        return id
    }

    /**
     * Removes a shown toast and forgets its buttons.
     *
     * @param player the player
     * @param id the id of the toast
     */
    override fun dismiss(player: Player, id: String) {
        registries[player.uniqueId]?.dismiss(id)
        if (PaperScreenService.INSTANCE.isReady(player)) registry.send(player, Packets.TOAST_DISMISS, ToastDismiss(id))
    }

    /**
     * Forgets the toasts of a player who left.
     *
     * @param event the quit event
     */
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        registries.remove(event.player.uniqueId)
    }

    /**
     * Holds the packet mapping and the running instance.
     */
    companion object {
        /**
         * The running service.
         */
        val INSTANCE: PaperToastService get() = ToastService.INSTANCE as PaperToastService

        /**
         * Maps a toast to the packet that shows it.
         *
         * @param id the id of the toast
         * @param toast the toast
         * @return the packet
         */
        fun packet(id: String, toast: Toast): ToastShow = ToastShow(
            id = id,
            type = PacketToastType.valueOf(toast.type.name),
            title = ScreenMapper.text(toast.title),
            description = toast.description?.let(ScreenMapper::text),
            actionLabel = toast.action?.label?.let(ScreenMapper::text),
            cancelLabel = toast.cancel?.label?.let(ScreenMapper::text),
            durationMillis = toast.durationMillis,
            dismissible = toast.dismissible,
            closeButton = toast.closeButton,
        )

        /**
         * Checks that the API and protocol toast types match by name.
         */
        init {
            check(ToastType.entries.map { it.name } == PacketToastType.entries.map { it.name }) { "Toast types differ between API and protocol" }
        }
    }
}
