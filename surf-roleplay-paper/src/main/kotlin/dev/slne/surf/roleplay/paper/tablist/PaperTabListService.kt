package dev.slne.surf.roleplay.paper.tablist

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.roleplay.api.client.paper.tablist.OrganisationCountProvider
import dev.slne.surf.roleplay.api.client.paper.tablist.SelfInfoProvider
import dev.slne.surf.roleplay.api.client.paper.tablist.TabListService
import dev.slne.surf.roleplay.paper.protocol.PaperPacketRegistry
import dev.slne.surf.roleplay.paper.screen.PaperScreenService
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.tablist.OrganisationCount
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.weather.ThunderChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.plugin.Plugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

private val log = logger()

/**
 * The Paper implementation of [TabListService].
 *
 * It sends every ready player the full tab list state right after the player's client is
 * welcomed, and afterwards whenever the state changed, at most once per
 * [TabListCoalescer.DEFAULT_WINDOW_MILLIS] per player. Changes are collected by a
 * [TabListCoalescer]; a repeating global task releases the due players every 250 ms and builds
 * and sends each player's state on the player's own scheduler. Organisation counts are computed
 * once per release and shared by every player released with it. It must be [started][start] by
 * the plugin before use.
 */
@AutoService(TabListService::class)
class PaperTabListService : TabListService, Listener {

    /**
     * The registered organisation providers in registration order.
     */
    private val organisations = CopyOnWriteArrayList<OrganisationCountProvider>()

    /**
     * The registered self-info providers in registration order.
     */
    private val selfInfo = CopyOnWriteArrayList<SelfInfoProvider>()

    /**
     * The pending changes of every player.
     */
    private val coalescer = TabListCoalescer(toleranceMillis = FLUSH_PERIOD_TICKS * MILLIS_PER_TICK / 2)

    /**
     * The time each online player joined, in epoch milliseconds.
     */
    private val sessionStarts = ConcurrentHashMap<UUID, Long>()

    /**
     * Builds the players' states and reports failing providers as warnings.
     */
    private val builder = TabListStateBuilder { provider, error ->
        log.atWarning().withCause(error).log("Tab list provider %s failed", provider)
    }

    /**
     * The plugin that owns the tab list, set by [start].
     */
    private lateinit var plugin: Plugin

    /**
     * The packet registry states are sent through, set by [start].
     */
    private lateinit var registry: PaperPacketRegistry

    /**
     * The current tab list settings.
     */
    @Volatile
    var config: TabListConfig = TabListConfig()
        private set

    /**
     * Starts the service: registers its listeners and the repeating task that sends due states.
     *
     * @param plugin the plugin that owns the tab list
     * @param registry the packet registry
     * @param config the tab list settings
     */
    fun start(plugin: Plugin, registry: PaperPacketRegistry, config: TabListConfig) {
        this.plugin = plugin
        this.registry = registry
        this.config = config
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.server.globalRegionScheduler.runAtFixedRate(plugin, { flush() }, FLUSH_PERIOD_TICKS, FLUSH_PERIOD_TICKS)
    }

    /**
     * Replaces the tab list settings and marks every player's state as changed.
     *
     * @param config the new settings
     */
    fun updateConfig(config: TabListConfig) {
        this.config = config
        changed()
    }

    /**
     * Registers an organisation and marks every player's state as changed.
     *
     * @param provider the provider of the organisation
     */
    override fun registerOrganisation(provider: OrganisationCountProvider) {
        organisations += provider
        changed()
    }

    /**
     * Registers a self-info provider and marks every player's state as changed.
     *
     * @param provider the provider
     */
    override fun registerSelfInfo(provider: SelfInfoProvider) {
        selfInfo += provider
        changed()
    }

    /**
     * Marks the state of every online player as changed.
     */
    override fun changed() {
        coalescer.markDirty(onlinePlayerIds())
    }

    /**
     * Marks the state of one player as changed.
     *
     * @param player the player
     */
    override fun changed(player: Player) {
        changed(player.uniqueId)
    }

