package dev.slne.surf.roleplay.fabric.settings

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the conflict detection of key bindings.
 */
class KeyBindingsTest {

    /**
     * Verifies that bindings on the same key conflict and a binding on its own key does not.
     */
    @Test
    fun `bindings on the same key conflict`() =
        assertEquals(setOf("a", "b"), KeyBindings.conflicts(listOf("a" to "key.keyboard.r", "b" to "key.keyboard.r", "c" to "key.keyboard.t")))

    /**
     * Verifies that unbound bindings never conflict with each other.
     */
    @Test
    fun `unbound bindings never conflict`() =
        assertEquals(emptySet<String>(), KeyBindings.conflicts(listOf("a" to "key.keyboard.unknown", "b" to "key.keyboard.unknown")))
}
