@file:Suppress("NonExtendableApiUsage")

package dev.slne.surf.roleplay.api.common.transaction

import dev.slne.surf.roleplay.api.common.transaction.exception.TransactionalDelegateNotSetException
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.PendingTransactionResult
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.transactional.PendingExecutionDecision
import dev.slne.surf.transaction.api.transactional.PendingExecutionResult
import dev.slne.surf.transaction.api.transactional.PendingRollbackPolicy
import dev.slne.surf.transaction.api.transactional.Transactional
import java.math.BigDecimal
import java.util.*
import kotlin.time.Duration

/**
 * A [Transactional] that forwards every operation to a swappable [delegate].
 *
 * Calling an operation before a delegate is set throws [TransactionalDelegateNotSetException].
 */
interface RoleplayTransactional : Transactional {
    /**
     * The name this transactional is identified by in error messages.
     */
    val delegateName: String

    /**
     * The transactional every operation is forwarded to, or `null` if none is set.
     */
    val delegate: Transactional?

    /**
     * Delegates to [delegate], throwing [TransactionalDelegateNotSetException] if none is set.
     */
    override suspend fun balance(
        account: Account,
        currency: Currency
    ): BigDecimal {
        return delegate?.balance(account, currency)
            ?: throw TransactionalDelegateNotSetException(delegateName)
    }

    /**
     * Delegates to [delegate], throwing [TransactionalDelegateNotSetException] if none is set.
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
        return delegate?.beginDeposit(
            account,
            initiator,
            amount,
            currency,
            timeout,
            ignoreMinimum,
            *additionalData
        ) ?: throw TransactionalDelegateNotSetException(delegateName)
    }

    /**
     * Delegates to [delegate], throwing [TransactionalDelegateNotSetException] if none is set.
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
        return delegate?.beginTransfer(
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
        ) ?: throw TransactionalDelegateNotSetException(delegateName)
    }

    /**
     * Delegates to [delegate], throwing [TransactionalDelegateNotSetException] if none is set.
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
        return delegate?.beginWithdrawal(
            account,
            initiator,
            amount,
            currency,
            timeout,
            ignoreMinimum,
            *additionalData
        ) ?: throw TransactionalDelegateNotSetException(delegateName)
    }

    /**
     * Delegates to [delegate], throwing [TransactionalDelegateNotSetException] if none is set.
     */
    override suspend fun deposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        return delegate?.deposit(
            account,
            initiator,
            amount,
            currency,
            ignoreMinimum,
            *additionalData
        ) ?: throw TransactionalDelegateNotSetException(delegateName)
    }

    /**
     * Delegates to [delegate], throwing [TransactionalDelegateNotSetException] if none is set.
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
        return delegate?.transfer(
            initiator,
            sender,
            amount,
            currency,
            receiver,
            ignoreSenderMinimum,
            ignoreReceiverMinimum,
            additionalSenderData,
            additionalReceiverData
        ) ?: throw TransactionalDelegateNotSetException(delegateName)
    }

    /**
     * Delegates to [delegate], throwing [TransactionalDelegateNotSetException] if none is set.
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
        return delegate?.withPendingDeposit(
            account,
            initiator,
            amount,
            currency,
            timeout,
            ignoreMinimum,
            additionalData,
            rollbackOn,
            block
        ) ?: throw TransactionalDelegateNotSetException(delegateName)
    }

    /**
     * Delegates to [delegate], throwing [TransactionalDelegateNotSetException] if none is set.
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
        return delegate?.withPendingDepositDecision(
            account,
            initiator,
            amount,
            currency,
            timeout,
            ignoreMinimum,
            additionalData,
            rollbackOn,
            block
        ) ?: throw TransactionalDelegateNotSetException(delegateName)
    }

    /**
     * Delegates to [delegate], throwing [TransactionalDelegateNotSetException] if none is set.
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
        return delegate?.withPendingTransfer(
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
        ) ?: throw TransactionalDelegateNotSetException(delegateName)
    }

    /**
     * Delegates to [delegate], throwing [TransactionalDelegateNotSetException] if none is set.
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
        return delegate?.withPendingTransferDecision(
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
        ) ?: throw TransactionalDelegateNotSetException(delegateName)
    }

    /**
     * Delegates to [delegate], throwing [TransactionalDelegateNotSetException] if none is set.
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
        return delegate?.withPendingWithdrawal(
            account,
            initiator,
            amount,
            currency,
            timeout,
            ignoreMinimum,
            additionalData,
            rollbackOn,
            block
        ) ?: throw TransactionalDelegateNotSetException(delegateName)
    }

    /**
     * Delegates to [delegate], throwing [TransactionalDelegateNotSetException] if none is set.
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
        return delegate?.withPendingWithdrawalDecision(
            account,
            initiator,
            amount,
            currency,
            timeout,
            ignoreMinimum,
            additionalData,
            rollbackOn,
            block
        ) ?: throw TransactionalDelegateNotSetException(delegateName)
    }

    /**
     * Delegates to [delegate], throwing [TransactionalDelegateNotSetException] if none is set.
     */
    override suspend fun withdraw(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        return delegate?.withdraw(
            account,
            initiator,
            amount,
            currency,
            ignoreMinimum,
            *additionalData
        ) ?: throw TransactionalDelegateNotSetException(delegateName)
    }
}
