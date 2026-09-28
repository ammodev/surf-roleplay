package dev.slne.surf.roleplay.paper.screen

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [RejectionLog].
 */
class RejectionLogTest {

    /**
     * The current time of the fake clock, in milliseconds.
     */
    private var now = 0L

    /**
     * A log that allows two warnings per player per minute on the fake clock.
     */
    private val log = RejectionLog(2) { now }

    /**
     * A player id.
     */
    private val player = UUID.randomUUID()

    /**
     * Verifies that warnings beyond the budget are suppressed once with a notice, and silenced
     * after that.
     */
    @Test
    fun `warnings beyond the budget are suppressed`() {
        val decisions = List(5) { log.decide(player) }

        assertEquals(
            listOf(RejectionLog.Decision.LOG, RejectionLog.Decision.LOG, RejectionLog.Decision.SUPPRESS_NOTICE, RejectionLog.Decision.SILENT, RejectionLog.Decision.SILENT),
            decisions,
        )
    }

    /**
     * Verifies that the budget renews after a minute and that forgetting a player resets it.
     */
    @Test
    fun `the budget renews`() {
        repeat(3) { log.decide(player) }

        now = 60_000
        assertEquals(RejectionLog.Decision.LOG, log.decide(player))
        repeat(3) { log.decide(player) }
        log.forget(player)
        assertEquals(RejectionLog.Decision.LOG, log.decide(player))
    }
}
