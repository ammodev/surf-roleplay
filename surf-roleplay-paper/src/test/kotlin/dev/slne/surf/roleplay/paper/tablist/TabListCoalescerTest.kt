package dev.slne.surf.roleplay.paper.tablist

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the per-player coalescing of tab list pushes.
 */
class TabListCoalescerTest {

    /** The current fake time in milliseconds. */
    private var now = 0L

    /** The coalescer under test, reading the fake time. */
    private val coalescer = TabListCoalescer(windowMillis = 2000) { now }

    /** A player. */
    private val player = UUID.randomUUID()

    /** Another player. */
    private val other = UUID.randomUUID()

    /**
     * Verifies that a change of a player who was never pushed is due at once.
     */
    @Test
    fun `first change is due at once`() {
        coalescer.markDirty(player)
        assertEquals(setOf(player), coalescer.due())
        assertTrue(coalescer.due().isEmpty())
    }

    /**
     * Verifies that two changes within the window produce exactly one push.
     */
    @Test
    fun `two changes within the window produce one push`() {
        coalescer.markDirty(player)
        assertEquals(setOf(player), coalescer.due())

        now = 500
        coalescer.markDirty(player)
        now = 1000
        coalescer.markDirty(player)
        assertTrue(coalescer.due().isEmpty())

        now = 2000
        assertEquals(setOf(player), coalescer.due())
        now = 2100
        assertTrue(coalescer.due().isEmpty())
    }

    /**
     * Verifies that a change after the window is pushed again and is not lost.
     */
    @Test
    fun `change after the window pushes again`() {
        coalescer.markDirty(player)
        coalescer.due()

        now = 5000
        coalescer.markDirty(player)
        assertEquals(setOf(player), coalescer.due())
    }

    /**
     * Verifies that a change made within the window right after a push is kept until the window
     * ends.
     */
    @Test
    fun `change inside the window is kept until the window ends`() {
        coalescer.markDirty(player)
        coalescer.due()

        now = 1999
        coalescer.markDirty(player)
        assertTrue(coalescer.due().isEmpty())
        now = 3999
        assertEquals(setOf(player), coalescer.due())
    }

    /**
     * Verifies that an immediate push starts the window and clears a pending change.
     */
    @Test
    fun `immediate push starts the window`() {
        coalescer.markDirty(player)
        coalescer.pushed(player)
        assertTrue(coalescer.due().isEmpty())

        now = 1000
        coalescer.markDirty(player)
        assertTrue(coalescer.due().isEmpty())
        now = 2000
        assertEquals(setOf(player), coalescer.due())
    }

    /**
     * Verifies that with flushes every 250 ms on a jittered clock and a change before every flush,
     * pushes come every 2 s within the jitter, never a flush period late.
     */
    @Test
    fun `jittered flushes push every window`() {
        val jittered = TabListCoalescer(windowMillis = 2000, toleranceMillis = 250) { now }
        val jitter = longArrayOf(0, 60, -60, 35, -45, 10, -20, 55, -55, 25)
        val pushes = mutableListOf<Long>()

        for (flush in 0 until 400) {
            now = flush * 250L + jitter[flush % jitter.size]
            jittered.markDirty(player)
            if (player in jittered.due()) pushes += now
        }

        val intervals = pushes.zipWithNext { a, b -> b - a }
        assertTrue(intervals.size > 40)
        assertTrue(intervals.all { it in 1750..2250 }, "intervals: $intervals")
    }

    /**
     * Verifies that the tolerance does not allow a push earlier than the window minus the
     * tolerance.
     */
    @Test
    fun `tolerance bounds the earliest push`() {
        val tolerant = TabListCoalescer(windowMillis = 2000, toleranceMillis = 250) { now }
        tolerant.markDirty(player)
        tolerant.due()

        now = 1749
        tolerant.markDirty(player)
        assertTrue(tolerant.due().isEmpty())
        now = 1750
        assertEquals(setOf(player), tolerant.due())
    }

    /**
     * Verifies that players are coalesced independently.
     */
    @Test
    fun `players are independent`() {
        coalescer.markDirty(player)
        coalescer.due()

        now = 100
        coalescer.markDirty(player)
        coalescer.markDirty(other)
        assertEquals(setOf(other), coalescer.due())
    }

    /**
     * Verifies that a player who quit is forgotten: the pending change is dropped and the window
     * does not apply to a later session.
     */
    @Test
    fun `quit removes the state`() {
        coalescer.markDirty(player)
        coalescer.due()
        now = 100
        coalescer.markDirty(player)

        coalescer.remove(player)
        assertTrue(coalescer.due().isEmpty())

        now = 200
        coalescer.markDirty(player)
        assertEquals(setOf(player), coalescer.due())
    }
}
