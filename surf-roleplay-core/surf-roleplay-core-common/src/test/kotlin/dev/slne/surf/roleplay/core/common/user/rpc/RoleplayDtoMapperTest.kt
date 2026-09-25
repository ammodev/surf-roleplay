package dev.slne.surf.roleplay.core.common.user.rpc

import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceQualification
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceRank
import dev.slne.surf.roleplay.api.common.identity.identities.sar.SarQualification
import dev.slne.surf.roleplay.api.common.identity.identities.sar.SarRank
import dev.slne.surf.roleplay.api.common.license.licenses.CarLicense
import dev.slne.surf.roleplay.api.common.license.licenses.TruckLicense
import dev.slne.surf.roleplay.api.common.license.revoke.LicenseRevokedReason
import dev.slne.surf.roleplay.core.common.identity.CoreCivilianIdentity
import dev.slne.surf.roleplay.core.common.identity.CorePoliceIdentity
import dev.slne.surf.roleplay.core.common.identity.CoreSarIdentity
import dev.slne.surf.roleplay.core.common.user.CoreRoleplayUser
import io.mockk.mockk
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the conversion of user, identity and license DTOs into in-memory domain values.
 */
class RoleplayDtoMapperTest {

    private val service = mockk<UserService>(relaxed = true)
    private val userUuid = UUID.randomUUID()
    private val owner = CoreRoleplayUser(userUuid, service)

    /**
     * Builds an identity DTO of [type] with the given rank and qualification keys and licenses.
     */
    private fun identityDto(
        type: String,
        rankKey: String? = null,
        qualificationKeys: List<String> = emptyList(),
        licenses: List<UserLicenseDto> = emptyList()
    ) = RoleplayIdentityDto(
        uuid = UUID.randomUUID(),
        type = type,
        accountId = UUID.randomUUID(),
        rankKey = rankKey,
        qualificationKeys = qualificationKeys,
        licenses = licenses
    )

