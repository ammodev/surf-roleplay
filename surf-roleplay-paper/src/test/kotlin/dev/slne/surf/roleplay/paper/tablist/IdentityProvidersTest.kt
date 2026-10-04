package dev.slne.surf.roleplay.paper.tablist

import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceIdentity
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceRank
import dev.slne.surf.roleplay.api.common.identity.identities.sar.SarIdentity
import dev.slne.surf.roleplay.api.common.identity.identities.sar.SarRank
import dev.slne.surf.roleplay.api.common.user.RoleplayUser
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the built-in organisation and self-info providers based on the active identity.
 */
class IdentityProvidersTest {

    /** The held users by player UUID. */
    private val users = HashMap<UUID, RoleplayUser>()

    /** The providers under test, reading [users]. */
    private val providers = IdentityProviders { users[it] }

    /**
     * Holds a user whose active identity is the given one and returns the player's UUID.
     */
    private fun player(active: RoleplayIdentity?): UUID {
        val uuid = UUID.randomUUID()
        users[uuid] = mockk { every { activeIdentity } returns active }
        return uuid
    }

    /**
     * Returns a plain identity of the given type.
     */
    private fun identity(type: IdentityType): RoleplayIdentity = mockk { every { this@mockk.type } returns type }

    /**
     * Verifies the keys, German labels and order of the built-in organisations.
     */
    @Test
    fun `organisations in order`() {
        assertEquals(listOf("civilian", "police", "sar"), providers.organisations.map { it.key })
        assertEquals(listOf("Zivilisten", "Polizei", "SAR"), providers.organisations.map { IdentityProviders.plain(it.label) })
    }

    /**
     * Verifies that each player counts for the organisation of their active identity only.
     */
    @Test
    fun `players count by the type of their active identity`() {
        val (civilian, police, sar) = providers.organisations
        val civilianPlayer = player(identity(IdentityType.CIVILIAN))
        val policePlayer = player(identity(IdentityType.POLICE))

        assertTrue(civilian.isMember(civilianPlayer))
        assertFalse(police.isMember(civilianPlayer))
        assertTrue(police.isMember(policePlayer))
        assertFalse(sar.isMember(policePlayer))
    }

    /**
     * Verifies that a player without an active identity or without a held user counts nowhere.
     */
    @Test
    fun `no active identity counts nowhere`() {
        val inactive = player(null)
        val unknown = UUID.randomUUID()

        providers.organisations.forEach {
            assertFalse(it.isMember(inactive))
            assertFalse(it.isMember(unknown))
        }
    }

    /**
     * Verifies that the rank comes from the active police or SAR identity as its German name.
     */
    @Test
    fun `rank of the active identity`() {
        val police = mockk<PoliceIdentity> {
            every { type } returns IdentityType.POLICE
            every { rank } returns PoliceRank.ChiefInspector
        }
        val sar = mockk<SarIdentity> {
            every { type } returns IdentityType.SAR
            every { rank } returns SarRank.EmergencyPhysician
        }

        assertEquals("Polizeihauptkommissar", providers.selfInfo.rankOf(player(police)))
        assertEquals("Notarzt", providers.selfInfo.rankOf(player(sar)))
    }

    /**
     * Verifies that civilians, players without an active identity and unknown players have no
     * rank, and that no character name is known yet.
     */
    @Test
    fun `no rank and no character name`() {
        assertNull(providers.selfInfo.rankOf(player(identity(IdentityType.CIVILIAN))))
        assertNull(providers.selfInfo.rankOf(player(null)))
        assertNull(providers.selfInfo.rankOf(UUID.randomUUID()))
    }
}
