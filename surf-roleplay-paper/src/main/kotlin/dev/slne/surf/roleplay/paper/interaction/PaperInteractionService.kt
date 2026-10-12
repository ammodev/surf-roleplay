package dev.slne.surf.roleplay.paper.interaction

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.roleplay.api.client.common.toast.Toast
import dev.slne.surf.roleplay.api.client.common.toast.ToastType
import dev.slne.surf.roleplay.api.client.paper.interaction.BlockTarget
import dev.slne.surf.roleplay.api.client.paper.interaction.EntityTarget
import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionBehaviour
import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionBlocker
import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionService
import dev.slne.surf.roleplay.api.client.paper.interaction.InteractionTarget
import dev.slne.surf.roleplay.api.client.paper.interaction.RadialAction
import dev.slne.surf.roleplay.api.client.paper.interaction.RadialCategory
import dev.slne.surf.roleplay.api.client.paper.interaction.RadialEntry
import dev.slne.surf.roleplay.api.client.paper.interaction.TargetKind
import dev.slne.surf.roleplay.paper.interaction.binding.BindingIndex
import dev.slne.surf.roleplay.paper.interaction.binding.BindingParser
import dev.slne.surf.roleplay.paper.interaction.binding.TargetBinding
import dev.slne.surf.roleplay.paper.protocol.PacketHandler
import dev.slne.surf.roleplay.paper.protocol.PaperPacketRegistry
import dev.slne.surf.roleplay.paper.screen.ActionRateLimiter
import dev.slne.surf.roleplay.paper.screen.PaperScreenService
import dev.slne.surf.roleplay.paper.screen.RejectionLog
import dev.slne.surf.roleplay.paper.screen.ScreenMapper
import dev.slne.surf.roleplay.paper.toast.PaperToastService
import dev.slne.surf.roleplay.protocol.Packets
import dev.slne.surf.roleplay.protocol.interaction.BlockTargetId
import dev.slne.surf.roleplay.protocol.interaction.InteractionAvailability
import dev.slne.surf.roleplay.protocol.interaction.InteractionExecute
import dev.slne.surf.roleplay.protocol.interaction.InteractionLimits
import dev.slne.surf.roleplay.protocol.interaction.InteractionOption
import dev.slne.surf.roleplay.protocol.interaction.InteractionRadialOffer
import dev.slne.surf.roleplay.protocol.interaction.InteractionTargetOffer
import dev.slne.surf.roleplay.protocol.interaction.InteractionTargetQuery
import dev.slne.surf.roleplay.protocol.interaction.TargetKinds
import io.papermc.paper.connection.PlayerGameConnection
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.FluidCollisionMode
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.bukkit.util.BoundingBox
import java.nio.file.Files
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/** The logger of the interaction service. */
private val log = logger()

/**
 * The Paper implementation of [InteractionService]: answers target queries and radial requests,
 * runs picked options after re-checking them, keeps every player's current offer, and tells
 * clients whether the target eye and the radial menu may be shown. It must be [started][start]
 * before use.
 */
@AutoService(InteractionService::class)
class PaperInteractionService : InteractionService, Listener {

    /**
     * What an issued offer stands for.
     */
    private sealed interface Pending {
        /**
         * A target offer.
         *
         * @property target the target, a lower block for two-block blocks
         * @property binding the target's binding
         * @property options the offered behaviours by option index
         */
        class Target(val target: InteractionTarget, val binding: TargetBinding, val options: List<BoundBehaviour>) : Pending

        /**
         * A radial offer.
         *
         * @property actions the entry path of every action node by node index
         */
        class Radial(val actions: Map<Int, List<RadialEntry>>) : Pending
    }

    /** The registered behaviours by key. */
    private val behaviours = ConcurrentHashMap<String, InteractionBehaviour>()

    /** The registered radial entries in registration order. */
    private val radial = CopyOnWriteArrayList<Pair<RadialCategory, RadialEntry>>()

    /** The contributions to the blocked predicate. */
    private val blockers = CopyOnWriteArrayList<InteractionBlocker>()

