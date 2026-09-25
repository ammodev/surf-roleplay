package dev.slne.surf.roleplay.api.common.license.requirement.requirements

import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import dev.slne.surf.roleplay.api.common.license.licenses.CarLicense
import dev.slne.surf.roleplay.api.common.license.revoke.LicenseRevokedReason
import dev.slne.surf.roleplay.api.common.license.user.UserLicense
import io.mockk.every
import io.mockk.mockk
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [HasOtherLicenseRequirement.isMet].
 */
class HasOtherLicenseRequirementTest {

    private val requirement = HasOtherLicenseRequirement(CarLicense)

    /**
     * Builds an identity fake that holds exactly [userLicenses].
     */
    private fun identityWith(vararg userLicenses: UserLicense): RoleplayIdentity {
        val identity = mockk<RoleplayIdentity>(relaxed = true)
        every { identity.licenses } returns ObjectOpenHashSet(userLicenses.toList())
        every { identity.hasLicense(any()) } answers { callOriginal() }
        return identity
    }

    @Test
    fun `requirement is met when the identity holds the prerequisite license`() {
        val identity = identityWith(
            UserLicense(licenseKey = CarLicense.key, acquiredAt = OffsetDateTime.now())
        )

        assertTrue(requirement.isMet(identity))
    }

    @Test
    fun `requirement is not met when the identity lacks the prerequisite license`() {
        val identity = identityWith()

        assertFalse(requirement.isMet(identity))
    }

    @Test
    fun `requirement is not met when the prerequisite license is revoked`() {
        val identity = identityWith(
            UserLicense(
                licenseKey = CarLicense.key,
                acquiredAt = OffsetDateTime.now(),
                revokedByUuid = UUID.randomUUID(),
                revokedReason = LicenseRevokedReason.CRIMINAL,
                revokedAt = OffsetDateTime.now()
            )
        )

        assertFalse(requirement.isMet(identity))
    }
}
