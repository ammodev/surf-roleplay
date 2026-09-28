package dev.slne.surf.roleplay.protocol.screen

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [ScreenStack].
 */
class ScreenStackTest {

    /**
     * Returns the session ids of the stack from bottom to top.
     *
     * @return the session ids
     */
    private fun ScreenStack<String>.ids() = entries.map { it.sessionId }

    /**
     * Verifies that opening without a parent replaces every open screen.
     */
    @Test
    fun `open without parent replaces the stack`() {
        val stack = ScreenStack<String>()
        stack.open(1, null, true, "a")
        stack.open(2, 1, true, "b")

        val removed = stack.open(3, null, true, "c")

        assertEquals(listOf(3), stack.ids())
        assertEquals(listOf(2, 1), removed.map { it.sessionId })
    }

    /**
     * Verifies that opening on a parent closes the screens above the parent and puts the new
     * screen on top of it.
     */
    @Test
    fun `open on a parent closes screens above the parent`() {
        val stack = ScreenStack<String>()
        stack.open(1, null, true, "a")
        stack.open(2, 1, true, "b")
        stack.open(3, 2, true, "c")

        val removed = stack.open(4, 1, false, "d")

        assertEquals(listOf(1, 4), stack.ids())
        assertEquals(listOf(3, 2), removed.map { it.sessionId })
        assertEquals(false, stack.top?.closable)
    }

    /**
     * Verifies that opening on an unknown parent replaces every open screen.
     */
    @Test
    fun `open on an unknown parent replaces the stack`() {
        val stack = ScreenStack<String>()
        stack.open(1, null, true, "a")

        stack.open(2, 99, true, "b")

        assertEquals(listOf(2), stack.ids())
    }

    /**
     * Verifies that closing a screen also closes every screen above it and restores its parent
     * as the top.
     */
    @Test
    fun `close removes the session and every screen above it`() {
        val stack = ScreenStack<String>()
        stack.open(1, null, true, "a")
        stack.open(2, 1, true, "b")
        stack.open(3, 2, true, "c")

        val removed = stack.close(2)

        assertEquals(listOf(1), stack.ids())
        assertEquals("a", stack.top?.content)
        assertEquals(listOf(3, 2), removed.map { it.sessionId })
    }

    /**
     * Verifies that closing an unknown session changes nothing.
     */
    @Test
    fun `close of an unknown session changes nothing`() {
        val stack = ScreenStack<String>()
        stack.open(1, null, true, "a")

        assertEquals(emptyList(), stack.close(5))
        assertEquals(listOf(1), stack.ids())
    }

    /**
     * Verifies that closing everything empties the stack.
     */
    @Test
    fun `close all empties the stack`() {
        val stack = ScreenStack<String>()
        stack.open(1, null, true, "a")
        stack.open(2, 1, true, "b")

        assertEquals(listOf(2, 1), stack.closeAll().map { it.sessionId })
        assertNull(stack.top)
    }

    /**
     * Verifies that opening a session id that is already open replaces it.
     */
    @Test
    fun `reopening a session id replaces it`() {
        val stack = ScreenStack<String>()
        stack.open(1, null, true, "a")
        stack.open(2, 1, true, "b")

        stack.open(2, 1, true, "c")

        assertEquals(listOf(1, 2), stack.ids())
        assertEquals("c", stack.find(2)?.content)
    }
}
