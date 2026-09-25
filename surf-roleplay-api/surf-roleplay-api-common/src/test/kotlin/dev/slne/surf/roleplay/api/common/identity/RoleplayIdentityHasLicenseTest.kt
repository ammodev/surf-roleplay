package dev.slne.surf.roleplay.api.common.identity

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
 * Tests for [RoleplayIdentity.hasLicense].
 */
class RoleplayIdentityHasLicenseTest {

    /**
     * Builds a mocked [RoleplayIdentity] whose [RoleplayIdentity.licenses] contains [userLicenses]
     * and whose [RoleplayIdentity.hasLicense] runs the real implementation.
     */
    private fun identityWith(vararg userLicenses: UserLicense): RoleplayIdentity {
        val identity = mockk<RoleplayIdentity>(relaxed = true)
        every { identity.licenses } returns ObjectOpenHashSet(userLicenses.toList())
        every { identity.hasLicense(any()) } answers { callOriginal() }
        return identity
    }

    /**
     * Verifies that a held, non-revoked license is reported as held.
     */
    @Test
    fun `hasLicense is true for a held, non-revoked license`() {
        val identity = identityWith(
            UserLicense(licenseKey = CarLicense.key, acquiredAt = OffsetDateTime.now())
        )

        assertTrue(identity.hasLicense(CarLicense))
    }

    /**
     * Verifies that a revoked license is not reported as held.
     */
    @Test
    fun `hasLicense is false for a revoked license`() {
        val identity = identityWith(
            UserLicense(
                licenseKey = CarLicense.key,
                acquiredAt = OffsetDateTime.now(),
                revokedByUuid = UUID.randomUUID(),
                revokedReason = LicenseRevokedReason.ADMINISTRATIVE,
                revokedAt = OffsetDateTime.now()
            )
        )

        assertFalse(identity.hasLicense(CarLicense))
    }

    /**
     * Verifies that a license the identity never held is not reported as held.
     */
    @Test
    fun `hasLicense is false when the license was never held`() {
        val identity = identityWith()

        assertFalse(identity.hasLicense(CarLicense))
    }
}