    /** The current offer of every player. */
    private val offers = OfferBook<Pending>()

    /** The availability last sent to each player. */
    private val sentAvailability = ConcurrentHashMap<UUID, Boolean>()

    /** Limits how often a failing condition logs its stack trace. */
    private val conditionLog = LogThrottle(60_000)

    /** Logs at most 20 rejections per player and minute. */
    private val rejections = RejectionLog(REJECTIONS_LOGGED_PER_MINUTE)

    /** The loaded bindings. */
    val bindings: BindingIndex = BindingIndex()

    /** The plugin, set by [start]. */
    private lateinit var plugin: Plugin

    /** The packet registry, set by [start]. */
    private lateinit var registry: PaperPacketRegistry

    /** The settings, set by [start]. */
    lateinit var config: InteractionConfig
        private set

    /** The limit of interaction packets per player, set by [start]. */
    private lateinit var limiter: ActionRateLimiter

    /**
     * Starts the service: registers the packet handlers and listeners and the open-screen hook.
     * Bindings are loaded separately by [reloadBindings] once every plugin has registered its
     * behaviours.
     *
     * @param plugin the plugin
     * @param registry the packet registry
     * @param config the settings
     */
    fun start(plugin: Plugin, registry: PaperPacketRegistry, config: InteractionConfig) {
        this.plugin = plugin
        this.registry = registry
        this.config = config
        this.limiter = ActionRateLimiter(config.maxQueriesPerSecond)
        registry.dispatcher.on(Packets.INTERACTION_TARGET_QUERY, onPlayerThread { player, packet -> query(player, packet) })
        registry.dispatcher.on(Packets.INTERACTION_RADIAL_REQUEST, onPlayerThread { player, _ -> radialRequest(player) })
        registry.dispatcher.on(Packets.INTERACTION_EXECUTE, onPlayerThread { player, packet -> execute(player, packet) })
        PaperScreenService.INSTANCE.onScreensChanged { refreshAvailability(it) }
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    /**
     * Wraps a handler so that it runs on the player's thread, after the rate limit.
     *
     * @param handler the handler
     * @return the packet handler
     */
    private fun <P : dev.slne.surf.roleplay.protocol.Packet> onPlayerThread(handler: (Player, P) -> Unit) = PacketHandler<P> { connection, packet ->
        val player = (connection as? PlayerGameConnection)?.player ?: return@PacketHandler
        player.scheduler.run(plugin, {
            if (!limiter.tryAcquire(player.uniqueId)) {
                reject(player, "rate limit exceeded")
                return@run
            }
            if (isBlocked(player)) {
                reject(player, "interaction is blocked", suspicious = false)
                return@run
            }
            handler(player, packet)
        }, null)
    }

    /**
     * Logs a rejected packet within the player's log budget.
     *
     * @param player the player
     * @param reason why
     * @param suspicious whether a normal client could not have sent it
     */
    private fun reject(player: Player, reason: String, suspicious: Boolean = true) {
        if (!suspicious) {
            log.atFine().log("Rejected interaction of %s: %s", player.name, reason)
            return
        }
        when (rejections.decide(player.uniqueId)) {
            RejectionLog.Decision.LOG -> log.atWarning().log("Rejected interaction of %s: %s", player.name, reason)
            RejectionLog.Decision.SUPPRESS_NOTICE -> log.atWarning().log("Further interaction rejections of %s are suppressed for a minute", player.name)
            RejectionLog.Decision.SILENT -> Unit
        }
    }

    /**
     * Answers a target query. Runs on the player's thread; the check runs on the target's thread.
     *
     * @param player the player
     * @param packet the query
     */
    private fun query(player: Player, packet: InteractionTargetQuery) {
        if (packet.kind.length > MAX_KIND_LENGTH) return reject(player, "target kind too long (${packet.kind.length} characters)")
        if (packet.id.length > InteractionLimits.MAX_TARGET_ID_LENGTH) return reject(player, "target id too long (${packet.id.length} characters)")
        val noOptions = { sendOffer(player, InteractionTargetOffer(0, packet.kind, packet.id)) }
        val retired = { player.scheduler.run(plugin, { noOptions() }, null); Unit }
        when (packet.kind) {
            TargetKinds.BLOCK -> {
                val position = BlockTargetId.parse(packet.id) ?: return reject(player, "malformed block id ${packet.id}")
                val world = player.world
                if (!world.isChunkLoaded(position.x shr 4, position.z shr 4)) return reject(player, "block ${packet.id} is in an unloaded chunk")
                val location = Location(world, position.x.toDouble(), position.y.toDouble(), position.z.toDouble())
                val here = player.location
                val dx = position.x + 0.5 - here.x
                val dy = position.y + 0.5 - here.y
                val dz = position.z + 0.5 - here.z
                val farthest = InteractionLimits.MAX_RANGE + 2
                if (dx * dx + dy * dy + dz * dz > farthest * farthest) return reject(player, "block ${packet.id} is far from the player")
                Bukkit.getRegionScheduler().run(plugin, location) {
                    val (base, binding) = bindings.forBlock(world.getBlockAt(position.x, position.y, position.z)) ?: return@run noOptions()
                    offer(player, BlockTarget(base), binding, packet)
                }
            }
            TargetKinds.ENTITY -> {
                val uuid = EntityTargetId.parse(packet.id) ?: return reject(player, "malformed entity id ${packet.id}")
                val reach = InteractionLimits.MAX_RANGE
                val entity = player.getNearbyEntities(reach, reach, reach).firstOrNull { it.uniqueId == uuid } ?: return reject(player, "no entity ${packet.id} near the player")
                runOnEntity(entity, work = {
                    val binding = bindings.forEntity(entity) ?: return@runOnEntity noOptions()
                    offer(player, EntityTarget(entity), binding, packet)
                }, retired = retired)
            }
            else -> reject(player, "unknown target kind ${packet.kind}")
        }
    }

    /**
     * Checks a bound target and sends its passing options. Runs on the target's thread.
     *
     * @param player the player
     * @param target the target
     * @param binding the target's binding
     * @param packet the query, echoed in the offer
     */
    private fun offer(player: Player, target: InteractionTarget, binding: TargetBinding, packet: InteractionTargetQuery) {
        val empty = InteractionTargetOffer(0, packet.kind, packet.id)
        val failure = reachFailure(player, target, binding)
        if (failure != null) {
            reject(player, "query for ${packet.kind} ${packet.id}: ${failure.reason}", suspicious = failure.suspicious)
            return sendOffer(player, empty)
        }
        val passing = OptionEvaluator.passing(boundBehaviours(binding), player, target) { key, error -> conditionFailed("behaviour", key, error) }
        if (passing.isEmpty()) {
            offers.clear(player.uniqueId)
            return sendOffer(player, empty)
        }
        val options = passing.map { InteractionOption(ScreenMapper.text(it.behaviour.label(it.context(player, target))), it.behaviour.icon) }
        val id = offers.issue(player.uniqueId, Pending.Target(target, binding, passing))
        sendOffer(player, InteractionTargetOffer(id, packet.kind, packet.id, options))
    }

    /**
     * Why a target cannot be reached.
     *
     * @property reason why, for the log
     * @property suspicious whether a normal client could not have asked for this target
     */
    private class ReachFailure(val reason: String, val suspicious: Boolean)

    /**
     * Returns why a target cannot be reached, or `null` if it can. Runs on the target's thread.
     *
     * @param player the player
     * @param target the target
     * @param binding the target's binding
     * @return the failure, or `null`
     */
    private fun reachFailure(player: Player, target: InteractionTarget, binding: TargetBinding): ReachFailure? {
        if (!Bukkit.isOwnedByCurrentRegion(player)) return ReachFailure("player and target are in different regions", suspicious = false)
        val box = boxOf(target) ?: return ReachFailure("target no longer exists", suspicious = false)
        if (worldOf(target) != player.world) return ReachFailure("target is in another world", suspicious = false)
        val eye = player.eyeLocation
        val distance = Reach.distanceToBox(eye.toVector(), box)
        val failed = TargetCheck.check(distance, binding.range ?: config.defaultRange, inSight(player, target, box)) as? TargetCheck.Result.Failed
        return failed?.let { ReachFailure(it.reason, suspicious = TargetCheck.isImplausible(distance)) }
    }

    /**
     * Logs a failing condition: with its stack trace at most once per minute for each behaviour or
     * entry, and without it otherwise.
     *
     * @param what what kind of condition owner failed
     * @param key the key of the behaviour or entry
     * @param error the failure
     */
    private fun conditionFailed(what: String, key: String, error: Throwable) {
        if (conditionLog.shouldLog("$what $key")) {
            log.atWarning().withCause(error).log("Condition of %s %s failed", what, key)
        } else {
            log.atFine().log("Condition of %s %s failed: %s", what, key, error)
        }
    }

    /**
     * Returns the bounding box of a target, a full block for blocks without collision.
     *
     * @param target the target
     * @return the box, or `null` for a removed entity
     */
    private fun boxOf(target: InteractionTarget): BoundingBox? = when (target) {
        is BlockTarget -> target.block.boundingBox.takeIf { it.volume > 0 } ?: BoundingBox.of(target.block)
        is EntityTarget -> target.entity.takeIf { it.isValid }?.boundingBox
        else -> null
    }

    /**
     * Returns the world of a target.
     *
     * @param target the target
     * @return the world
     */
    private fun worldOf(target: InteractionTarget) = when (target) {
        is BlockTarget -> target.block.world
        is EntityTarget -> target.entity.world
        else -> null
    }

    /**
     * Tells whether no block stands between the player's eyes and the centre of the target. For
     * a block target, hitting the target itself or its other half counts as in sight.
     *
     * @param player the player
     * @param target the target
     * @param box the target's bounding box
     * @return whether the target is in sight
     */
    private fun inSight(player: Player, target: InteractionTarget, box: BoundingBox): Boolean {
        val eye = player.eyeLocation
        val direction = box.center.subtract(eye.toVector())
        val distance = direction.length()
        if (distance < 1e-6) return true
        val hit = player.world.rayTraceBlocks(eye, direction.normalize(), distance, FluidCollisionMode.NEVER, true)?.hitBlock ?: return true
        return target is BlockTarget && sameBlock(target.block, hit)
    }

    /**
     * Tells whether a hit block is the target block or the upper half above it.
     *
     * @param target the lower target block
     * @param hit the hit block
     * @return whether they are the same target
     */
    private fun sameBlock(target: Block, hit: Block) = hit == target || (hit.x == target.x && hit.z == target.z && hit.y == target.y + 1 && hit.type == target.type)

    /**
     * Resolves the behaviours of a binding that are still registered.
     *
     * @param binding the binding
     * @return the bound behaviours
     */
    private fun boundBehaviours(binding: TargetBinding) = binding.behaviours.mapNotNull { bound -> behaviours[bound.key]?.let { BoundBehaviour(it, bound.parameters) } }

    /**
     * Sends a target offer. An offer without options also drops the player's current offer.
     *
     * @param player the player
     * @param offer the offer
     */
    private fun sendOffer(player: Player, offer: InteractionTargetOffer) {
        if (offer.options.isEmpty()) offers.clear(player.uniqueId)
        registry.send(player, Packets.INTERACTION_TARGET_OFFER, offer)
    }

    /**
     * Builds and sends the radial menu. Runs on the player's thread.
     *
     * @param player the player
     */
    private fun radialRequest(player: Player) {
        val built = RadialTreeBuilder.build(radial.toList(), player, ScreenMapper::text) { key, error -> conditionFailed("radial entry", key, error) }
        if (built.dropped > 0) log.atWarning().log("Radial menu of %s cut at %d nodes, %d dropped", player.name, InteractionLimits.MAX_RADIAL_NODES, built.dropped)
        if (built.nodes.isEmpty()) {
            offers.clear(player.uniqueId)
            return registry.send(player, Packets.INTERACTION_RADIAL_OFFER, InteractionRadialOffer(0))
        }
        val id = offers.issue(player.uniqueId, Pending.Radial(built.actions))
        registry.send(player, Packets.INTERACTION_RADIAL_OFFER, InteractionRadialOffer(id, built.nodes))
    }

    /**
     * Runs a picked option or radial node after checking it again. Runs on the player's thread.
     *
     * @param player the player
     * @param packet the pick
     */
    private fun execute(player: Player, packet: InteractionExecute) {
        val pending = when (val claim = offers.claim(player.uniqueId, packet.offerId)) {
            is OfferBook.Claim.Rejected -> return reject(player, "execute: ${claim.reason}", suspicious = packet.offerId !in 1..offers.lastIssued())
            is OfferBook.Claim.Accepted -> claim.payload
        }
        when (pending) {
            is Pending.Target -> {
                val bound = pending.options.getOrNull(packet.index) ?: return reject(player, "execute: option ${packet.index} was never offered")
                onOwner(pending.target, retired = { player.scheduler.run(plugin, { denied(player) }, null) }) {
                    val failure = reachFailure(player, pending.target, pending.binding)?.reason
                        ?: "interaction is blocked".takeIf { isBlocked(player) }
                        ?: runCatching { bound.passes(player, pending.target) }.getOrDefault(false).let { if (it) null else "conditions no longer pass" }
                    if (failure != null) {
                        log.atFine().log("Dropped pick %s of %s: %s", bound.behaviour.key, player.name, failure)
                        return@onOwner denied(player)
                    }
                    runCatching { bound.behaviour.action.run(bound.context(player, pending.target)) }
                        .onFailure { log.atWarning().withCause(it).log("Action %s of %s failed", bound.behaviour.key, player.name) }
                }
            }
            is Pending.Radial -> {
                val path = pending.actions[packet.index] ?: return reject(player, "execute: node ${packet.index} is not an action")
                val passes = runCatching { path.all { entry -> entry.conditions.all { it.test(player) } } }.getOrDefault(false)
                if (!passes) return denied(player)
                val action = path.last() as RadialAction
                runCatching { action.handler.run(player) }.onFailure { log.atWarning().withCause(it).log("Radial action %s of %s failed", action.key, player.name) }
            }
        }
    }

    /**
     * Runs work on the thread that owns a target.
     *
     * @param target the target
     * @param retired run instead of [work] if an entity target was removed before the work could run
     * @param work the work
     */
    private fun onOwner(target: InteractionTarget, retired: () -> Unit, work: () -> Unit) {
        when (target) {
            is BlockTarget -> Bukkit.getRegionScheduler().run(plugin, target.block.location) { work() }
            is EntityTarget -> runOnEntity(target.entity, work, retired)
            else -> Unit
        }
    }

    /**
     * Runs work on the thread that owns an entity. If the entity is already removed, [retired]
     * runs instead, on the calling thread.
     *
     * @param entity the entity
     * @param work the work
     * @param retired run instead of [work] if the entity was removed before the work could run
     */
    private fun runOnEntity(entity: Entity, work: () -> Unit, retired: () -> Unit) {
        if (entity.scheduler.run(plugin, { work() }, retired) == null) retired()
    }

    /**
     * Tells the player that the picked option is no longer possible.
     *
     * @param player the player
     */
    private fun denied(player: Player) {
        PaperToastService.INSTANCE.show(player, Toast(Component.text("Das geht gerade nicht."), type = ToastType.WARNING))
    }

    /**
     * Tells whether a player may not interact right now. Runs on the player's thread.
     *
     * @param player the player
     * @return whether interaction is blocked
     */
    private fun isBlocked(player: Player): Boolean =
        player.gameMode == GameMode.SPECTATOR ||
            PaperScreenService.INSTANCE.openScreens(player).isNotEmpty() ||
            blockers.any { runCatching { it.isBlocked(player) }.getOrDefault(true) }

    /**
     * Registers a behaviour that bindings can refer to by its key.
     *
     * @param behaviour the behaviour
     * @throws IllegalArgumentException if a behaviour with the same key is registered
     */
    override fun registerBehaviour(behaviour: InteractionBehaviour) {
        require(behaviours.putIfAbsent(behaviour.key, behaviour) == null) { "Behaviour ${behaviour.key} is already registered" }
    }

    /**
     * Registers an entry of the radial menu.
     *
     * @param category the top-level category the entry belongs to
     * @param entry the entry
     */
    override fun registerRadialEntry(category: RadialCategory, entry: RadialEntry) {
        radial += category to entry
    }

    /**
     * Adds a predicate that blocks interaction for the players it matches.
     *
     * @param blocker the blocker
     */
    override fun addBlocker(blocker: InteractionBlocker) {
        blockers += blocker
    }

    /**
     * Re-evaluates whether a player may interact and tells the player's client if that changed.
     *
     * @param player the player
     */
    override fun refreshAvailability(player: Player) {
        player.scheduler.run(plugin, {
            if (!PaperScreenService.INSTANCE.isReady(player)) return@run
            val available = !isBlocked(player)
            if (sentAvailability.put(player.uniqueId, available) == available) return@run
            if (!available) offers.clear(player.uniqueId)
            registry.send(player, Packets.INTERACTION_AVAILABILITY, InteractionAvailability(available))
        }, null)
    }

    /**
     * Returns the target kinds of a registered behaviour.
     *
     * @param key the behaviour key
     * @return its kinds, or `null` if no behaviour has that key
     */
    fun behaviourKinds(key: String): Set<TargetKind>? = behaviours[key]?.kinds

    /**
     * Reads interactions.yml from the data folder, creating it from the bundled default if
     * missing, and replaces the bindings. Invalid entries are logged and skipped; an unreadable
     * or invalid file keeps the current bindings.
     *
     * @param plugin the plugin that owns the data folder
     * @return a German summary for staff
     */
    fun reloadBindings(plugin: Plugin): String {
        val file = plugin.dataFolder.toPath().resolve("interactions.yml")
        if (Files.notExists(file)) plugin.saveResource("interactions.yml", false)
        val result = try {
            BindingParser.parse(Files.readString(file), ::behaviourKinds)
        } catch (exception: Exception) {
            log.atWarning().withCause(exception).log("Could not load %s, keeping the current bindings", file)
            return "interactions.yml konnte nicht gelesen werden. Die bisherigen Bindungen bleiben aktiv."
        }
        result.errors.forEach { log.atWarning().log("Skipped binding in interactions.yml: %s", it) }
        bindings.bindings = result.bindings
        val loaded = result.bindings.blocks.size + result.bindings.entities.size
        log.atInfo().log("Loaded %d interaction bindings, skipped %d", loaded, result.errors.size)
        return "$loaded Bindungen geladen, ${result.errors.size} übersprungen."
    }

    /**
     * Re-evaluates the availability after a game mode change.
     *
     * @param event the event
     */
    @EventHandler
    fun onGameModeChange(event: PlayerGameModeChangeEvent) = refreshAvailability(event.player)

    /**
     * Drops the current offer when the player changes world.
     *
     * @param event the event
     */
    @EventHandler
    fun onChangedWorld(event: PlayerChangedWorldEvent) = offers.clear(event.player.uniqueId)

    /**
     * Forgets a player who quits.
     *
     * @param event the quit event
     */
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val id = event.player.uniqueId
        offers.clear(id)
        sentAvailability.remove(id)
        limiter.forget(id)
        rejections.forget(id)
    }

    /**
     * Holds the running instance.
     */
    companion object {
        /**
         * The running service.
         */
        val INSTANCE: PaperInteractionService get() = InteractionService.INSTANCE as PaperInteractionService

        /**
         * The number of suspicious rejections logged per player and minute.
         */
        private const val REJECTIONS_LOGGED_PER_MINUTE = 20

        /**
         * The longest target kind accepted in a query.
         */
        private const val MAX_KIND_LENGTH = 32
    }
}
