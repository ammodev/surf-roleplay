@file:Suppress("NonExtendableApiUsage")

package dev.slne.surf.roleplay.core.common.identity

import dev.slne.surf.api.core.util.freeze
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import dev.slne.surf.roleplay.api.common.license.License
import dev.slne.surf.roleplay.api.common.license.LicenseGrantResult
import dev.slne.surf.roleplay.api.common.license.revoke.LicenseRevokedReason
import dev.slne.surf.roleplay.api.common.license.user.UserLicense
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.PendingTransactionResult
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.transactional.PendingExecutionDecision
import dev.slne.surf.transaction.api.transactional.PendingExecutionResult
import dev.slne.surf.transaction.api.transactional.PendingRollbackPolicy
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.math.BigDecimal
import java.util.*
import kotlin.time.Duration

/**
 * The shared state of every roleplay identity held in memory.
 *
 * Holds the identifiers, the owned account's identifier and the licenses of an identity, and
 * carries out every transactional operation against the transaction system on behalf of the
 * identity's owner.
 *
 * @param uuid the unique identifier of this identity
 * @param userUuid the UUID of the player who owns this identity
 * @param accountId the identifier of the transaction account owned by this identity
 * @param licenses the licenses currently or formerly held by this identity
 * @param service the remote user service write operations are sent to
 */
abstract class CoreRoleplayIdentity(
    override val uuid: UUID,
    override val userUuid: UUID,
    override val accountId: UUID,
    licenses: Collection<UserLicense>,
    protected val service: UserService
) : RoleplayIdentity {
    private val _licenses: ObjectSet<UserLicense> = mutableObjectSetOf(licenses)

    /**
     * A read-only view of the licenses currently or formerly held by this identity.
     */
    override val licenses: ObjectSet<UserLicense> get() = _licenses.freeze()

    /**
     * Resolves the transaction account identified by [accountId] through the transaction system.
     *
     * @return the account owned by this identity
     */
    override suspend fun account(): Account {
        TODO()
    }

    /**
     * Grants [license] to this identity through the remote user service, evaluating the license's
     * requirements first unless [force] is `true`.
     *
     * @param license the license to grant
     * @param grantedBy the UUID of the player granting the license, or `null` to record no
     *        granting player
     * @param force whether to grant the license without evaluating its requirements
     * @return the outcome of the grant
     */
    override suspend fun grantLicense(
        license: License,
        grantedBy: UUID?,
        force: Boolean
    ): LicenseGrantResult {
        TODO()
    }

    /**
     * Revokes the unrevoked license with the key of [license] through the remote user service.
     *
     * @param license the license to revoke
     * @param revokedBy the UUID of the player revoking the license
     * @param reason why the license is revoked
     * @return `true` if a license was revoked, `false` if no unrevoked license with that key is held
     */
    override suspend fun revokeLicense(
        license: License,
        revokedBy: UUID,
        reason: LicenseRevokedReason
    ): Boolean {
        TODO()
    }

    /**
     * Returns the balance of [account] in [currency] as reported by the transaction system.
     */
    override suspend fun balance(account: Account, currency: Currency): BigDecimal {
        TODO()
    }

    /**
     * Creates a pending deposit of [amount] into [account] through the transaction system.
     */
    override suspend fun beginDeposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): PendingTransactionResult {
        TODO()
    }

    /**
     * Creates a pending withdrawal of [amount] from [account] through the transaction system.
     */
    override suspend fun beginWithdrawal(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): PendingTransactionResult {
        TODO()
    }

    /**
     * Creates a pending transfer of [amount] from [sender] to [receiver] through the transaction
     * system.
     */
    override suspend fun beginTransfer(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        timeout: Duration,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: Set<TransactionData>,
        additionalReceiverData: Set<TransactionData>
    ): PendingTransactionResult {
        TODO()
    }

    /**
     * Reserves a deposit into [account], runs [block] and commits the deposit through the
     * transaction system.
     */
    override suspend fun <T> withPendingDeposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        additionalData: Set<TransactionData>,
        rollbackOn: PendingRollbackPolicy,
        block: suspend (UUID) -> T
    ): PendingExecutionResult<T> {
        TODO()
    }

    /**
     * Reserves a withdrawal from [account], runs [block] and commits the withdrawal through the
     * transaction system.
     */
    override suspend fun <T> withPendingWithdrawal(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        additionalData: Set<TransactionData>,
        rollbackOn: PendingRollbackPolicy,
        block: suspend (UUID) -> T
    ): PendingExecutionResult<T> {
        TODO()
    }

    /**
     * Reserves a transfer from [sender] to [receiver], runs [block] and commits the transfer
     * through the transaction system.
     */
    override suspend fun <T> withPendingTransfer(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        timeout: Duration,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: Set<TransactionData>,
        additionalReceiverData: Set<TransactionData>,
        rollbackOn: PendingRollbackPolicy,
        block: suspend (UUID) -> T
    ): PendingExecutionResult<T> {
        TODO()
    }

    /**
     * Reserves a deposit into [account] and commits or rolls it back as decided by [block],
     * through the transaction system.
     */
    override suspend fun <T> withPendingDepositDecision(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        additionalData: Set<TransactionData>,
        rollbackOn: PendingRollbackPolicy,
        block: suspend (UUID) -> PendingExecutionDecision<T>
    ): PendingExecutionResult<T> {
        TODO()
    }

    /**
     * Reserves a withdrawal from [account] and commits or rolls it back as decided by [block],
     * through the transaction system.
     */
    override suspend fun <T> withPendingWithdrawalDecision(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        additionalData: Set<TransactionData>,
        rollbackOn: PendingRollbackPolicy,
        block: suspend (UUID) -> PendingExecutionDecision<T>
    ): PendingExecutionResult<T> {
        TODO()
    }

    /**
     * Reserves a transfer from [sender] to [receiver] and commits or rolls it back as decided by
     * [block], through the transaction system.
     */
    override suspend fun <T> withPendingTransferDecision(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        timeout: Duration,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: Set<TransactionData>,
        additionalReceiverData: Set<TransactionData>,
        rollbackOn: PendingRollbackPolicy,
        block: suspend (UUID) -> PendingExecutionDecision<T>
    ): PendingExecutionResult<T> {
        TODO()
    }

    /**
     * Deposits [amount] into [account] through the transaction system.
     */
    override suspend fun deposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        TODO()
    }

    /**
     * Withdraws [amount] from [account] through the transaction system.
     */
    override suspend fun withdraw(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        TODO()
    }

    /**
     * Transfers [amount] from [sender] to [receiver] through the transaction system.
     */
    override suspend fun transfer(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: Set<TransactionData>,
        additionalReceiverData: Set<TransactionData>
    ): TransactionResult {
        TODO()
    }
}