    /**
     * Marks the state of one player as changed.
     *
     * @param player the player's unique id
     */
    fun changed(player: UUID) {
        coalescer.markDirty(player)
    }

    /**
     * Sends a player the full state at once on the player's scheduler, if the player's client is
     * ready, and starts the player's next push window.
     *
     * @param player the player
     */
    fun pushNow(player: Player) {
        player.scheduler.run(plugin, {
            coalescer.pushed(player.uniqueId)
            push(player, countOrganisations(), plugin.server.onlinePlayers.size)
        }, null)
    }

    /**
     * Releases the due players and sends each of them the current state on the player's own
     * scheduler.
     */
    private fun flush() {
        val due = coalescer.due()
        if (due.isEmpty()) return
        val counts = countOrganisations()
        val onlineTotal = plugin.server.onlinePlayers.size
        due.forEach { id ->
            val player = plugin.server.getPlayer(id) ?: return@forEach
            player.scheduler.run(plugin, { push(player, counts, onlineTotal) }, null)
        }
    }

    /**
     * Builds and sends a player's state if the player's client is ready. Runs on the player's
     * scheduler.
     *
     * @param player the player
     * @param counts the organisation counts
     * @param onlineTotal the number of players online
     */
    private fun push(player: Player, counts: List<OrganisationCount>, onlineTotal: Int) {
        if (!PaperScreenService.INSTANCE.isReady(player)) return
        val world = player.world
        val state = builder.build(
            organisations = counts,
            onlineTotal = onlineTotal,
            weather = TabListStateBuilder.weather(world.hasStorm(), world.isThundering),
            sessionStartMillis = sessionStarts[player.uniqueId] ?: System.currentTimeMillis(),
            selfInfo = selfInfo,
            read = TabListStateBuilder.reader(player),
            config = config,
        )
        registry.send(player, Packets.TAB_LIST_STATE, state)
    }

    /**
     * Counts the members of every registered organisation among the online players.
     *
     * @return the organisation counts
     */
    private fun countOrganisations(): List<OrganisationCount> {
        val players = plugin.server.onlinePlayers.toList()
        return builder.organisations(organisations, { provider -> players.count(provider::counts) }, config)
    }

    /**
     * Returns the unique ids of the online players.
     *
     * @return the unique ids, empty before the service is started
     */
    private fun onlinePlayerIds(): List<UUID> =
        if (::plugin.isInitialized) plugin.server.onlinePlayers.map { it.uniqueId } else emptyList()

    /**
     * Marks the players of a world as changed.
     *
     * @param world the world
     */
    private fun worldChanged(world: World) {
        coalescer.markDirty(plugin.server.onlinePlayers.filter { it.world.uid == world.uid }.map { it.uniqueId })
    }

    /**
     * Records the start of a player's session and marks every player's state as changed, since
     * the online total changed.
     *
     * @param event the join event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinEvent) {
        sessionStarts[event.player.uniqueId] = System.currentTimeMillis()
        changed()
    }

    /**
     * Forgets a player who left and marks the other players' states as changed, since the online
     * total and organisation counts changed.
     *
     * @param event the quit event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        val id = event.player.uniqueId
        sessionStarts -= id
        coalescer.remove(id)
        coalescer.markDirty(onlinePlayerIds().filter { it != id })
    }

    /**
     * Marks the players of a world whose weather changes as changed.
     *
     * @param event the weather change event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onWeatherChange(event: WeatherChangeEvent) {
        worldChanged(event.world)
    }

    /**
     * Marks the players of a world whose thunder state changes as changed.
     *
     * @param event the thunder change event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onThunderChange(event: ThunderChangeEvent) {
        worldChanged(event.world)
    }

    /**
     * Holds the running instance and the flush period.
     */
    companion object {
        /**
         * The running service.
         */
        val INSTANCE: PaperTabListService get() = TabListService.INSTANCE as PaperTabListService

        /**
         * The period of the task that sends due states, in ticks.
         */
        private const val FLUSH_PERIOD_TICKS: Long = 5

        /**
         * The nominal length of a server tick in milliseconds.
         */
        private const val MILLIS_PER_TICK: Long = 50
    }
}
