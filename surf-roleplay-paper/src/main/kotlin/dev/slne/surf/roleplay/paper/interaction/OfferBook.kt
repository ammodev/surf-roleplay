package dev.slne.surf.roleplay.paper.interaction

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * The current offer of every player. A new offer replaces the previous one, and an offer can be
 * claimed once.
 *
 * @param T the payload kept with an offer
 */
class OfferBook<T> {

    /**
     * The result of claiming an offer.
     *
     * @param T the payload type
     */
    sealed interface Claim<out T> {
        /**
         * The offer was the player's current one and is now used up.
         *
         * @property payload the payload kept with the offer
         */
        data class Accepted<T>(val payload: T) : Claim<T>

        /**
         * The offer is not the player's current one.
         *
         * @property reason why, for the log
         */
        data class Rejected(val reason: String) : Claim<Nothing>
    }

    /**
     * An issued offer.
     *
     * @property id the offer id
     * @property payload the payload
     */
    private class Current<T>(val id: Int, val payload: T)

    /**
     * The current offer of every player who has one.
     */
    private val current = ConcurrentHashMap<UUID, Current<T>>()

    /**
     * The source of offer ids; ids are positive and never 0.
     */
    private val next = AtomicInteger(0)

    /**
     * Issues a new offer for a player, replacing their previous one.
     *
     * @param player the player's unique id
     * @param payload the payload to keep with the offer
     * @return the offer id, never 0
     */
    fun issue(player: UUID, payload: T): Int {
        var issued = 0
        current.compute(player) { _, _ ->
            issued = next.updateAndGet { if (it == Int.MAX_VALUE) 1 else it + 1 }
            Current(issued, payload)
        }
        return issued
    }

    /**
     * Claims a player's offer. Only the current offer can be claimed, and only once.
     *
     * @param player the player's unique id
     * @param offerId the claimed offer id
     * @return the payload, or the reason the claim was rejected
     */
    fun claim(player: UUID, offerId: Int): Claim<T> {
        val offer = current[player] ?: return Claim.Rejected("no current offer")
        if (offer.id != offerId) return Claim.Rejected("offer $offerId is not the current offer ${offer.id}")
        return if (current.remove(player, offer)) Claim.Accepted(offer.payload) else Claim.Rejected("offer $offerId was already used")
    }

    /**
     * Forgets a player's current offer.
     *
     * @param player the player's unique id
     */
    fun clear(player: UUID) {
        current.remove(player)
    }

    /**
     * Returns the id of the most recently issued offer of any player.
     *
     * @return the id, or 0 if no offer was issued yet
     */
    fun lastIssued(): Int = next.get()
}
