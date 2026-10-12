package dev.slne.surf.roleplay.fabric.interaction

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the timeout of an unanswered radial request.
 */
class RequestTimeoutTest {

    /**
     * Verifies that a request expires exactly when the timeout has passed.
     */
    @Test
    fun `expires after the timeout`() {
        assertEquals(false, RequestTimeout.expired(sentAt = 1_000, now = 1_000))
        assertEquals(false, RequestTimeout.expired(sentAt = 1_000, now = 2_999))
        assertEquals(true, RequestTimeout.expired(sentAt = 1_000, now = 3_000))
    }
}