    /**
     * Returns the current time truncated to whole seconds in the system default zone.
     */
    private fun now(): OffsetDateTime =
        OffsetDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS)

    /**
     * Verifies that a civilian identity DTO maps its identifiers and licenses to the domain identity.
     */
    @Test
    fun `civilian identity maps its identifiers and licenses`() {
        val license = UserLicenseDto(CarLicense.key.asString(), now(), null, null, null, null)
        val dto = identityDto(IdentityType.CIVILIAN.name, licenses = listOf(license))

        val identity = assertIs<CoreCivilianIdentity>(dto.toDomain(owner))

        assertEquals(dto.uuid, identity.uuid)
        assertEquals(userUuid, identity.userUuid)
        assertEquals(dto.accountId, identity.accountId)
        assertEquals(IdentityType.CIVILIAN, identity.type)
        assertEquals(setOf(CarLicense.key), identity.licenses.map { it.licenseKey }.toSet())
    }

    /**
     * Verifies that a police identity DTO maps its rank and qualifications to the domain identity.
     */
    @Test
    fun `police identity maps its rank and qualifications`() {
        val dto = identityDto(
            IdentityType.POLICE.name,
            rankKey = PoliceRank.ChiefInspector.key.asString(),
            qualificationKeys = listOf(
                PoliceQualification.DogHandler.key.asString(),
                PoliceQualification.TrafficPolice.key.asString()
            )
        )

        val identity = assertIs<CorePoliceIdentity>(dto.toDomain(owner))

        assertEquals(IdentityType.POLICE, identity.type)
        assertEquals(PoliceRank.ChiefInspector, identity.rank)
        assertEquals(
            setOf(PoliceQualification.DogHandler, PoliceQualification.TrafficPolice),
            identity.qualifications.toSet()
        )
    }

    /**
     * Verifies that a search-and-rescue identity DTO maps its rank and qualifications to the domain identity.
     */
    @Test
    fun `sar identity maps its rank and qualifications`() {
        val dto = identityDto(
            IdentityType.SAR.name,
            rankKey = SarRank.Paramedic.key.asString(),
            qualificationKeys = listOf(SarQualification.WaterRescue.key.asString())
        )

        val identity = assertIs<CoreSarIdentity>(dto.toDomain(owner))

        assertEquals(IdentityType.SAR, identity.type)
        assertEquals(SarRank.Paramedic, identity.rank)
        assertEquals(setOf(SarQualification.WaterRescue), identity.qualifications.toSet())
    }

    /**
     * Verifies that a rank key naming no known rank resolves to the lowest rank of the identity's organisation.
     */
    @Test
    fun `unknown rank key resolves to the lowest rank`() {
        val police = identityDto(IdentityType.POLICE.name, rankKey = "roleplay:no_such_rank")
        val sar = identityDto(IdentityType.SAR.name, rankKey = "Not A Valid Key!")

        assertEquals(PoliceRank.Cadet, assertIs<CorePoliceIdentity>(police.toDomain(owner)).rank)
        assertEquals(SarRank.RescueAssistant, assertIs<CoreSarIdentity>(sar.toDomain(owner)).rank)
    }

    /**
     * Verifies that a missing rank key resolves to the lowest rank of the identity's organisation.
     */
    @Test
    fun `missing rank key resolves to the lowest rank`() {
        val police = identityDto(IdentityType.POLICE.name, rankKey = null)
        val sar = identityDto(IdentityType.SAR.name, rankKey = null)

        assertEquals(PoliceRank.Cadet, assertIs<CorePoliceIdentity>(police.toDomain(owner)).rank)
        assertEquals(SarRank.RescueAssistant, assertIs<CoreSarIdentity>(sar.toDomain(owner)).rank)
    }

    /**
     * Verifies that qualification keys naming no known qualification are dropped while known ones are kept.
     */
    @Test
    fun `unknown qualification keys are dropped`() {
        val police = identityDto(
            IdentityType.POLICE.name,
            qualificationKeys = listOf(
                "roleplay:no_such_qualification",
                PoliceQualification.SpecialForces.key.asString()
            )
        )
        val sar = identityDto(
            IdentityType.SAR.name,
            qualificationKeys = listOf("roleplay:no_such_qualification", "Not A Valid Key!")
        )

        assertEquals(
            setOf(PoliceQualification.SpecialForces),
            assertIs<CorePoliceIdentity>(police.toDomain(owner)).qualifications.toSet()
        )
        assertTrue(assertIs<CoreSarIdentity>(sar.toDomain(owner)).qualifications.isEmpty())
    }

    /**
     * Verifies that identities of an unknown type are left out of the mapped user.
     */
    @Test
    fun `identities of an unknown type are skipped`() {
        val civilian = identityDto(IdentityType.CIVILIAN.name)
        val user = RoleplayUserDto(userUuid, listOf(identityDto("FIRE_DEPARTMENT"), civilian))

        val domain = user.toDomain(service)

        assertEquals(userUuid, domain.uuid)
        assertEquals(listOf(civilian.uuid), domain.identities.map { it.uuid })
        assertNull(identityDto("FIRE_DEPARTMENT").toDomain(owner))
        assertNull(domain.activeIdentity)
    }

    /**
     * Verifies that mapping a revoked license DTO keeps every revocation field.
     */
    @Test
    fun `revoked license keeps every revocation field`() {
        val grantedBy = UUID.randomUUID()
        val revokedBy = UUID.randomUUID()
        val acquiredAt = now().minusDays(3)
        val revokedAt = now()
        val dto = UserLicenseDto(
            licenseKey = TruckLicense.key.asString(),
            acquiredAt = acquiredAt,
            grantedByUuid = grantedBy,
            revokedByUuid = revokedBy,
            revokedReason = LicenseRevokedReason.CRIMINAL.name,
            revokedAt = revokedAt
        )

        val license = assertNotNull(dto.toDomain())

        assertEquals(TruckLicense.key, license.licenseKey)
        assertEquals(acquiredAt, license.acquiredAt)
        assertEquals(grantedBy, license.grantedByUuid)
        assertEquals(revokedBy, license.revokedByUuid)
        assertEquals(LicenseRevokedReason.CRIMINAL, license.revokedReason)
        assertEquals(revokedAt, license.revokedAt)
        assertTrue(license.isRevoked)
    }

    /**
     * Verifies that a revocation reason naming no known reason maps to null while the license is still reported as revoked.
     */
    @Test
    fun `unknown revocation reason maps to null while the license stays revoked`() {
        val dto = UserLicenseDto(
            licenseKey = CarLicense.key.asString(),
            acquiredAt = now().minusDays(1),
            grantedByUuid = null,
            revokedByUuid = UUID.randomUUID(),
            revokedReason = "NO_SUCH_REASON",
            revokedAt = now()
        )

        val license = assertNotNull(dto.toDomain())

        assertNull(license.revokedReason)
        assertTrue(license.isRevoked)
    }

    /**
     * Verifies that a license DTO with a malformed key is dropped while valid ones are kept.
     */
    @Test
    fun `licenses with a malformed key are dropped`() {
        val valid = UserLicenseDto(CarLicense.key.asString(), now(), null, null, null, null)
        val malformed = UserLicenseDto("Not A Valid Key!", now(), null, null, null, null)
        val dto = identityDto(IdentityType.CIVILIAN.name, licenses = listOf(malformed, valid))

        val identity = assertIs<CoreCivilianIdentity>(dto.toDomain(owner))

        assertNull(malformed.toDomain())
        assertEquals(listOf(CarLicense.key), identity.licenses.map { it.licenseKey })
    }
}
