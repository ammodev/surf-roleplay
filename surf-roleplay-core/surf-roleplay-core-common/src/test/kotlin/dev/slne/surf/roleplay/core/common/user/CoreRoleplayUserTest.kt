package dev.slne.surf.roleplay.core.common.user

import dev.slne.surf.roleplay.api.common.identity.exceptions.UnknownIdentityException
import dev.slne.surf.roleplay.core.common.identity.CoreCivilianIdentity
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Tests for the active-identity handling of [CoreRoleplayUser].
 */
class CoreRoleplayUserTest {

    private val service = mockk<UserService>(relaxed = true)
    private val userUuid = UUID.randomUUID()

    /**
     * Builds a civilian identity with [uuid] owned by the player with [owner].
     */
    private fun civilian(uuid: UUID = UUID.randomUUID(), owner: UUID = userUuid) =
        CoreCivilianIdentity(uuid, owner, UUID.randomUUID(), emptyList(), service)

    @Test
    fun `setActiveIdentity activates the owned instance`() = runBlocking {
        val owned = civilian()
        val user = CoreRoleplayUser(userUuid, listOf(owned), service)
        val equalUuidCopy = civilian(uuid = owned.uuid)

        user.setActiveIdentity(equalUuidCopy)

        assertSame(owned, user.activeIdentity)
    }

    @Test
    fun `setActiveIdentity rejects a foreign identity`() = runBlocking {
        val user = CoreRoleplayUser(userUuid, listOf(civilian()), service)
        val foreign = civilian(owner = UUID.randomUUID())

        val exception = assertFailsWith<UnknownIdentityException> {
            user.setActiveIdentity(foreign)
        }

        assertEquals(userUuid, exception.userUuid)
        assertEquals(foreign.uuid, exception.identityUuid)
        assertNull(user.activeIdentity)
    }

    @Test
    fun `clearActiveIdentity leaves no identity active`() = runBlocking {
        val owned = civilian()
        val user = CoreRoleplayUser(userUuid, listOf(owned), service)
        user.setActiveIdentity(owned)

        user.clearActiveIdentity()

        assertNull(user.activeIdentity)
    }
}
