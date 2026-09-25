package dev.slne.surf.roleplay.core.common.identity.account

import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.account.result.AccountCreationResult.FailureReason
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for the account naming scheme and account lookup of [IdentityAccountResolver].
 */
class IdentityAccountResolverTest {

    private val player = UUID.fromString("0123abcd-4567-89ef-0123-456789abcdef")
    private val otherPlayer = UUID.randomUUID()

    /**
     * Builds an account fake with [name] owned by [owner].
     */
    private fun account(name: String, owner: UUID, id: UUID = UUID.randomUUID()): Account {
        val account = mockk<Account>()
        every { account.name } returns name
        every { account.ownerUuid } returns owner
        every { account.accountId } returns id
        return account
    }

    /**
     * An in-memory account store that records every creation attempt.
     *
     * @property accounts the accounts by name that already exist
     * @property createResult computes the result of a creation attempt for a name
     */
    private class Store(
        val accounts: MutableMap<String, Account> = mutableMapOf(),
        val createResult: (UUID, String) -> AccountCreationResult
    ) {
        /** Every name a creation was attempted for, in order. */
        val createdNames = mutableListOf<String>()

        /** Builds a resolver backed by this store. */
        fun resolver() = IdentityAccountResolver(
            findByName = { accounts[it] },
            createAccount = { owner, name ->
                createdNames += name
                createResult(owner, name)
            }
        )
    }

    /**
     * Verifies that the first candidate account name uses eight hex digits for every identity type.
     */
    @Test
    fun `first candidate name uses eight hex digits for every identity type`() {
        assertEquals("rp-civilian-0123abcd", IdentityAccountResolver.accountName(player, IdentityType.CIVILIAN, 8))
        assertEquals("rp-police-0123abcd", IdentityAccountResolver.accountName(player, IdentityType.POLICE, 8))
        assertEquals("rp-sar-0123abcd", IdentityAccountResolver.accountName(player, IdentityType.SAR, 8))

        for (type in IdentityType.entries) {
            assertEquals(
                IdentityAccountResolver.accountName(player, type, 8),
                IdentityAccountResolver.candidateNames(player, type).first()
            )
        }
    }

    /**
     * Verifies that successive candidate names grow by four hex digits and never exceed the maximum account name length.
     */
    @Test
    fun `candidate names grow by four digits and never exceed the maximum length`() {
        for (type in IdentityType.entries) {
            val names = IdentityAccountResolver.candidateNames(player, type)

            assertTrue(names.all { it.length <= Account.MAX_NAME_LENGTH })
            names.zipWithNext().forEach { (shorter, longer) ->
                assertEquals(shorter.length + 4, longer.length)
                assertTrue(longer.startsWith(shorter))
            }
        }

        assertEquals(
            listOf(
                "rp-civilian-0123abcd",
                "rp-civilian-0123abcd4567",
                "rp-civilian-0123abcd456789ef",
                "rp-civilian-0123abcd456789ef0123"
            ),
            IdentityAccountResolver.candidateNames(player, IdentityType.CIVILIAN)
        )
    }

    /**
     * Verifies that an existing account already owned by the player is reused without creating one.
     */
    @Test
    fun `an account the player already owns is reused`() = runBlocking {
        val ownId = UUID.randomUUID()
        val store = Store(createResult = { _, _ -> error("must not create") })
        store.accounts["rp-police-0123abcd"] = account("rp-police-0123abcd", player, ownId)

        val resolved = store.resolver().resolve(player, IdentityType.POLICE)

        assertEquals(ownId, resolved)
        assertTrue(store.createdNames.isEmpty())
    }

    /**
     * Verifies that a candidate name owned by another player makes the resolver try a longer prefix.
     */
    @Test
    fun `a foreign-owned name lengthens the prefix`() = runBlocking {
        val createdId = UUID.randomUUID()
        val store = Store(createResult = { owner, name -> AccountCreationResult.Success(account(name, owner, createdId)) })
        store.accounts["rp-sar-0123abcd"] = account("rp-sar-0123abcd", otherPlayer)

        val resolved = store.resolver().resolve(player, IdentityType.SAR)

        assertEquals(createdId, resolved)
        assertEquals(listOf("rp-sar-0123abcd4567"), store.createdNames)
    }

    /**
     * Verifies that a candidate name with no existing account is created for the player.
     */
    @Test
    fun `an absent name is created for the player`() = runBlocking {
        val createdId = UUID.randomUUID()
        var createdOwner: UUID? = null
        val store = Store(createResult = { owner, name ->
            createdOwner = owner
            AccountCreationResult.Success(account(name, owner, createdId))
        })

        val resolved = store.resolver().resolve(player, IdentityType.CIVILIAN)

        assertEquals(createdId, resolved)
        assertEquals(player, createdOwner)
        assertEquals(listOf("rp-civilian-0123abcd"), store.createdNames)
    }

    /**
     * Verifies that an account created concurrently by the same player is reused after the creation attempt fails.
     */
    @Test
    fun `a name taken concurrently by the player is reused`() = runBlocking {
        val ownId = UUID.randomUUID()
        lateinit var store: Store
        store = Store(createResult = { owner, name ->
            store.accounts[name] = account(name, owner, ownId)
            AccountCreationResult.Failed(FailureReason.NAME_ALREADY_EXISTS)
        })

        val resolved = store.resolver().resolve(player, IdentityType.POLICE)

        assertEquals(ownId, resolved)
        assertEquals(listOf("rp-police-0123abcd"), store.createdNames)
    }

    /**
     * Verifies that an account created concurrently by another player makes the resolver try a longer prefix.
     */
    @Test
    fun `a name taken concurrently by another player lengthens the prefix`() = runBlocking {
        val createdId = UUID.randomUUID()
        lateinit var store: Store
        store = Store(createResult = { owner, name ->
            if (name == "rp-police-0123abcd") {
                store.accounts[name] = account(name, otherPlayer)
                AccountCreationResult.Failed(FailureReason.NAME_ALREADY_EXISTS)
            } else {
                AccountCreationResult.Success(account(name, owner, createdId))
            }
        })

        val resolved = store.resolver().resolve(player, IdentityType.POLICE)

        assertEquals(createdId, resolved)
        assertEquals(listOf("rp-police-0123abcd", "rp-police-0123abcd4567"), store.createdNames)
    }

    /**
     * Verifies that resolution fails once every candidate name is owned by another player.
     */
    @Test
    fun `every candidate name owned by another player fails`() {
        val store = Store(createResult = { _, _ -> error("must not create") })
        for (name in IdentityAccountResolver.candidateNames(player, IdentityType.CIVILIAN)) {
            store.accounts[name] = account(name, otherPlayer)
        }

        assertFailsWith<IllegalStateException> {
            runBlocking { store.resolver().resolve(player, IdentityType.CIVILIAN) }
        }
        assertTrue(store.createdNames.isEmpty())
    }

    /**
     * Verifies that a creation failure unrelated to a taken name fails resolution with that reason.
     */
    @Test
    fun `a creation failure other than a taken name fails with the reason`() {
        val store = Store(createResult = { _, _ -> AccountCreationResult.Failed(FailureReason.NAME_TOO_LONG) })

        val exception = assertFailsWith<IllegalStateException> {
            runBlocking { store.resolver().resolve(player, IdentityType.SAR) }
        }

        assertTrue(exception.message.orEmpty().contains(FailureReason.NAME_TOO_LONG.name))
        assertEquals(listOf("rp-sar-0123abcd"), store.createdNames)
    }
}
