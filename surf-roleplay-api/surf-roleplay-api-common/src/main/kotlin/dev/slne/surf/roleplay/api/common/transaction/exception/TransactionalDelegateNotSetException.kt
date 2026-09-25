package dev.slne.surf.roleplay.api.common.transaction.exception

/**
 * Thrown when a transactional operation is invoked before a delegate has been set.
 *
 * @property delegateName the name of the transactional with no delegate set
 */
data class TransactionalDelegateNotSetException(
    val delegateName: String
) : IllegalStateException("Transactional delegate is not set for $delegateName")
