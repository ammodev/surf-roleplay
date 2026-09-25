package dev.slne.surf.roleplay.core.common.identity

import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.license.LicenseGrantResult
import dev.slne.surf.roleplay.api.common.license.licenses.CarLicense
import dev.slne.surf.roleplay.api.common.license.licenses.TruckLicense
import dev.slne.surf.roleplay.api.common.license.requirement.requirements.HasOtherLicenseRequirement
import dev.slne.surf.roleplay.api.common.license.revoke.LicenseRevokedReason
import dev.slne.surf.roleplay.core.common.identity.account.IdentityAccountResolver
import dev.slne.surf.roleplay.core.common.user.CoreRoleplayUser
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayIdentityDto
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayUserDto
import dev.slne.surf.roleplay.core.common.user.rpc.UserLicenseDto
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for granting and revoking licenses on [CoreRoleplayIdentity].
 */
class CoreRoleplayIdentityLicenseTest {

    private val service = mockk<UserService>(relaxed = true)
    private val userUuid = UUID.randomUUID()
    private val identityUuid = UUID.randomUUID()
    private val accountId = UUID.randomUUID()
    private val granter = UUID.randomUUID()
    private val acquiredAt = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS)

    private val resolver = IdentityAccountResolver(
        findByName = { error("accounts are not looked up in this test") },
        createAccount = { _, _ -> error("accounts are not created in this test") }
    )

    /**
     * Builds the state of the user with a single civilian identity holding [licenses].
     */
    private fun state(vararg licenses: UserLicenseDto) = RoleplayUserDto(
        userUuid,
        listOf(
            RoleplayIdentityDto(
                identityUuid,
                IdentityType.CIVILIAN.name,
                accountId,
                null,
                emptyList(),
                licenses.toList()
            )
        )
    )

    /**
     * Builds an unrevoked license DTO for the license with [key].
     */
    private fun held(key: String, grantedBy: UUID? = null) =
        UserLicenseDto(key, acquiredAt, grantedBy, null, null, null)

    /**
     * Builds a user holding a single civilian identity with [licenses] and returns that identity.
     */
    private fun identityWith(vararg licenses: UserLicenseDto): CoreRoleplayIdentity {
        val user = CoreRoleplayUser(userUuid, service, resolver)
        user.applyState(state(*licenses))
        return user.identities.single() as CoreRoleplayIdentity
    }

    @Test
    fun `granting a held license reports AlreadyOwned without sending`() = runBlocking {
        val identity = identityWith(held(CarLicense.key.asString()))

        val result = identity.grantLicense(CarLicense, granter, force = false)

        assertSame(LicenseGrantResult.AlreadyOwned, result)
        coVerify(exactly = 0) { service.grantLicense(any(), any(), any(), any()) }
    }

    @Test
    fun `granting a license with unmet requirements reports the breakdown without sending`() = runBlocking {
        val identity = identityWith()

        val result = assertIs<LicenseGrantResult.RequirementsNotMet>(
            identity.grantLicense(TruckLicense, granter, force = false)
        )

        assertFalse(result.calculation.isMet)
        assertFalse(result.calculation.results.getBoolean(HasOtherLicenseRequirement(CarLicense)))
        coVerify(exactly = 0) { service.grantLicense(any(), any(), any(), any()) }
    }

    @Test
    fun `forcing a grant skips the requirements and applies the returned state`() = runBlocking {
        val identity = identityWith()
        val truckKey = TruckLicense.key.asString()
        coEvery { service.grantLicense(userUuid, identityUuid, truckKey, granter) } returns
                state(held(truckKey, granter))

        val result = assertIs<LicenseGrantResult.Granted>(
            identity.grantLicense(TruckLicense, granter, force = true)
        )

        assertEquals(TruckLicense.key, result.userLicense.licenseKey)
        assertEquals(granter, result.userLicense.grantedByUuid)
        assertNull(result.userLicense.revokedAt)
        assertTrue(identity.hasLicense(TruckLicense))
    }

    @Test
    fun `granting a license with met requirements sends it`() = runBlocking {
        val carKey = CarLicense.key.asString()
        val truckKey = TruckLicense.key.asString()
        val identity = identityWith(held(carKey))
        coEvery { service.grantLicense(userUuid, identityUuid, truckKey, null) } returns
                state(held(carKey), held(truckKey))

        val result = identity.grantLicense(TruckLicense, null, force = false)

        assertIs<LicenseGrantResult.Granted>(result)
        assertTrue(identity.hasLicense(TruckLicense))
    }

    @Test
    fun `revoking a license that is not held returns false without sending`() = runBlocking {
        val identity = identityWith()

        assertFalse(identity.revokeLicense(CarLicense, granter, LicenseRevokedReason.CRIMINAL))
        coVerify(exactly = 0) { service.revokeLicense(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `revoking a held license applies the returned state`() = runBlocking {
        val carKey = CarLicense.key.asString()
        val identity = identityWith(held(carKey))
        coEvery {
            service.revokeLicense(userUuid, identityUuid, carKey, granter, LicenseRevokedReason.CRIMINAL.name)
        } returns state(
            UserLicenseDto(carKey, acquiredAt, null, granter, LicenseRevokedReason.CRIMINAL.name, acquiredAt)
        )

        assertTrue(identity.revokeLicense(CarLicense, granter, LicenseRevokedReason.CRIMINAL))
        assertFalse(identity.hasLicense(CarLicense))
    }
}
