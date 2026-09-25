package dev.slne.surf.roleplay.core.common.user

import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.identity.exceptions.IdentityAlreadyExistsException
import dev.slne.surf.roleplay.api.common.identity.exceptions.UnknownIdentityException
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceQualification
import dev.slne.surf.roleplay.api.common.identity.identities.police.PoliceRank
import dev.slne.surf.roleplay.api.common.license.licenses.CarLicense
import dev.slne.surf.roleplay.core.common.identity.CorePoliceIdentity
import dev.slne.surf.roleplay.core.common.identity.account.IdentityAccountResolver
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayIdentityDto
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayUserDto
import dev.slne.surf.roleplay.core.common.user.rpc.UserLicenseDto
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import dev.slne.surf.transaction.api.account.Account
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for identity creation, state application and active-identity handling of
 * [CoreRoleplayUser].
 */
class CoreRoleplayUserTest {

    private val service = mockk<UserService>(relaxed = true)
    private val userUuid = UUID.randomUUID()

    /** The number of times the account resolver looked up or created an account. */
    private var resolverCalls = 0

    private val resolvedAccountId = UUID.randomUUID()

    private val resolver = IdentityAccountResolver(
        findByName = {
            resolverCalls++
            null
        },
        createAccount = { _, _ ->
            resolverCalls++
            error("accounts are not created in this test")
        }
    )

    /**
     * Builds an identity DTO of [type] with [uuid] and the given rank, qualification keys and
     * licenses.
     */
    private fun identityDto(
        type: IdentityType,
        uuid: UUID = UUID.randomUUID(),
        rankKey: String? = null,
        qualificationKeys: List<String> = emptyList(),
        licenses: List<UserLicenseDto> = emptyList()
    ) = RoleplayIdentityDto(uuid, type.name, UUID.randomUUID(), rankKey, qualificationKeys, licenses)

    /**
     * Builds a user owned by [userUuid] holding the identities described by [identities].
     */
    private fun user(vararg identities: RoleplayIdentityDto) =
        CoreRoleplayUser(userUuid, service, resolver).also {
            it.applyState(RoleplayUserDto(userUuid, identities.toList()))
        }

    @Test
    fun `setActiveIdentity activates the owned instance`() = runBlocking {
        val dto = identityDto(IdentityType.CIVILIAN)
        val user = user(dto)
        val equalUuidCopy = user(dto).identities.single()

        user.setActiveIdentity(equalUuidCopy)

        assertSame(user.identities.single(), user.activeIdentity)
    }

    @Test
    fun `setActiveIdentity rejects a foreign identity`() = runBlocking {
        val user = user(identityDto(IdentityType.CIVILIAN))
        val foreign = user(identityDto(IdentityType.CIVILIAN)).identities.single()

        val exception = assertFailsWith<UnknownIdentityException> {
            user.setActiveIdentity(foreign)
        }

        assertEquals(userUuid, exception.userUuid)
        assertEquals(foreign.uuid, exception.identityUuid)
        assertNull(user.activeIdentity)
    }

    @Test
    fun `clearActiveIdentity leaves no identity active`() = runBlocking {
        val user = user(identityDto(IdentityType.CIVILIAN))
        user.setActiveIdentity(user.identities.single())

        user.clearActiveIdentity()

        assertNull(user.activeIdentity)
    }

    @Test
    fun `createIdentity rejects a second identity of the same type without resolving or sending`() {
        val user = user(identityDto(IdentityType.POLICE))

        val exception = assertFailsWith<IdentityAlreadyExistsException> {
            runBlocking { user.createIdentity(IdentityType.POLICE) }
        }

        assertEquals(userUuid, exception.userUuid)
        assertEquals(IdentityType.POLICE, exception.type)
        assertEquals(0, resolverCalls)
        coVerify(exactly = 0) { service.createIdentity(any(), any(), any()) }
    }

