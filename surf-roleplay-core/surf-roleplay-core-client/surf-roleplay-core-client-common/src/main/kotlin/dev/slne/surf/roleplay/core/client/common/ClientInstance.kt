package dev.slne.surf.roleplay.core.client.common

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.rabbitmq.api.ClientRabbitMQApi
import java.nio.file.Path

private val instance = requiredService<ClientInstance>()

abstract class ClientInstance {
    abstract val dataPath: Path

    val rabbitApi: ClientRabbitMQApi = ClientRabbitMQApi.create("surf-roleplay", dataPath)

    suspend fun onLoad() {
        
    }

    suspend fun onEnable() {
        rabbitApi.freezeAndConnect()
    }

    suspend fun onDisable() {
        rabbitApi.disconnect()
    }

    companion object {
        val INSTANCE get() = instance
    }
}