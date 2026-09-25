package dev.slne.surf.roleplay.microservice.user

import dev.slne.surf.rabbitmq.api.InternalRabbitMQ
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.rabbitmq.api.rpc.RabbitRpcServiceFactory
import dev.slne.surf.rabbitmq.api.rpc.ServerRabbitRpcService
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests that [UserServiceImpl] can be registered with the server-side RPC service of
 * surf-rabbitmq, which resolves generated descriptors for RPC service interfaces only.
 */
@OptIn(InternalRabbitMQ::class, ExperimentalSerializationApi::class)
class UserServiceRegistrationTest {

    private val scope = CoroutineScope(SupervisorJob())

    private val api = mockk<ServerRabbitMQApi>(relaxed = true).also {
        every { it.scope } returns scope
        every { it.cbor } returns Cbor {}
    }

    /** The server-side RPC service created by the factory surf-rabbitmq uses at runtime. */
    private val rpcService =
        RabbitRpcServiceFactory.instance.createRpcService(api) as ServerRabbitRpcService

    /** Cancels the coroutine scope handed to the RPC service. */
    @AfterTest
    fun cancelScope() {
        scope.cancel()
    }

    /**
     * Verifies that the descriptor of the [UserService] interface resolves and that
     * [UserServiceImpl] registers under it.
     */
    @Test
    fun `user service registers under its interface`() {
        val descriptor = rpcService.serviceDescriptorOf(UserService::class)

        rpcService.registerService(UserService::class, UserServiceImpl)

        assertEquals(UserService::class.qualifiedName, descriptor.fqName)
    }

    /**
     * Verifies that no descriptor exists for the implementation class, so registering under it
     * fails.
     */
    @Test
    fun `user service cannot register under its implementation class`() {
        assertFailsWith<IllegalStateException> {
            rpcService.registerService(UserServiceImpl::class, UserServiceImpl)
        }
    }
}
