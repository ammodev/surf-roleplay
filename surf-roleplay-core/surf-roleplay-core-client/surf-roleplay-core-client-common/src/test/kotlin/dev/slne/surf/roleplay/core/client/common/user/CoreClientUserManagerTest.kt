package dev.slne.surf.roleplay.core.client.common.user

import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayUserDto
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Tests for loading, caching and evicting users in [CoreClientUserManager].
 */
class CoreClientUserManagerTest {

    private val service = mockk<UserService>()
    private val manager = CoreClientUserManager { service }
    private val uuid = UUID.randomUUID()

    init {
        coEvery { service.findOrCreateByUuid(uuid) } answers { RoleplayUserDto(uuid, emptyList()) }
        coEvery { service.findByUuid(uuid) } answers { RoleplayUserDto(uuid, emptyList()) }
    }

    @Test
    fun `loadAndCache caches the loaded user`() = runBlocking {
        val loaded = manager.loadAndCache(uuid)

        assertEquals(uuid, loaded.uuid)
        assertSame(loaded, manager.findByUuid(uuid))
        assertSame(loaded, manager.findOrCreateByUuid(uuid))
        coVerify(exactly = 1) { service.findOrCreateByUuid(uuid) }
        coVerify(exactly = 0) { service.findByUuid(any()) }
    }

    @Test
    fun `loadAndCache keeps the instance already cached`() = runBlocking {
        val first = manager.loadAndCache(uuid)

        assertSame(first, manager.loadAndCache(uuid))
    }

    @Test
    fun `findByUuid of a user that is not cached does not cache it`() = runBlocking {
        val first = assertNotNull(manager.findByUuid(uuid))
        val second = assertNotNull(manager.findByUuid(uuid))

        assertNotSame(first, second)
        coVerify(exactly = 2) { service.findByUuid(uuid) }
    }

    @Test
    fun `findByUuid returns null for an unknown user`() = runBlocking {
        val unknown = UUID.randomUUID()
        coEvery { service.findByUuid(unknown) } returns null

        assertNull(manager.findByUuid(unknown))
    }

    @Test
    fun `findOrCreateByUuid of a user that is not cached does not cache it`() = runBlocking {
        val first = manager.findOrCreateByUuid(uuid)
        val second = manager.findOrCreateByUuid(uuid)

        assertNotSame(first, second)
        coVerify(exactly = 2) { service.findOrCreateByUuid(uuid) }
    }

    @Test
    fun `evict removes the cached user`() = runBlocking {
        val cached = manager.loadAndCache(uuid)

        manager.evict(uuid)

        assertNotSame(cached, manager.findByUuid(uuid))
        coVerify(exactly = 1) { service.findByUuid(uuid) }
    }
}
