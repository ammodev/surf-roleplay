package dev.slne.surf.roleplay.api.common.transaction.exception

data class TransactionalDelegateNotSetException(
    val delegateName: String
) : IllegalStateException("Transactional delegate is not set for $delegateName")