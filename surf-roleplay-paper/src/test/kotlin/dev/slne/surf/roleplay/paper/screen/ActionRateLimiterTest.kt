package dev.slne.surf.roleplay.paper.screen

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [ActionRateLimiter].
 */
class ActionRateLimiterTest {

    /**
     * The current time of the fake clock, in milliseconds.
     */
    private var now = 0L

    /**
     * A limiter of three actions per second on the fake clock.
     */
    private val limiter = ActionRateLimiter(3) { now }

    /**
     * A player id.
     */
    private val player = UUID.randomUUID()

    /**
     * Verifies that actions beyond the limit within one second are refused.
     */
    @Test
    fun `actions beyond the limit are refused`() {
        val results = List(4) { limiter.tryAcquire(player) }

        assertEquals(listOf(true, true, true, false), results)
    }

    /**
     * Verifies that actions older than a second no longer count.
     */
    @Test
    fun `old actions stop counting after a second`() {
        repeat(3) { limiter.tryAcquire(player) }

        now = 999
        assertFalse(limiter.tryAcquire(player))
        now = 1000
        assertTrue(limiter.tryAcquire(player))
    }

    /**
     * Verifies that each player has their own budget and that forgetting a player resets it.
     */
    @Test
    fun `players have separate budgets`() {
        val other = UUID.randomUUID()
        repeat(3) { limiter.tryAcquire(player) }

        assertTrue(limiter.tryAcquire(other))
        limiter.forget(player)
        assertTrue(limiter.tryAcquire(player))
    }
}
