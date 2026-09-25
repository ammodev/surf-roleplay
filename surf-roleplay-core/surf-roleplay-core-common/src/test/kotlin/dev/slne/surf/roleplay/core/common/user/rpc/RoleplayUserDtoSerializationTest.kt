package dev.slne.surf.roleplay.core.common.user.rpc

import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceQualification
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceRank
import dev.slne.surf.roleplay.api.common.identity.identities.sar.SarQualification
import dev.slne.surf.roleplay.api.common.identity.identities.sar.SarRank
import dev.slne.surf.roleplay.api.common.license.licenses.CarLicense
import dev.slne.surf.roleplay.api.common.license.licenses.TruckLicense
import dev.slne.surf.roleplay.api.common.license.revoke.LicenseRevokedReason
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that a complete user DTO survives JSON encoding and decoding unchanged.
 */
class RoleplayUserDtoSerializationTest {

    /**
     * Converts [instant] to an offset date-time carrying the system zone's offset at that instant.
     */
    private fun at(instant: Instant): OffsetDateTime =
        instant.atZone(ZoneId.systemDefault()).toOffsetDateTime()

    @Test
    fun `full user round-trips through JSON`() {
        val now = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        val user = RoleplayUserDto(
            uuid = UUID.randomUUID(),
            identities = listOf(
                RoleplayIdentityDto(
                    uuid = UUID.randomUUID(),
                    type = IdentityType.CIVILIAN.name,
                    accountId = UUID.randomUUID(),
                    rankKey = null,
                    qualificationKeys = emptyList(),
                    licenses = listOf(
                        UserLicenseDto(CarLicense.key.asString(), at(now.minus(10, ChronoUnit.DAYS)), null, null, null, null),
                        UserLicenseDto(
                            licenseKey = TruckLicense.key.asString(),
                            acquiredAt = at(now.minus(5, ChronoUnit.DAYS)),
                            grantedByUuid = UUID.randomUUID(),
                            revokedByUuid = UUID.randomUUID(),
                            revokedReason = LicenseRevokedReason.ADMINISTRATIVE.name,
                            revokedAt = at(now)
                        )
                    )
                ),
                RoleplayIdentityDto(
                    uuid = UUID.randomUUID(),
                    type = IdentityType.POLICE.name,
                    accountId = UUID.randomUUID(),
                    rankKey = PoliceRank.Inspector.key.asString(),
                    qualificationKeys = listOf(PoliceQualification.HelicopterPilot.key.asString()),
                    licenses = emptyList()
                ),
                RoleplayIdentityDto(
                    uuid = UUID.randomUUID(),
                    type = IdentityType.SAR.name,
                    accountId = UUID.randomUUID(),
                    rankKey = SarRank.EmergencyPhysician.key.asString(),
                    qualificationKeys = listOf(
                        SarQualification.AirRescue.key.asString(),
                        SarQualification.MountainRescue.key.asString()
                    ),
                    licenses = emptyList()
                )
            )
        )

        val encoded = Json.encodeToString(RoleplayUserDto.serializer(), user)
        val decoded = Json.decodeFromString(RoleplayUserDto.serializer(), encoded)

        assertEquals(user, decoded)
    }
}
