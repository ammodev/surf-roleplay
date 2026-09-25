@file:Suppress("NonExtendableApiUsage")

package dev.slne.surf.roleplay.core.common.identity

import dev.slne.surf.api.core.util.freeze
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import dev.slne.surf.roleplay.api.common.license.License
import dev.slne.surf.roleplay.api.common.license.LicenseGrantResult
import dev.slne.surf.roleplay.api.common.license.revoke.LicenseRevokedReason
import dev.slne.surf.roleplay.api.common.license.user.UserLicense
import dev.slne.surf.roleplay.core.common.user.CoreRoleplayUser
import dev.slne.surf.roleplay.core.common.user.rpc.RoleplayIdentityDto
import dev.slne.surf.roleplay.core.common.user.rpc.UserService
import dev.slne.surf.roleplay.core.common.user.rpc.userLicenses
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.PendingTransactionResult
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.transactional.PendingExecutionDecision
import dev.slne.surf.transaction.api.transactional.PendingExecutionResult
import dev.slne.surf.transaction.api.transactional.PendingRollbackPolicy
import dev.slne.surf.transaction.api.user.TransactionUser
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.math.BigDecimal
import java.util.*
import kotlin.time.Duration

/**
 * The shared state of every roleplay identity held in memory.
 *
 * Holds the identifiers, the owned account's identifier and the licenses of an identity. License
 * changes are sent to the remote user service through [owner], which applies the returned state
 * to this identity. Every transactional operation is carried out by the transaction user of the
 * identity's owner.
 *
 * @param uuid the unique identifier of this identity
 * @param owner the in-memory user who owns this identity
 * @param accountId the identifier of the transaction account owned by this identity
 * @param licenses the licenses currently or formerly held by this identity
 */
