package dev.slne.surf.roleplay.paper.tablist

import dev.slne.surf.roleplay.api.client.paper.tablist.OrganisationCountProvider
import dev.slne.surf.roleplay.api.client.paper.tablist.SelfInfoProvider
import dev.slne.surf.roleplay.paper.screen.ScreenMapper
import dev.slne.surf.roleplay.protocol.tablist.OnlineLevel
import dev.slne.surf.roleplay.protocol.tablist.OrganisationCount
import dev.slne.surf.roleplay.protocol.tablist.TabListState
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Builds the tab list state of a player from the registered providers, the tab list settings and
 * the clock.
 *
 * A provider that throws does not stop the build: an organisation whose count cannot be determined
 * keeps its row with [OnlineLevel.UNKNOWN] and no exact count, a key, label or icon that cannot
 * be read falls back to the provider's class name, the key, or no icon, and a self value whose
 * provider throws is taken from the next provider or left empty. Each
 * failing provider is reported to [logFailure] at most once per [LOG_INTERVAL_MILLIS]. Every
 * method is thread-safe.
 *
 * @property clock returns the current time in epoch milliseconds
 * @property logFailure receives the name of a failing provider and its exception
 */
class TabListStateBuilder(
    private val clock: () -> Long = System::currentTimeMillis,
    private val logFailure: (provider: String, error: Throwable) -> Unit,
) {
    /**
     * The time each failing provider was last reported, by provider instance.
     */
    private val lastLogged = ConcurrentHashMap<Any, Long>()

    /**
     * Counts the members of every organisation among the online players.
     *
     * @param providers the organisation providers in display order
     * @param count returns the number of online players a provider counts
     * @param config the tab list settings
     * @return one count per organisation, in provider order; the exact count is set only for
     *         organisations configured exact whose count could be determined
     */
    fun organisations(
        providers: List<OrganisationCountProvider>,
        count: (OrganisationCountProvider) -> Int,
        config: TabListConfig,
    ): List<OrganisationCount> = providers.map { provider ->
        val key = guarded(provider, { "organisation ${provider.javaClass.name}" }) { provider.key } ?: provider.javaClass.name
        val name = { "organisation '$key'" }
        val label = guarded(provider, name) { ScreenMapper.text(provider.label) } ?: ScreenMapper.text(Component.text(key))
        val icon = guarded(provider, name) { provider.icon } ?: ""
        val settings = config.organisation(key)
        val count = guarded(provider, name) { count(provider) }
        OrganisationCount(
            key = key,
            label = label,
            icon = icon,
            level = count?.let { OnlineLevels.level(it, settings.thresholds) } ?: OnlineLevel.UNKNOWN,
            exact = if (settings.exact) count else null,
        )
    }

    /**
     * Builds the tab list state of one player. While any of the organisations is shown coarse,
     * the online total is rounded with [approximateTotal] and flagged approximate, so the exact
     * total is never sent.
     *
     * @param organisations the organisation counts, shared by every player
     * @param onlineTotal the number of players online
     * @param weather the weather in the player's world
     * @param sessionStartMillis the start of the player's session in epoch milliseconds
     * @param selfInfo the self-info providers in registration order
     * @param read reads one self value of the player from a provider
     * @param config the tab list settings
     * @return the state
     */
    fun build(
        organisations: List<OrganisationCount>,
        onlineTotal: Int,
        weather: Component,
        sessionStartMillis: Long,
        selfInfo: List<SelfInfoProvider>,
        read: (SelfInfoProvider, SelfValue) -> String?,
        config: TabListConfig,
    ): TabListState {
        val now = clock()
        val approximate = organisations.any { !config.organisation(it.key).exact }
        return TabListState(
            organisations = organisations,
            onlineTotal = if (approximate) approximateTotal(onlineTotal) else onlineTotal,
            onlineTotalApproximate = approximate,
            serverTimeMillis = now,
            zoneId = TabListConfig.ZONE.id,
            restartAtMillis = config.nextRestartMillis(now),
            weather = ScreenMapper.text(weather),
            announcement = config.announcement?.let { ScreenMapper.text(Component.text(it)) },
            characterName = firstValue(selfInfo) { read(it, SelfValue.CHARACTER_NAME) },
            job = firstValue(selfInfo) { read(it, SelfValue.JOB) },
            rank = firstValue(selfInfo) { read(it, SelfValue.RANK) },
            sessionStartMillis = sessionStartMillis,
        )
    }

    /**
     * Returns the first value a self-info provider knows, skipping providers that throw.
     *
     * @param providers the providers in registration order
     * @param value reads the value from one provider
     * @return the first non-null value, or `null` if no provider knows it
     */
    private fun firstValue(providers: List<SelfInfoProvider>, value: (SelfInfoProvider) -> String?): String? =
        providers.firstNotNullOfOrNull { provider -> guarded(provider, { "self info ${provider.javaClass.name}" }) { value(provider) } }

    /**
     * Runs a provider call and turns any throwable except a [VirtualMachineError] into `null`,
     * reporting it at most once per [LOG_INTERVAL_MILLIS] per provider.
     *
     * @param provider the provider, used to rate-limit its reports
     * @param name returns the name the provider is reported under
     * @param block the provider call
     * @return the result of [block], or `null` if it threw
     */
    private fun <T> guarded(provider: Any, name: () -> String, block: () -> T): T? = try {
        block()
    } catch (error: VirtualMachineError) {
        throw error
    } catch (exception: Throwable) {
        val now = clock()
        var report = false
        lastLogged.compute(provider) { _, last ->
            if (last == null || now - last >= LOG_INTERVAL_MILLIS) {
                report = true
                now
            } else {
                last
            }
        }
        if (report) logFailure(name(), exception)
        null
    }

    /**
     * A personal value in the footer of the tab list.
     */
    enum class SelfValue {
        /** The name of the player's character. */
        CHARACTER_NAME,

        /** The player's job. */
        JOB,

        /** The player's rank. */
        RANK,
    }

    /**
     * Holds the log interval, the self value reader and the weather texts.
     */
    companion object {
        /**
         * The shortest time between two reports of the same failing provider.
         */
        const val LOG_INTERVAL_MILLIS: Long = 60_000

        /**
         * Returns the players who are not leaving.
         *
         * @param P the player type
         * @param players the players the server lists as online
         * @param id returns the unique id of a player
         * @param leaving the unique ids of players who are leaving
         * @return the players whose unique id is not in [leaving], in their original order
         */
        fun <P> present(players: Collection<P>, id: (P) -> UUID, leaving: Set<UUID>): List<P> =
            if (leaving.isEmpty()) players.toList() else players.filter { id(it) !in leaving }

        /**
         * Rounds an online total to the nearest multiple of five.
         *
         * @param total the exact total
         * @return the rounded total
         */
        fun approximateTotal(total: Int): Int = (total + 2) / 5 * 5

        /**
         * Returns a reader of a player's self values from a provider.
         *
         * @param player the player
         * @return a function reading the given self value of [player] from a provider
         */
        fun reader(player: Player): (SelfInfoProvider, SelfValue) -> String? = { provider, value ->
            when (value) {
                SelfValue.CHARACTER_NAME -> provider.characterName(player)
                SelfValue.JOB -> provider.job(player)
                SelfValue.RANK -> provider.rank(player)
            }
        }

        /**
         * Returns the German description of a world's weather.
         *
         * @param storm whether it rains or snows
         * @param thundering whether there is a thunderstorm
         * @return "Gewitter" during a thunderstorm, "Regen" while it rains, otherwise "Klar"
         */
        fun weather(storm: Boolean, thundering: Boolean): Component = when {
            storm && thundering -> Component.text("Gewitter")
            storm -> Component.text("Regen")
            else -> Component.text("Klar")
        }
    }
}
