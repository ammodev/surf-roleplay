package dev.slne.surf.roleplay.core.common.user

import dev.slne.surf.api.core.util.freeze
import dev.slne.surf.api.core.util.mutableObjectListOf
import dev.slne.surf.roleplay.api.common.identity.IdentityType
import dev.slne.surf.roleplay.api.common.identity.RoleplayIdentity
import dev.slne.surf.roleplay.api.common.identity.exceptions.NoActiveIdentityException
import dev.slne.surf.roleplay.api.common.user.RoleplayUser
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.PendingTransactionResult
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.transactional.PendingExecutionDecision
import dev.slne.surf.transaction.api.transactional.PendingExecutionResult
import dev.slne.surf.transaction.api.transactional.PendingRollbackPolicy
import it.unimi.dsi.fastutil.objects.ObjectList
import java.math.BigDecimal
import java.util.*
import kotlin.time.Duration

class CoreRoleplayUser(
    override val uuid: UUID,
    identities: ObjectList<RoleplayIdentity>
) : RoleplayUser {
    private val _identities = mutableObjectListOf(identities)
    override val identities get() = _identities.freeze()

    private var _activeIdentity: RoleplayIdentity? = null
    override val activeIdentity get() = _activeIdentity

    /**
     * Creates a new identity of [type] for this user.
     *
     * Not yet functional; always throws [NotImplementedError].
     */
    override suspend fun createIdentity(type: IdentityType): RoleplayIdentity {
        TODO("Implement")
    }

    override suspend fun setActiveIdentity(identity: RoleplayIdentity) {
        _activeIdentity = identity

        TODO("Implement")
    }

    /**
     * Deactivates the active identity.
     *
     * Not yet functional; always throws [NotImplementedError].
     */
    override fun clearActiveIdentity() {
        TODO("Implement")
    }

    /**
     * Permanently deletes [identity] from this user.
     *
     * Not yet functional; always throws [NotImplementedError].
     */
    override suspend fun deleteIdentity(identity: RoleplayIdentity) {
        TODO("Implement")
    }

    override suspend fun balance(
        account: Account,
        currency: Currency
    ): BigDecimal {
        return activeIdentity?.balance(account, currency)
            ?: throw NoActiveIdentityException(uuid)
    }

    override suspend fun beginDeposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): PendingTransactionResult {
        return activeIdentity?.beginDeposit(
            account,
            initiator,
            amount,
            currency,
            timeout,
            ignoreMinimum,
            *additionalData
        ) ?: throw NoActiveIdentityException(uuid)
    }

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
        return activeIdentity?.beginTransfer(
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
        ) ?: throw NoActiveIdentityException(uuid)
    }

    override suspend fun beginWithdrawal(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): PendingTransactionResult {
        return activeIdentity?.beginWithdrawal(
            account,
            initiator,
            amount,
            currency,
            timeout,
            ignoreMinimum,
            *additionalData
        ) ?: throw NoActiveIdentityException(uuid)
    }

    override suspend fun deposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        TODO("Not yet implemented")
    }

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
        TODO("Not yet implemented")
    }

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
        TODO("Not yet implemented")
    }

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
        TODO("Not yet implemented")
    }

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
        TODO("Not yet implemented")
    }

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
        TODO("Not yet implemented")
    }

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
        TODO("Not yet implemented")
    }

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
        TODO("Not yet implemented")
    }

    override suspend fun withdraw(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        TODO("Not yet implemented")
    }
}