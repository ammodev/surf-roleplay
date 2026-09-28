package dev.slne.surf.roleplay.fabric.server

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [RoleplayServerState].
 */
class RoleplayServerStateTest {

    /**
     * Verifies that a new state is inactive.
     */
    @Test
    fun `starts inactive`() {
        assertFalse(RoleplayServerState().isActive)
    }

    /**
     * Verifies that activating turns the state active and notifies listeners once, even when
     * activated twice.
     */
    @Test
    fun `activate notifies listeners once`() {
        val state = RoleplayServerState()
        val changes = mutableListOf<Boolean>()
        state.onChange { changes += it }

        state.activate()
        state.activate()

        assertTrue(state.isActive)
        assertEquals(listOf(true), changes)
    }

    /**
     * Verifies that resetting an active state turns it inactive and notifies listeners, while
     * resetting an inactive state notifies nobody.
     */
    @Test
    fun `reset notifies only when active`() {
        val state = RoleplayServerState()
        val changes = mutableListOf<Boolean>()
        state.onChange { changes += it }

        state.reset()
        state.activate()
        state.reset()

        assertFalse(state.isActive)
        assertEquals(listOf(true, false), changes)
    }
}
