package dev.slne.surf.roleplay.paper.tablist

import org.bukkit.configuration.ConfigurationSection
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * The settings of the roleplay tab list, kept in the `tab-list` section of the plugin
 * configuration.
 *
 * @property organisations the display settings per organisation key; organisations not listed use
 *           [Organisation.DEFAULT]
 * @property restartTime the local time of the daily restart in [ZONE], or `null` if the server
 *           does not restart daily
 * @property announcement the announcement shown to every player, or `null` if none is set
 */
data class TabListConfig(
    val organisations: Map<String, Organisation> = emptyMap(),
    val restartTime: LocalTime? = DEFAULT_RESTART_TIME,
    val announcement: String? = null,
) {

    /**
     * How the online count of one organisation is shown.
     *
     * @property exact whether the exact count is sent in addition to the coarse level
     * @property thresholds the two thresholds of the coarse levels
     */
    data class Organisation(val exact: Boolean = false, val thresholds: List<Int> = OnlineLevels.DEFAULT_THRESHOLDS) {
        /**
         * Validates the thresholds.
         */
        init {
            OnlineLevels.level(0, thresholds)
        }

        /**
         * Holds the default settings.
         */
        companion object {
            /**
             * The settings of an organisation that is not configured: coarse with the default
             * thresholds.
             */
            val DEFAULT = Organisation()
        }
    }

    /**
     * Returns the display settings of an organisation.
     *
     * @param key the organisation key
     * @return the configured settings, or [Organisation.DEFAULT] if the organisation is not
     *         configured
     */
    fun organisation(key: String): Organisation = organisations[key] ?: Organisation.DEFAULT

    /**
     * Returns the next daily restart strictly after a point in time. On the day clocks move
     * forward, a restart time inside the gap moves to the first valid time after it; on the day
     * clocks move back, the earlier of the two occurrences is used.
     *
     * @param nowMillis the point in time in epoch milliseconds
     * @return the next restart in epoch milliseconds, or `null` if no daily restart is configured
     */
    fun nextRestartMillis(nowMillis: Long): Long? {
        val time = restartTime ?: return null
        val now = Instant.ofEpochMilli(nowMillis).atZone(ZONE)
        var candidate = now.toLocalDate().atTime(time).atZone(ZONE)
        if (!candidate.isAfter(now)) candidate = now.toLocalDate().plusDays(1).atTime(time).atZone(ZONE)
        return candidate.toInstant().toEpochMilli()
    }

    /**
     * Writes these settings into the `tab-list` section of a configuration, replacing the
     * section.
     *
     * @param config the plugin configuration
     */
    fun writeTo(config: ConfigurationSection) {
        config.set(SECTION, null)
        val section = config.createSection(SECTION)
        section.set(RESTART_TIME, restartTime?.format(TIME_FORMAT) ?: "")
        section.set(ANNOUNCEMENT, announcement ?: "")
        val organisationsSection = section.createSection(ORGANISATIONS)
        organisations.forEach { (key, settings) ->
            val organisation = organisationsSection.createSection(key)
            organisation.set(EXACT, settings.exact)
            organisation.set(THRESHOLDS, settings.thresholds)
        }
    }

    /**
     * Reads tab list settings from the plugin configuration.
     */
    companion object {
        /**
         * The zone the clock and the daily restart are shown and scheduled in.
         */
        val ZONE: ZoneId = ZoneId.of("Europe/Berlin")

        /**
         * The daily restart time used when the configuration sets none.
         */
        val DEFAULT_RESTART_TIME: LocalTime = LocalTime.of(5, 0)

        /**
         * The format of the restart time in the configuration.
         */
        private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        /** The path of the tab list section. */
        private const val SECTION = "tab-list"

        /** The key of the restart time. */
        private const val RESTART_TIME = "restart-time"

        /** The key of the announcement. */
        private const val ANNOUNCEMENT = "announcement"

        /** The key of the organisation settings. */
        private const val ORGANISATIONS = "organisations"

        /** The key of an organisation's exact flag. */
        private const val EXACT = "exact"

        /** The key of an organisation's thresholds. */
        private const val THRESHOLDS = "thresholds"

        /**
         * Reads the settings from the `tab-list` section of a configuration.
         *
         * @param config the plugin configuration
         * @return the settings, with defaults for missing values
         * @throws IllegalArgumentException if the restart time is not `HH:mm` or an organisation's
         *         thresholds are not two strictly increasing, non-negative values
         */
        fun from(config: ConfigurationSection): TabListConfig {
            val section = config.getConfigurationSection(SECTION) ?: return TabListConfig()
            val restartText = section.getString(RESTART_TIME)
            val restartTime = when {
                restartText == null -> DEFAULT_RESTART_TIME
                restartText.isBlank() -> null
                else -> try {
                    LocalTime.parse(restartText.trim(), TIME_FORMAT)
                } catch (exception: DateTimeParseException) {
                    throw IllegalArgumentException("$SECTION.$RESTART_TIME must be HH:mm, was '$restartText'", exception)
                }
            }
            val announcement = section.getString(ANNOUNCEMENT)?.takeIf { it.isNotBlank() }
            val organisationsSection = section.getConfigurationSection(ORGANISATIONS)
            val organisations = organisationsSection?.getKeys(false)?.associateWith { key ->
                val organisation = organisationsSection.getConfigurationSection(key)
                    ?: return@associateWith Organisation.DEFAULT
                val thresholds = if (organisation.contains(THRESHOLDS)) organisation.getIntegerList(THRESHOLDS) else OnlineLevels.DEFAULT_THRESHOLDS
                Organisation(exact = organisation.getBoolean(EXACT, false), thresholds = thresholds)
            }.orEmpty()
            return TabListConfig(organisations, restartTime, announcement)
        }
    }
}
