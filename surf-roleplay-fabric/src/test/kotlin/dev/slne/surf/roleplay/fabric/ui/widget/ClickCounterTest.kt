package dev.slne.surf.roleplay.fabric.ui.widget

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the multi-click detection of [ClickCounter].
 */
class ClickCounterTest {

    /**
     * Verifies that quick clicks at the same spot count up to three and a fourth starts over.
     */
    @Test
    fun `quick clicks count to three and then start over`() {
        val counter = ClickCounter()

        assertEquals(1, counter.click(10.0, 10.0, 1_000))
        assertEquals(2, counter.click(10.0, 10.0, 1_200))
        assertEquals(3, counter.click(11.0, 9.0, 1_400))
        assertEquals(1, counter.click(10.0, 10.0, 1_600))
        assertEquals(2, counter.click(10.0, 10.0, 1_700))
    }

    /**
     * Verifies that a click after the interval starts a new count.
     */
    @Test
    fun `a slow click starts over`() {
        val counter = ClickCounter()

        counter.click(10.0, 10.0, 1_000)
        assertEquals(2, counter.click(10.0, 10.0, 1_500))
        assertEquals(1, counter.click(10.0, 10.0, 2_001))
    }

    /**
     * Verifies that a click away from the previous one starts a new count.
     */
    @Test
    fun `a distant click starts over`() {
        val counter = ClickCounter()

        counter.click(10.0, 10.0, 1_000)
        assertEquals(1, counter.click(20.0, 10.0, 1_100))
        assertEquals(2, counter.click(22.0, 12.0, 1_200))
    }

    /**
     * Verifies that a reset makes the next click a single click.
     */
    @Test
    fun `reset starts over`() {
        val counter = ClickCounter()

        counter.click(10.0, 10.0, 1_000)
        counter.reset()
        assertEquals(1, counter.click(10.0, 10.0, 1_100))
    }
}
