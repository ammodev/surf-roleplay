package dev.slne.surf.roleplay.paper.handshake

import org.bukkit.configuration.ConfigurationSection
import java.time.Duration

/**
 * The settings of the mod handshake.
 *
 * @property timeout how long the server waits for the client mod's hello
 */
data class HandshakeConfig(val timeout: Duration) {

    /**
     * Reads handshake settings from the plugin configuration.
     */
    companion object {
        /**
         * The timeout used when the configuration sets none.
         */
        private const val DEFAULT_TIMEOUT_SECONDS = 10L

        /**
         * Reads the handshake settings from the `handshake` section of a configuration.
         *
         * @param config the plugin configuration
         * @return the handshake settings, with defaults for missing values
         * @throws IllegalArgumentException if the timeout is not positive
         */
        fun from(config: ConfigurationSection): HandshakeConfig {
            val seconds = config.getLong("handshake.timeout-seconds", DEFAULT_TIMEOUT_SECONDS)
            require(seconds > 0) { "handshake.timeout-seconds must be positive, was $seconds" }
            return HandshakeConfig(Duration.ofSeconds(seconds))
        }
    }
}
