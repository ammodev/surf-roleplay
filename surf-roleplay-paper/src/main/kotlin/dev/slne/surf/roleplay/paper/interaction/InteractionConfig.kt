package dev.slne.surf.roleplay.paper.interaction

import dev.slne.surf.roleplay.protocol.interaction.InteractionLimits
import org.bukkit.configuration.ConfigurationSection

/**
 * The interaction settings from config.yml.
 *
 * @property defaultRange the reach in blocks for bindings without an override
 * @property maxQueriesPerSecond the largest number of interaction packets a player may send per second
 * @property demoEntries whether the demo radial entry is registered
 */
data class InteractionConfig(
    val defaultRange: Double = 3.0,
    val maxQueriesPerSecond: Int = 10,
    val demoEntries: Boolean = false,
) {
    /**
     * Holds the reader.
     */
    companion object {
        /**
         * Reads the `interaction` section. Invalid values fall back to the default with a warning.
         *
         * @param config the plugin configuration
         * @param warn receives a warning per invalid value
         * @return the settings
         */
        fun from(config: ConfigurationSection, warn: (String) -> Unit): InteractionConfig {
            val defaults = InteractionConfig()
            val range = config.getDouble("interaction.default-range", defaults.defaultRange)
            val rate = config.getInt("interaction.max-queries-per-second", defaults.maxQueriesPerSecond)
            return InteractionConfig(
                defaultRange = if (range > 0.0 && range <= InteractionLimits.MAX_RANGE) range else defaults.defaultRange.also { warn("interaction.default-range $range is outside (0, ${InteractionLimits.MAX_RANGE}], using $it") },
                maxQueriesPerSecond = if (rate > 0) rate else defaults.maxQueriesPerSecond.also { warn("interaction.max-queries-per-second $rate must be positive, using $it") },
                demoEntries = config.getBoolean("interaction.demo-entries", defaults.demoEntries),
            )
        }
    }
}
