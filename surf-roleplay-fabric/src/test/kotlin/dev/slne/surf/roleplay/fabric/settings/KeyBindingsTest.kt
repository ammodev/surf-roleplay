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
        assertEquals(setOf("a", "b"), KeyBindings.conflicts(listOf("a" to "key.keyboard.r", "b" to "key.keyboard.r", "c" to "key.keyboard.t"), emptyList()))

    /**
     * Verifies that unbound bindings never conflict with each other.
     */
    @Test
    fun `unbound bindings never conflict`() =
        assertEquals(emptySet<String>(), KeyBindings.conflicts(listOf("a" to "key.keyboard.unknown", "b" to "key.keyboard.unknown"), emptyList()))

    /**
     * Verifies that a roleplay binding conflicts with a binding outside the roleplay category on
     * the same key.
     */
    @Test
    fun `a roleplay binding conflicts with another binding on its key`() =
        assertEquals(setOf("a"), KeyBindings.conflicts(listOf("a" to "key.keyboard.w", "b" to "key.keyboard.t"), listOf("key.keyboard.w", "key.keyboard.s")))

    /**
     * Verifies that every roleplay binding of a key bound three times is marked.
     */
    @Test
    fun `three bindings on one key all conflict`() =
        assertEquals(setOf("a", "b"), KeyBindings.conflicts(listOf("a" to "key.keyboard.r", "b" to "key.keyboard.r"), listOf("key.keyboard.r")))

    /**
     * Verifies that an unbound roleplay binding does not conflict with unbound other bindings.
     */
    @Test
    fun `an unbound binding does not conflict with other unbound bindings`() =
        assertEquals(emptySet<String>(), KeyBindings.conflicts(listOf("a" to "key.keyboard.unknown"), listOf("key.keyboard.unknown", "key.keyboard.unknown")))
}
