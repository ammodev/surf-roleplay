package dev.slne.surf.roleplay.core.common.identity.account

import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import java.util.*

/**
 * Finds or creates the transaction account a new identity of a player will own.
 *
 * Account names have the form `rp-<type>-<prefix>`, where `<type>` is the lower-case name of the
 * [IdentityType] and `<prefix>` is taken from the start of the player UUID's hex digits without
 * dashes. The prefix starts with [INITIAL_PREFIX_LENGTH] digits. An account with the candidate
 * name that the player owns is reused; one owned by someone else makes the prefix grow by
 * [PREFIX_LENGTH_STEP] digits. When no account has the candidate name, it is created for the
 * player.
 *
 * @param findByName looks up the account with the given name, returning `null` if none exists
 * @param createAccount creates an account with the given name owned by the given player
 */
class IdentityAccountResolver(
    private val findByName: suspend (name: String) -> Account?,
    private val createAccount: suspend (owner: UUID, name: String) -> AccountCreationResult
) {

    /**
     * Returns the identifier of the account an identity of [type] owned by [playerUuid] uses.
     *
     * @param playerUuid the UUID of the player who owns the identity
     * @param type the organisation the identity belongs to
     * @return the identifier of the reused or newly created account
     * @throws IllegalStateException if every candidate name within
     *         [Account.MAX_NAME_LENGTH] belongs to another player, or if the account cannot be
     *         created for a reason other than its name being taken
     */
    suspend fun resolve(playerUuid: UUID, type: IdentityType): UUID {
        for (name in candidateNames(playerUuid, type)) {
            val existing = findByName(name)
            if (existing != null) {
                if (existing.ownerUuid == playerUuid) return existing.accountId
                continue
            }

            when (val result = createAccount(playerUuid, name)) {
                is AccountCreationResult.Success -> return result.account.accountId
                is AccountCreationResult.Failed -> {
                    if (result.reason != AccountCreationResult.FailureReason.NAME_ALREADY_EXISTS) {
                        throw IllegalStateException(
                            "Could not create account '$name' for player $playerUuid: ${result.reason}"
                        )
                    }

                    val concurrent = findByName(name)
                    if (concurrent != null && concurrent.ownerUuid == playerUuid) {
                        return concurrent.accountId
                    }
                }
            }
        }

        throw IllegalStateException(
            "Every account name for a ${type.name} identity of player $playerUuid is taken"
        )
    }

    /**
     * Provides the account naming scheme and the resolver backed by the transaction system.
     */
    companion object {
        /**
         * The number of hex digits of the player UUID the first candidate name uses.
         */
        const val INITIAL_PREFIX_LENGTH = 8

        /**
         * The number of hex digits the prefix grows by for each further candidate name.
         */
        const val PREFIX_LENGTH_STEP = 4

        /**
         * Builds the account name for an identity of [type] owned by [playerUuid], using the
         * first [prefixLength] hex digits of the player UUID.
         *
         * @param playerUuid the UUID of the player who owns the identity
         * @param type the organisation the identity belongs to
         * @param prefixLength the number of hex digits of the player UUID to use
         * @return the account name `rp-<type>-<prefix>`
         */
        fun accountName(playerUuid: UUID, type: IdentityType, prefixLength: Int): String =
            namePrefix(type) + hexDigits(playerUuid).take(prefixLength)

        /**
         * Lists every candidate account name for an identity of [type] owned by [playerUuid], in
         * the order they are tried.
         *
         * Each name is [PREFIX_LENGTH_STEP] hex digits longer than the previous one, and no name
         * is longer than [Account.MAX_NAME_LENGTH] or uses more hex digits than the UUID has.
         *
         * @param playerUuid the UUID of the player who owns the identity
         * @param type the organisation the identity belongs to
         * @return the candidate names, shortest first
         */
        fun candidateNames(playerUuid: UUID, type: IdentityType): List<String> {
            val digits = hexDigits(playerUuid)
            val maxPrefixLength = minOf(digits.length, Account.MAX_NAME_LENGTH - namePrefix(type).length)

            return (INITIAL_PREFIX_LENGTH..maxPrefixLength step PREFIX_LENGTH_STEP)
                .map { accountName(playerUuid, type, it) }
        }

        /**
         * Creates a resolver that looks up and creates accounts through the transaction system.
         *
         * @return the resolver used by in-memory users outside of tests
         */
        fun transactionBacked(): IdentityAccountResolver = IdentityAccountResolver(
            findByName = { name -> Account.byName(name) },
            createAccount = { owner, name -> Account.create(owner, name) }
        )

        /**
         * Returns the fixed start `rp-<type>-` of every account name for an identity of [type].
         */
        private fun namePrefix(type: IdentityType): String = "rp-${type.name.lowercase()}-"

        /**
         * Returns the 32 hex digits of [uuid] without dashes.
         */
        private fun hexDigits(uuid: UUID): String = uuid.toString().replace("-", "")
    }
}
