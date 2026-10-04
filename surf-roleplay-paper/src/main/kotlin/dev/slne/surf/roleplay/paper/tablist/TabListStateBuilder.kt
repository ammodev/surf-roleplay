package dev.slne.surf.roleplay.paper.tablist

import dev.slne.surf.roleplay.api.client.paper.tablist.OrganisationCountProvider
import dev.slne.surf.roleplay.api.client.paper.tablist.SelfInfoProvider
import dev.slne.surf.roleplay.paper.screen.ScreenMapper
import dev.slne.surf.roleplay.protocol.tablist.OrganisationCount
import dev.slne.surf.roleplay.protocol.tablist.TabListState
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

/**
 * Builds the tab list state of a player from the registered providers, the tab list settings and
 * the clock.
 *
 * A provider that throws does not stop the build: an organisation whose provider throws is left
 * out, and a self value whose provider throws is taken from the next provider or left empty. Each
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
     * @return one count per organisation whose provider did not throw, in provider order; the
     *         exact count is set only for organisations configured exact
     */
    fun organisations(
        providers: List<OrganisationCountProvider>,
        count: (OrganisationCountProvider) -> Int,
        config: TabListConfig,
    ): List<OrganisationCount> = providers.mapNotNull { provider ->
        guarded(provider, { "organisation '${runCatching { provider.key }.getOrElse { provider.javaClass.name }}'" }) {
            val settings = config.organisation(provider.key)
            val count = count(provider)
            OrganisationCount(
                key = provider.key,
                label = ScreenMapper.text(provider.label),
                icon = provider.icon,
                level = OnlineLevels.level(count, settings.thresholds),
                exact = if (settings.exact) count else null,
            )
        }
    }

    /**
     * Builds the tab list state of one player.
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
        return TabListState(
            organisations = organisations,
            onlineTotal = onlineTotal,
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
     * Runs a provider call and turns an exception into `null`, reporting it at most once per
     * [LOG_INTERVAL_MILLIS] per provider.
     *
     * @param provider the provider, used to rate-limit its reports
     * @param name returns the name the provider is reported under
     * @param block the provider call
     * @return the result of [block], or `null` if it threw
     */
    private fun <T> guarded(provider: Any, name: () -> String, block: () -> T): T? = try {
        block()
    } catch (exception: Exception) {
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