    @Test
    fun `createIdentity sends the resolved account and returns the new identity`() = runBlocking {
        val ownAccount = mockk<Account>()
        every { ownAccount.ownerUuid } returns userUuid
        every { ownAccount.accountId } returns resolvedAccountId
        val reusingResolver = IdentityAccountResolver(
            findByName = { ownAccount },
            createAccount = { _, _ -> error("accounts are not created in this test") }
        )
        val civilian = identityDto(IdentityType.CIVILIAN)
        val user = CoreRoleplayUser(userUuid, service, reusingResolver).also {
            it.applyState(RoleplayUserDto(userUuid, listOf(civilian)))
        }
        val existing = user.identities.single()
        val created = identityDto(IdentityType.SAR)
        coEvery { service.createIdentity(userUuid, IdentityType.SAR.name, resolvedAccountId) } returns
                RoleplayUserDto(userUuid, listOf(civilian, created))

        val identity = user.createIdentity(IdentityType.SAR)

        assertEquals(created.uuid, identity.uuid)
        assertEquals(IdentityType.SAR, identity.type)
        assertSame(existing, user.identities.first { it.uuid == civilian.uuid })
    }

    @Test
    fun `createIdentity rejected by the service fails with IdentityAlreadyExistsException`() {
        val ownAccount = mockk<Account>()
        every { ownAccount.ownerUuid } returns userUuid
        every { ownAccount.accountId } returns resolvedAccountId
        val user = CoreRoleplayUser(
            userUuid,
            service,
            IdentityAccountResolver(findByName = { ownAccount }, createAccount = { _, _ -> error("unused") })
        )
        coEvery { service.createIdentity(any(), any(), any()) } returns null

        assertFailsWith<IdentityAlreadyExistsException> {
            runBlocking { user.createIdentity(IdentityType.CIVILIAN) }
        }
    }

    @Test
    fun `applyState updates held identities in place`() = runBlocking {
        val policeUuid = UUID.randomUUID()
        val user = user(identityDto(IdentityType.POLICE, uuid = policeUuid))
        val police = assertIs<CorePoliceIdentity>(user.identities.single())
        user.setActiveIdentity(police)
        val acquiredAt = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS)

        user.applyState(
            RoleplayUserDto(
                userUuid,
                listOf(
                    identityDto(
                        IdentityType.POLICE,
                        uuid = policeUuid,
                        rankKey = PoliceRank.Inspector.key.asString(),
                        qualificationKeys = listOf(PoliceQualification.DogHandler.key.asString()),
                        licenses = listOf(
                            UserLicenseDto(CarLicense.key.asString(), acquiredAt, null, null, null, null)
                        )
                    )
                )
            )
        )

        assertSame(police, user.identities.single())
        assertSame(police, user.activeIdentity)
        assertEquals(PoliceRank.Inspector, police.rank)
        assertEquals(setOf(PoliceQualification.DogHandler), police.qualifications.toSet())
        assertTrue(police.hasLicense(CarLicense))
    }

    @Test
    fun `applyState adds new identities and removes missing ones`() {
        val kept = identityDto(IdentityType.CIVILIAN)
        val removed = identityDto(IdentityType.POLICE)
        val added = identityDto(IdentityType.SAR)
        val user = user(kept, removed)
        val keptInstance = user.identities.first { it.uuid == kept.uuid }

        user.applyState(RoleplayUserDto(userUuid, listOf(kept, added)))

        assertEquals(listOf(kept.uuid, added.uuid), user.identities.map { it.uuid })
        assertSame(keptInstance, user.identities.first())
    }

    @Test
    fun `applyState clears the active identity when it is missing`() = runBlocking {
        val kept = identityDto(IdentityType.CIVILIAN)
        val removed = identityDto(IdentityType.POLICE)
        val user = user(kept, removed)
        user.setActiveIdentity(user.identities.first { it.uuid == removed.uuid })

        user.applyState(RoleplayUserDto(userUuid, listOf(kept)))

        assertNull(user.activeIdentity)
    }
}
