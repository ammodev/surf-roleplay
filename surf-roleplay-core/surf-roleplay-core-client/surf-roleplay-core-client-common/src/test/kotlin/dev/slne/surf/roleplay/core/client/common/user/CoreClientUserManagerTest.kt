package dev.slne.surf.roleplay.core.client.common.user

import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayUserDto
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Tests for loading, holding, releasing and evicting users in [CoreClientUserManager].
 */
class CoreClientUserManagerTest {

    private val service = mockk<UserService>()
    private val manager = CoreClientUserManager { service }
    private val uuid = UUID.randomUUID()

    init {
        coEvery { service.findOrCreateByUuid(uuid) } answers { RoleplayUserDto(uuid, emptyList()) }
        coEvery { service.findByUuid(uuid) } answers { RoleplayUserDto(uuid, emptyList()) }
    }

    /**
     * Verifies that loadAndCache caches the loaded user.
     */
    @Test
    fun `loadAndCache caches the loaded user`() = runBlocking {
        val loaded = manager.loadAndCache(uuid)

        assertEquals(uuid, loaded.uuid)
        assertSame(loaded, manager.findByUuid(uuid))
        assertSame(loaded, manager.findOrCreateByUuid(uuid))
        coVerify(exactly = 1) { service.findOrCreateByUuid(uuid) }
        coVerify(exactly = 0) { service.findByUuid(any()) }
    }

    /**
     * Verifies that loadAndCache reuses the cached instance without loading again.
     */
    @Test
    fun `loadAndCache reuses the cached instance without loading again`() = runBlocking {
        val first = manager.loadAndCache(uuid)

        assertSame(first, manager.loadAndCache(uuid))
        coVerify(exactly = 1) { service.findOrCreateByUuid(uuid) }
    }

    /**
     * Verifies that loadAndCache racing a slower load shares one instance and holds it twice.
     */
    @Test
    fun `loadAndCache racing a slower load shares one instance and holds it twice`() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        var calls = 0
        coEvery { service.findOrCreateByUuid(uuid) } coAnswers {
            if (++calls == 1) gate.await()
            RoleplayUserDto(uuid, emptyList())
        }

        val slow = async(start = CoroutineStart.UNDISPATCHED) { manager.loadAndCache(uuid) }
        val fast = manager.loadAndCache(uuid)
        gate.complete(Unit)

        assertSame(fast, slow.await())
        coVerify(exactly = 2) { service.findOrCreateByUuid(uuid) }

        manager.release(uuid)
        assertSame(fast, manager.findByUuid(uuid))
        coVerify(exactly = 0) { service.findByUuid(any()) }

        manager.release(uuid)
        assertNotSame(fast, manager.findByUuid(uuid))
        coVerify(exactly = 1) { service.findByUuid(uuid) }
    }

    /**
     * Verifies that release after two acquires keeps the user cached.
     */
    @Test
    fun `release after two acquires keeps the user cached`() = runBlocking {
        val cached = manager.loadAndCache(uuid)
        manager.loadAndCache(uuid)

        manager.release(uuid)

        assertSame(cached, manager.findByUuid(uuid))
        coVerify(exactly = 0) { service.findByUuid(any()) }
    }

    /**
     * Verifies that second release after two acquires evicts the user.
     */
    @Test
    fun `second release after two acquires evicts the user`() = runBlocking {
        val cached = manager.loadAndCache(uuid)
        manager.loadAndCache(uuid)

        manager.release(uuid)
        manager.release(uuid)

        assertNotSame(cached, manager.findByUuid(uuid))
        coVerify(exactly = 1) { service.findByUuid(uuid) }
    }

    /**
     * Verifies that release of a user that is not cached does nothing.
     */
    @Test
    fun `release of a user that is not cached does nothing`() = runBlocking {
        manager.release(uuid)
        val cached = manager.loadAndCache(uuid)

        assertSame(cached, manager.findByUuid(uuid))
    }

    /**
     * Verifies that failed load acquires no hold.
     */
    @Test
    fun `failed load acquires no hold`() = runBlocking {
        coEvery { service.findOrCreateByUuid(uuid) } throws IllegalStateException("unavailable")

        assertFailsWith<IllegalStateException> { manager.loadAndCache(uuid) }

        assertNotNull(manager.findByUuid(uuid))
        coVerify(exactly = 1) { service.findByUuid(uuid) }
    }

    /**
     * Verifies that findByUuid of a user that is not cached does not cache it.
     */
    @Test
    fun `findByUuid of a user that is not cached does not cache it`() = runBlocking {
        val first = assertNotNull(manager.findByUuid(uuid))
        val second = assertNotNull(manager.findByUuid(uuid))

        assertNotSame(first, second)
        coVerify(exactly = 2) { service.findByUuid(uuid) }
    }

    /**
     * Verifies that findByUuid returns null for an unknown user.
     */
    @Test
    fun `findByUuid returns null for an unknown user`() = runBlocking {
        val unknown = UUID.randomUUID()
        coEvery { service.findByUuid(unknown) } returns null

        assertNull(manager.findByUuid(unknown))
    }

    /**
     * Verifies that findOrCreateByUuid of a user that is not cached does not cache it.
     */
    @Test
    fun `findOrCreateByUuid of a user that is not cached does not cache it`() = runBlocking {
        val first = manager.findOrCreateByUuid(uuid)
        val second = manager.findOrCreateByUuid(uuid)

        assertNotSame(first, second)
        coVerify(exactly = 2) { service.findOrCreateByUuid(uuid) }
    }

    /**
     * Verifies that evict removes the cached user regardless of remaining holds.
     */
    @Test
    fun `evict removes the cached user regardless of remaining holds`() = runBlocking {
        val cached = manager.loadAndCache(uuid)
        manager.loadAndCache(uuid)

        manager.evict(uuid)

        assertNotSame(cached, manager.findByUuid(uuid))
        coVerify(exactly = 1) { service.findByUuid(uuid) }
    }

    /**
     * Verifies that cached returns the held user without loading or acquiring a hold, and nothing
     * for a user that is not held.
     */
    @Test
    fun `cached returns only held users without acquiring a hold`() = runBlocking {
        assertNull(manager.cached(uuid))

        val loaded = manager.loadAndCache(uuid)
        assertSame(loaded, manager.cached(uuid))

        manager.release(uuid)
        assertNull(manager.cached(uuid))
        coVerify(exactly = 0) { service.findByUuid(any()) }
        coVerify(exactly = 1) { service.findOrCreateByUuid(uuid) }
    }
}
