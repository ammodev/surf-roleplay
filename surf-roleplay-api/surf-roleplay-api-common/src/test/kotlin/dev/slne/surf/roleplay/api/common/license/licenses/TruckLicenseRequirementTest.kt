package dev.slne.surf.roleplay.api.common.license.licenses

import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import dev.slne.surf.roleplay.api.common.license.user.UserLicense
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

    /**
     * Builds an identity fake that holds exactly [userLicenses].
     */
    private fun identityWith(vararg userLicenses: UserLicense): RoleplayIdentity {
        val identity = mockk<RoleplayIdentity>(relaxed = true)
        every { identity.licenses } returns ObjectOpenHashSet(userLicenses.toList())
        every { identity.hasLicense(any()) } answers { callOriginal() }
        return identity
    }

    /**
     * Verifies that the requirement is met when the identity already holds the car license.
     */
    @Test
    fun `requirement is met when the identity holds the car license`() {
        val identity = identityWith(
            UserLicense(licenseKey = CarLicense.key, acquiredAt = OffsetDateTime.now())
        )

        val result = TruckLicense.calculateRequirements(identity)

        assertTrue(result.isMet)
    }

    /**
     * Verifies that the requirement is not met when the identity lacks the car license.
     */
    @Test
    fun `requirement is not met when the identity lacks the car license`() {
        val identity = identityWith()

        val result = TruckLicense.calculateRequirements(identity)

        assertFalse(result.isMet)
    }
}
