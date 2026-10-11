package dev.slne.surf.roleplay.paper.interaction

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

/**
 * Tests for the per-player current offer.
 */
class OfferBookTest {

    /** The player the offers belong to. */
    private val player = UUID.randomUUID()

    /**
     * Verifies that the current offer can be claimed exactly once.
     */
    @Test
    fun `current offer is claimed once`() {
        val book = OfferBook<String>()
        val id = book.issue(player, "door")

        assertEquals(OfferBook.Claim.Accepted("door"), book.claim(player, id))
        assertIs<OfferBook.Claim.Rejected>(book.claim(player, id))
    }

    /**
     * Verifies that a new offer replaces the previous one, which can no longer be claimed.
     */
    @Test
    fun `old and unknown offers are rejected`() {
        val book = OfferBook<String>()
        val old = book.issue(player, "first")
        val current = book.issue(player, "second")

        assertNotEquals(old, current)
        assertIs<OfferBook.Claim.Rejected>(book.claim(player, old))
        assertIs<OfferBook.Claim.Rejected>(book.claim(player, 999_999))
        assertIs<OfferBook.Claim.Rejected>(book.claim(player, 0))
        assertEquals(OfferBook.Claim.Accepted("second"), book.claim(player, current))
    }

    /**
     * Verifies that offers are per player and are gone once cleared.
     */
    @Test
    fun `offers are per player and clearable`() {
        val book = OfferBook<String>()
        val other = UUID.randomUUID()
        val id = book.issue(player, "door")

        assertIs<OfferBook.Claim.Rejected>(book.claim(other, id))
        book.clear(player)
        assertIs<OfferBook.Claim.Rejected>(book.claim(player, id))
    }

    /**
     * Verifies that concurrent issues for one player leave exactly one claimable offer, whose id
     * was returned to one of the issuers.
     */
    @Test
    fun `concurrent issues leave one consistent offer`() {
        repeat(200) {
            val book = OfferBook<Int>()
            val threads = 8
            val ids = java.util.concurrent.ConcurrentHashMap<Int, Int>()
            val start = java.util.concurrent.CountDownLatch(1)
            val workers = (1..threads).map { n ->
                Thread {
                    start.await()
                    ids[book.issue(player, n)] = n
                }.also { it.start() }
            }
            start.countDown()
            workers.forEach { it.join() }

            val claimable = ids.keys.filter { id -> book.claim(player, id) is OfferBook.Claim.Accepted }
            assertEquals(1, claimable.size)
            assertEquals(threads, ids.size)
        }
    }
}
