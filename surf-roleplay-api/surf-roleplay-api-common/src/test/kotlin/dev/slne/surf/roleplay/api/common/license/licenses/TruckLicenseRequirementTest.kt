package dev.slne.surf.roleplay.api.common.license.licenses

import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import dev.slne.surf.roleplay.api.common.license.user.UserLicense
import dev.slne.surf.roleplay.api.common.user.RoleplayUser
import io.mockk.every
import io.mockk.mockk
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [TruckLicense]'s requirement that the car license already be held.
 */
class TruckLicenseRequirementTest {

    private fun userWithActiveIdentity(vararg userLicenses: UserLicense): RoleplayUser {
        val identity = mockk<RoleplayIdentity>(relaxed = true)
        every { identity.licenses } returns ObjectOpenHashSet(userLicenses.toList())
        every { identity.hasLicense(any()) } answers { callOriginal() }

        val user = mockk<RoleplayUser>(relaxed = true)
        every { user.activeIdentity } returns identity
        return user
    }

    @Test
    fun `requirement is met when the active identity holds the car license`() {
        val user = userWithActiveIdentity(
            UserLicense(licenseKey = CarLicense.key, acquiredAt = OffsetDateTime.now())
        )

        val result = TruckLicense.calculateRequirements(user)

        assertTrue(result.isMet)
    }

    @Test
    fun `requirement is not met when the active identity lacks the car license`() {
        val user = userWithActiveIdentity()

        val result = TruckLicense.calculateRequirements(user)

        assertFalse(result.isMet)
    }
}
