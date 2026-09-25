@file:Suppress("NonExtendableApiUsage")

package dev.slne.surf.roleplay.api.common.identity

import dev.slne.surf.roleplay.api.common.license.License
import dev.slne.surf.roleplay.api.common.license.LicenseGrantResult
import dev.slne.surf.roleplay.api.common.license.revoke.LicenseRevokedReason
import dev.slne.surf.roleplay.api.common.license.user.UserLicense
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transactional.Transactional
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.math.BigDecimal
import java.util.*

/**
 * A single roleplay persona owned by a user.
 *
 * An identity holds its own transactional account, licenses and organisation
 * membership, independent of every other identity the same user may own.
 */
interface RoleplayIdentity : Transactional {
    /**
     * The unique identifier of this identity.
     */
    val uuid: UUID

    /**
     * The UUID of the player who owns this identity.
     */
    val userUuid: UUID

    /**
     * The organisation this identity belongs to.
     */
    val type: IdentityType

    /**
     * The identifier of the transaction account owned by this identity.
     */
    val accountId: UUID

    /**
     * The licenses currently or formerly held by this identity.
     */
    val licenses: ObjectSet<UserLicense>

    /**
     * Resolves the transaction account owned by this identity.
     *
     * @return the account identified by [accountId]
     */
    suspend fun account(): Account

    /**
     * Returns the balance of this identity's own account in [currency].
     *
     * @param currency the currency of the balance
     * @return the current balance of this identity's account
     */
    suspend fun balance(currency: Currency): BigDecimal = balance(account(), currency)

    /**
     * Deposits [amount] into this identity's own account, initiated by the owning player.
     *
     * @param amount the amount to deposit
     * @param currency the currency of [amount]
     * @return the result of the transaction
     */
    suspend fun deposit(amount: BigDecimal, currency: Currency): TransactionResult =
        deposit(account(), userUuid, amount, currency)

    /**
     * Withdraws [amount] from this identity's own account, initiated by the owning player.
     *
     * @param amount the amount to withdraw
     * @param currency the currency of [amount]
     * @return the result of the transaction
     */
    suspend fun withdraw(amount: BigDecimal, currency: Currency): TransactionResult =
        withdraw(account(), userUuid, amount, currency)

    /**
     * Transfers [amount] from this identity's own account to [receiver], initiated by the
     * owning player.
     *
     * @param amount the amount to transfer
     * @param currency the currency of [amount]
     * @param receiver the account receiving the funds
     * @return the result of the transaction
     */
    suspend fun transfer(amount: BigDecimal, currency: Currency, receiver: Account): TransactionResult =
        transfer(userUuid, account(), amount, currency, receiver)

    /**
     * Grants [license] to this identity.
     *
     * Unless [force] is `true`, the license's requirements are evaluated against this identity
     * first and the license is only granted if all of them are met. An identity that already
     * holds an unrevoked license with the same key is not granted it again.
     *
     * @param license the license to grant
     * @param grantedBy the UUID of the player granting the license, or `null` to record no
     *        granting player
     * @param force whether to grant the license without evaluating its requirements
     * @return [LicenseGrantResult.Granted] with the new license,
     *         [LicenseGrantResult.AlreadyOwned] if the license is already held, or
     *         [LicenseGrantResult.RequirementsNotMet] with the requirement breakdown
     */
    suspend fun grantLicense(
        license: License,
        grantedBy: UUID?,
        force: Boolean = false
    ): LicenseGrantResult

    /**
     * Revokes the unrevoked license with the key of [license] held by this identity.
     *
     * The revoked license stays in [licenses] with its revocation details recorded.
     *
     * @param license the license to revoke
     * @param revokedBy the UUID of the player revoking the license
     * @param reason why the license is revoked
     * @return `true` if a license was revoked, `false` if this identity holds no unrevoked
     *         license with the key of [license]
     */
    suspend fun revokeLicense(
        license: License,
        revokedBy: UUID,
        reason: LicenseRevokedReason
    ): Boolean

    /**
     * Checks whether this identity currently holds [license].
     *
     * A revoked license is never considered held, even if it was granted at some point.
     */
    fun hasLicense(license: License): Boolean = licenses.any { userLicense ->
        !userLicense.isRevoked && userLicense.licenseKey == license.key
    }
}
