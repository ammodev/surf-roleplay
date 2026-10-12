package dev.slne.surf.roleplay.api.client.paper.interaction

import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for the validation of the public interaction types.
 */
class InteractionApiTest {

    /**
     * Verifies that target kinds must be snake_case.
     */
    @Test
    fun `target kinds must be snake_case`() {
        assertEquals("vehicle", TargetKind("vehicle").key)
        assertFailsWith<IllegalArgumentException> { TargetKind("Vehicle") }
        assertFailsWith<IllegalArgumentException> { TargetKind("") }
    }

    /**
     * Verifies that a behaviour needs a dotted lowercase key and at least one target kind.
     */
    @Test
    fun `behaviours validate key and kinds`() {
        val action = InteractionAction { }
        InteractionBehaviour("door.toggle", setOf(TargetKind.BLOCK), { Component.text("Öffnen") }, action = action)
        assertFailsWith<IllegalArgumentException> { InteractionBehaviour("Door Toggle", setOf(TargetKind.BLOCK), { Component.empty() }, action = action) }
        assertFailsWith<IllegalArgumentException> { InteractionBehaviour("door.toggle", emptySet(), { Component.empty() }, action = action) }
    }

    /**
     * Verifies that radial entries validate their keys.
     */
    @Test
    fun `radial entries validate keys`() {
        RadialAction("demo.hello", Component.text("Hallo")) { }
        assertFailsWith<IllegalArgumentException> { RadialAction("demo hello", Component.text("Hallo")) { } }
        assertFailsWith<IllegalArgumentException> { RadialSubmenu("", Component.text("Test"), children = emptyList()) }
    }
}
