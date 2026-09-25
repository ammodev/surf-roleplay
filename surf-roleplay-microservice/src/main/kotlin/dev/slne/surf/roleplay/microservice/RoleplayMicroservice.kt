package dev.slne.surf.roleplay.microservice

import com.google.auto.service.AutoService
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.microservice.api.microservice.Microservice
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import dev.slne.surf.roleplay.microservice.user.UserServiceImpl
import dev.slne.surf.roleplay.microservice.user.db.tables.RoleplayIdentitiesTable
import dev.slne.surf.roleplay.microservice.user.db.tables.RoleplayIdentityLicensesTable
import dev.slne.surf.roleplay.microservice.user.db.tables.RoleplayIdentityQualificationsTable
import dev.slne.surf.roleplay.microservice.user.db.tables.RoleplayUsersTable
import kotlin.io.path.Path

/**
 * The roleplay microservice.
 *
 * On bootstrap it creates the missing roleplay tables and serves the user service over RabbitMQ.
 */
@AutoService(Microservice::class)
class RoleplayMicroservice : Microservice() {
    /** The directory the database and RabbitMQ configuration files are read from. */
    override val dataPath = Path("config")

    /** The database connection used by every transaction of this microservice. */
    val databaseApi = DatabaseApi.create(dataPath)

    /** The RabbitMQ connection the user service is served over. */
    val rabbitApi = ServerRabbitMQApi.create("surf-roleplay", dataPath)

    /**
     * Creates the missing roleplay tables, registers the user service and connects to RabbitMQ.
     *
     * @param args the command line arguments of the microservice
     */
    override suspend fun onBootstrap(args: List<String>) {
        suspendTransaction {
            SchemaUtils.create(
                RoleplayUsersTable,
                RoleplayIdentitiesTable,
                RoleplayIdentityQualificationsTable,
                RoleplayIdentityLicensesTable
            )
        }

        rabbitApi.registerRpcService<UserService>(UserServiceImpl)
        rabbitApi.freezeAndConnect()
    }

    /**
     * Disconnects from RabbitMQ and closes the database connection.
     */
    override suspend fun onDisable() {
        rabbitApi.disconnect()
        databaseApi.shutdown()
    }
}