abstract class CoreRoleplayIdentity(
    override val uuid: UUID,
    val owner: CoreRoleplayUser,
    override val accountId: UUID,
    licenses: Collection<UserLicense>
) : RoleplayIdentity {
    @Volatile
    private var _licenses: ObjectSet<UserLicense> = mutableObjectSetOf(licenses)

    /**
     * The UUID of the player who owns this identity.
     */
    override val userUuid: UUID get() = owner.uuid

    /**
     * The remote user service write operations of this identity are sent to.
     */
    protected val service: UserService get() = owner.service

    /**
     * A read-only view of the licenses currently or formerly held by this identity.
     */
    override val licenses: ObjectSet<UserLicense> get() = _licenses.freeze()

    /**
     * Replaces the licenses and every organisation-specific value of this identity with the
     * state described by [dto].
     *
     * @param dto the state of this identity as returned by the remote user service
     */
    internal open fun applyState(dto: RoleplayIdentityDto) {
        _licenses = mutableObjectSetOf(dto.userLicenses())
    }

    /**
     * Resolves the transaction account identified by [accountId] through the transaction system.
     *
     * @return the account owned by this identity
     * @throws IllegalStateException if no account with [accountId] exists
     */
    override suspend fun account(): Account = Account.byId(accountId)
        ?: throw IllegalStateException("Account $accountId of identity $uuid does not exist")

    /**
     * Grants [license] to this identity through the remote user service.
     *
     * Nothing is sent if this identity already holds an unrevoked license with the key of
     * [license], or if [force] is `false` and any requirement of [license] is not met by this
     * identity.
     *
     * @param license the license to grant
     * @param grantedBy the UUID of the player granting the license, or `null` to record no
     *        granting player
     * @param force whether to grant the license without evaluating its requirements
     * @return [LicenseGrantResult.Granted] with the unrevoked license held afterwards,
     *         [LicenseGrantResult.AlreadyOwned] if the license was already held, or
     *         [LicenseGrantResult.RequirementsNotMet] with the requirement breakdown
     * @throws IllegalStateException if the returned state holds no unrevoked license with the key
     *         of [license]
     */
    override suspend fun grantLicense(
        license: License,
        grantedBy: UUID?,
        force: Boolean
    ): LicenseGrantResult = owner.write {
        if (hasLicense(license)) return@write LicenseGrantResult.AlreadyOwned

        if (!force) {
            val calculation = license.calculateRequirements(this)
            if (!calculation.isMet) return@write LicenseGrantResult.RequirementsNotMet(calculation)
        }

        owner.applyState(service.grantLicense(userUuid, uuid, license.key.asString(), grantedBy))

        val granted = _licenses.firstOrNull { !it.isRevoked && it.licenseKey == license.key }
            ?: throw IllegalStateException(
                "License ${license.key.asString()} is not held by identity $uuid after granting it"
            )
        LicenseGrantResult.Granted(granted)
    }

    /**
     * Revokes the unrevoked license with the key of [license] through the remote user service.
     *
     * Nothing is sent if this identity holds no unrevoked license with that key.
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
    ): Boolean = owner.write {
        if (!hasLicense(license)) return@write false

        owner.applyState(
            service.revokeLicense(userUuid, uuid, license.key.asString(), revokedBy, reason.name)
        )
        true
    }

    /**
     * Returns the transaction user of the player who owns this identity, which carries out every
     * transactional operation of this identity.
     */
    private fun transactionUser(): TransactionUser = TransactionUser.byUuid(userUuid)

    /**
     * Returns the balance of [account] in [currency], as reported by the owner's transaction user.
     */
    override suspend fun balance(account: Account, currency: Currency): BigDecimal =
        transactionUser().balance(account, currency)

    /**
     * Creates a pending deposit of [amount] into [account] through the owner's transaction user.
     */
    override suspend fun beginDeposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): PendingTransactionResult = transactionUser().beginDeposit(
        account, initiator, amount, currency, timeout, ignoreMinimum, *additionalData
    )

    /**
     * Creates a pending withdrawal of [amount] from [account] through the owner's transaction
     * user.
     */
    override suspend fun beginWithdrawal(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): PendingTransactionResult = transactionUser().beginWithdrawal(
        account, initiator, amount, currency, timeout, ignoreMinimum, *additionalData
    )

    /**
     * Creates a pending transfer of [amount] from [sender] to [receiver] through the owner's
     * transaction user.
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
    ): PendingTransactionResult = transactionUser().beginTransfer(
        initiator,
        sender,
        amount,
        currency,
        receiver,
        timeout,
        ignoreSenderMinimum,
        ignoreReceiverMinimum,
        additionalSenderData,
        additionalReceiverData
    )

    /**
     * Reserves a deposit into [account], runs [block] and commits the deposit through the
     * owner's transaction user.
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
    ): PendingExecutionResult<T> = transactionUser().withPendingDeposit(
        account, initiator, amount, currency, timeout, ignoreMinimum, additionalData, rollbackOn, block
    )

    /**
     * Reserves a withdrawal from [account], runs [block] and commits the withdrawal through the
     * owner's transaction user.
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
    ): PendingExecutionResult<T> = transactionUser().withPendingWithdrawal(
        account, initiator, amount, currency, timeout, ignoreMinimum, additionalData, rollbackOn, block
    )

    /**
     * Reserves a transfer from [sender] to [receiver], runs [block] and commits the transfer
     * through the owner's transaction user.
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
    ): PendingExecutionResult<T> = transactionUser().withPendingTransfer(
        initiator,
        sender,
        amount,
        currency,
        receiver,
        timeout,
        ignoreSenderMinimum,
        ignoreReceiverMinimum,
        additionalSenderData,
        additionalReceiverData,
        rollbackOn,
        block
    )

    /**
     * Reserves a deposit into [account] and commits or rolls it back as decided by [block],
     * through the owner's transaction user.
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
    ): PendingExecutionResult<T> = transactionUser().withPendingDepositDecision(
        account, initiator, amount, currency, timeout, ignoreMinimum, additionalData, rollbackOn, block
    )

    /**
     * Reserves a withdrawal from [account] and commits or rolls it back as decided by [block],
     * through the owner's transaction user.
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
    ): PendingExecutionResult<T> = transactionUser().withPendingWithdrawalDecision(
        account, initiator, amount, currency, timeout, ignoreMinimum, additionalData, rollbackOn, block
    )

    /**
     * Reserves a transfer from [sender] to [receiver] and commits or rolls it back as decided by
     * [block], through the owner's transaction user.
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
    ): PendingExecutionResult<T> = transactionUser().withPendingTransferDecision(
        initiator,
        sender,
        amount,
        currency,
        receiver,
        timeout,
        ignoreSenderMinimum,
        ignoreReceiverMinimum,
        additionalSenderData,
        additionalReceiverData,
        rollbackOn,
        block
    )

    /**
     * Deposits [amount] into [account] through the owner's transaction user.
     */
    override suspend fun deposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult = transactionUser().deposit(
        account, initiator, amount, currency, ignoreMinimum, *additionalData
    )

    /**
     * Withdraws [amount] from [account] through the owner's transaction user.
     */
    override suspend fun withdraw(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult = transactionUser().withdraw(
        account, initiator, amount, currency, ignoreMinimum, *additionalData
    )

    /**
     * Transfers [amount] from [sender] to [receiver] through the owner's transaction user.
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
    ): TransactionResult = transactionUser().transfer(
        initiator,
        sender,
        amount,
        currency,
        receiver,
        ignoreSenderMinimum,
        ignoreReceiverMinimum,
        additionalSenderData,
        additionalReceiverData
    )
}
