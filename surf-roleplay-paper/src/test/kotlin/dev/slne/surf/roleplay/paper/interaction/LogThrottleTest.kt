package dev.slne.surf.roleplay.paper.interaction

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the per-key log throttle.
 */
class LogThrottleTest {

    /** The simulated time in milliseconds. */
    private var now = 1_000L

    /** The throttle under test, reading [now]. */
    private val throttle = LogThrottle(60_000) { now }

    /**
     * Verifies that a key is let through once per interval and independently of other keys.
     */
    @Test
    fun `lets a key through once per interval`() {
        assertEquals(true, throttle.shouldLog("a"))
        assertEquals(false, throttle.shouldLog("a"))
        assertEquals(true, throttle.shouldLog("b"))

        now += 59_999
        assertEquals(false, throttle.shouldLog("a"))

        now += 1
        assertEquals(true, throttle.shouldLog("a"))
    }
}
