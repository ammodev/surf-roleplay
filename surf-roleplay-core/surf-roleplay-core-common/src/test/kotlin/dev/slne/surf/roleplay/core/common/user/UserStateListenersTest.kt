package dev.slne.surf.roleplay.core.common.user

import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceRank
import dev.slne.surf.roleplay.core.common.identity.account.IdentityAccountResolver
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayIdentityDto
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayUserDto
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for notifying [UserStateListener]s of changes to a user's identity state.
 */
class UserStateListenersTest {

    private val userUuid = UUID.randomUUID()

    /** The users this test was notified about, restricted to [userUuid]. */
    private val notified = mutableListOf<UUID>()

    /** Records notifications about [userUuid]. */
    private val listener = UserStateListener { uuid -> if (uuid == userUuid) notified += uuid }

    /** Unregisters every listener this test registered. */
    private val registrations = mutableListOf<AutoCloseable>()

    /**
     * Unregisters the listeners registered by the test.
     */
    @AfterTest
    fun unregister() {
        registrations.forEach(AutoCloseable::close)
    }

    /**
     * Registers a listener for the duration of the test.
     */
    private fun register(listener: UserStateListener) {
        registrations += UserStateListeners.register(listener)
    }

    /**
     * Builds a user holding one police identity with the given rank key.
     */
    private fun user(identityUuid: UUID, rankKey: String) =
        CoreRoleplayUser(userUuid, mockk<UserService>(relaxed = true), IdentityAccountResolver({ null }, { _, _ -> error("unused") }))
            .also { it.applyState(dto(identityUuid, rankKey)) }

    /**
     * Builds the state of a user holding one police identity with the given rank key.
     */
    private fun dto(identityUuid: UUID, rankKey: String) = RoleplayUserDto(
        userUuid,
        listOf(RoleplayIdentityDto(identityUuid, IdentityType.POLICE.name, UUID.randomUUID(), rankKey, emptyList(), emptyList())),
    )

    /**
     * Verifies that activating and clearing the active identity and applying a new state each
     * notify the listener.
     */
    @Test
    fun `identity changes notify listeners`() = runBlocking {
        val identityUuid = UUID.randomUUID()
        val user = user(identityUuid, PoliceRank.Cadet.key.asString())
        register(listener)

        user.setActiveIdentity(user.identities.single())
        assertEquals(1, notified.size)

        user.applyState(dto(identityUuid, PoliceRank.PoliceOfficer.key.asString()))
        assertEquals(2, notified.size)

        user.clearActiveIdentity()
        assertEquals(3, notified.size)
        assertNull(user.activeIdentity)
    }

    /**
     * Verifies that an unregistered listener is no longer notified.
     */
    @Test
    fun `unregistered listener is not notified`() = runBlocking {
        val user = user(UUID.randomUUID(), PoliceRank.Cadet.key.asString())
        UserStateListeners.register(listener).close()

        user.setActiveIdentity(user.identities.single())

        assertEquals(emptyList(), notified)
    }

    /**
     * Verifies that a throwing listener neither fails the change nor keeps other listeners from
     * being notified.
     */
    @Test
    fun `throwing listener does not break the change`() = runBlocking {
        val user = user(UUID.randomUUID(), PoliceRank.Cadet.key.asString())
        register { error("broken") }
        register(listener)

        user.setActiveIdentity(user.identities.single())

        assertEquals(user.identities.single(), user.activeIdentity)
        assertEquals(1, notified.size)
    }
}
