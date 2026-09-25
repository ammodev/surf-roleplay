package dev.slne.surf.roleplay.core.client.common

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.rabbitmq.api.ClientRabbitMQApi
import java.nio.file.Path

private val instance = requiredService<ClientInstance>()

/**
 * The per-platform entry point of a roleplay client, holding its RabbitMQ connection and
 * lifecycle hooks.
 */
abstract class ClientInstance {
    /**
     * The directory this client stores its data in.
     */
    abstract val dataPath: Path

    /**
     * The RabbitMQ API this client uses to reach the roleplay microservice.
     */
    val rabbitApi: ClientRabbitMQApi = ClientRabbitMQApi.create("surf-roleplay", dataPath)

    /**
     * Runs during the platform's load phase.
     */
    suspend fun onLoad() {

    }

    /**
     * Freezes and connects [rabbitApi] during the platform's enable phase.
     */
    suspend fun onEnable() {
        rabbitApi.freezeAndConnect()
    }

    /**
     * Disconnects [rabbitApi] during the platform's disable phase.
     */
    suspend fun onDisable() {
        rabbitApi.disconnect()
    }

    /**
     * Provides access to the single registered [ClientInstance] service.
     */
    companion object {
        /**
         * The single registered [ClientInstance] service instance.
         */
        val INSTANCE get() = instance
    }
}
