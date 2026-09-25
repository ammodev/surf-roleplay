package dev.slne.surf.roleplay.core.common.identity

import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceQualification
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceRank
import dev.slne.surf.roleplay.core.common.identity.account.IdentityAccountResolver
import dev.slne.surf.roleplay.core.common.user.CoreRoleplayUser
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayIdentityDto
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayUserDto
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for rank and qualification changes on [CorePoliceIdentity].
 */
class CorePoliceIdentityTest {

    private val service = mockk<UserService>(relaxed = true)
    private val userUuid = UUID.randomUUID()
    private val identityUuid = UUID.randomUUID()
    private val accountId = UUID.randomUUID()

    /**
     * Builds the state of the user with a single police identity of [rank] holding
     * [qualifications].
     */
    private fun state(rank: PoliceRank, vararg qualifications: PoliceQualification) = RoleplayUserDto(
        userUuid,
        listOf(
            RoleplayIdentityDto(
                identityUuid,
                IdentityType.POLICE.name,
                accountId,
                rank.key.asString(),
                qualifications.map { it.key.asString() },
                emptyList()
            )
        )
    )

    /**
     * Builds a user holding the police identity described by [state] and returns that identity.
     */
    private fun identity(state: RoleplayUserDto): CorePoliceIdentity {
        val resolver = IdentityAccountResolver({ error("unused") }, { _, _ -> error("unused") })
        val user = CoreRoleplayUser(userUuid, service, resolver)
        user.applyState(state)
        return user.identities.single() as CorePoliceIdentity
    }

    /**
     * Verifies that setting the currently held rank sends nothing to the remote user service.
     */
    @Test
    fun `setRank to the held rank sends nothing`() = runBlocking {
        val police = identity(state(PoliceRank.Inspector))

        police.setRank(PoliceRank.Inspector)

        coVerify(exactly = 0) { service.setRank(any(), any(), any()) }
    }

    /**
     * Verifies that a successful setRank call applies the rank returned by the remote user service.
     */
    @Test
    fun `setRank applies the returned rank`() = runBlocking {
        val police = identity(state(PoliceRank.Cadet))
        coEvery { service.setRank(userUuid, identityUuid, PoliceRank.Inspector.key.asString()) } returns
                state(PoliceRank.Inspector)

        police.setRank(PoliceRank.Inspector)

        assertEquals(PoliceRank.Inspector, police.rank)
    }

    /**
     * Verifies that adding an already-held qualification returns false without sending anything.
     */
    @Test
    fun `adding a held qualification returns false without sending`() = runBlocking {
        val police = identity(state(PoliceRank.Cadet, PoliceQualification.DogHandler))

        assertFalse(police.addQualification(PoliceQualification.DogHandler))
        coVerify(exactly = 0) { service.addQualification(any(), any(), any()) }
    }

    /**
     * Verifies that addQualification applies the qualifications returned by the remote user service.
     */
    @Test
    fun `adding a qualification applies the returned state`() = runBlocking {
        val police = identity(state(PoliceRank.Cadet))
        coEvery {
            service.addQualification(userUuid, identityUuid, PoliceQualification.DogHandler.key.asString())
        } returns state(PoliceRank.Cadet, PoliceQualification.DogHandler)

        assertTrue(police.addQualification(PoliceQualification.DogHandler))
        assertEquals(setOf(PoliceQualification.DogHandler), police.qualifications.toSet())
    }

    /**
     * Verifies that removing a qualification that is not held returns false without sending anything.
     */
    @Test
    fun `removing a qualification that is not held returns false without sending`() = runBlocking {
        val police = identity(state(PoliceRank.Cadet))

        assertFalse(police.removeQualification(PoliceQualification.DogHandler))
        coVerify(exactly = 0) { service.removeQualification(any(), any(), any()) }
    }

    /**
     * Verifies that removeQualification applies the qualifications returned by the remote user service.
     */
    @Test
    fun `removing a held qualification applies the returned state`() = runBlocking {
        val police = identity(state(PoliceRank.Cadet, PoliceQualification.DogHandler))
        coEvery {
            service.removeQualification(userUuid, identityUuid, PoliceQualification.DogHandler.key.asString())
        } returns state(PoliceRank.Cadet)

        assertTrue(police.removeQualification(PoliceQualification.DogHandler))
        assertTrue(police.qualifications.isEmpty())
    }
}
