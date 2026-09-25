package dev.slne.surf.roleplay.microservice

import com.google.auto.service.AutoService
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.microservice.api.microservice.Microservice
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.roleplay.microservice.user.UserServiceImpl
import dev.slne.surf.roleplay.microservice.user.db.tables.RoleplayUsersTable
import kotlin.io.path.Path

@AutoService(Microservice::class)
class RoleplayMicroservice : Microservice() {
    override val dataPath = Path("config")

    val databaseApi = DatabaseApi.create(dataPath)
    val rabbitApi = ServerRabbitMQApi.create("surf-roleplay", dataPath)

    override suspend fun onBootstrap(args: List<String>) {
        suspendTransaction {
            SchemaUtils.create(RoleplayUsersTable)
        }

        rabbitApi.registerRpcService(UserServiceImpl)
        rabbitApi.freezeAndConnect()
    }

    override suspend fun onDisable() {
        rabbitApi.disconnect()
        databaseApi.shutdown()
    }
}