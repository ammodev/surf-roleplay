package dev.slne.surf.roleplay.paper.interaction.binding

import dev.slne.surf.roleplay.api.client.paper.interaction.TargetKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for reading and validating interactions.yml.
 */
class BindingParserTest {

    /**
     * Returns the kinds of the behaviours known in these tests.
     *
     * @param key the behaviour key
     * @return its kinds, or `null` if unknown
     */
    private fun kinds(key: String): Set<TargetKind>? = when (key) {
        "door.toggle" -> setOf(TargetKind.BLOCK)
        "npc.talk" -> setOf(TargetKind.ENTITY)
        else -> null
    }

    /**
     * Verifies that valid block and entity bindings load with parameters and range.
     */
    @Test
    fun `valid bindings load`() {
        val result = BindingParser.parse(
            """
            blocks:
              - world: world
                x: 10
                y: 64
                z: -3
                range: 4.5
                behaviours:
                  - door.toggle
            entities:
              - key: guard_1
                behaviours:
                  - key: npc.talk
                    parameters:
                      greeting: "Moin"
            """.trimIndent(),
            ::kinds,
        )

        assertEquals(emptyList(), result.errors)
        assertEquals(TargetBinding(listOf(BehaviourBinding("door.toggle", emptyMap())), 4.5), result.bindings.blocks[BlockPosition("world", 10, 64, -3)])
        assertEquals(TargetBinding(listOf(BehaviourBinding("npc.talk", mapOf("greeting" to "Moin"))), null), result.bindings.entities["guard_1"])
    }

    /**
     * Verifies that each invalid entry is reported and skipped while the valid ones load.
     */
    @Test
    fun `invalid entries are skipped and reported`() {
        val result = BindingParser.parse(
            """
            blocks:
              - { world: world, x: 1, y: 64, z: 1, behaviours: [door.toggle] }
              - { world: world, x: 2, y: 64, z: 2, behaviours: [door.togle] }
              - { world: world, x: 3, y: 64, behaviours: [door.toggle] }
              - { world: world, x: 4, y: 64, z: 4, range: 12, behaviours: [door.toggle] }
              - { world: world, x: 1, y: 64, z: 1, behaviours: [door.toggle] }
              - { world: world, x: 5, y: 64, z: 5, behaviours: [npc.talk] }
              - { world: world, x: 6, y: 64, z: 6, behaviours: [] }
            entities:
              - { key: "Bad Key", behaviours: [npc.talk] }
            """.trimIndent(),
            ::kinds,
        )

        assertEquals(setOf(BlockPosition("world", 1, 64, 1)), result.bindings.blocks.keys)
        assertEquals(emptyMap(), result.bindings.entities)
        assertEquals(7, result.errors.size)
        assertTrue(result.errors.any { "door.togle" in it })
    }

    /**
     * Verifies the limits on behaviours per entry and on coordinate values.
     */
    @Test
    fun `behaviour count duplicates and coordinate range are validated`() {
        val many = (1..17).joinToString(", ") { "door.toggle" }
        val result = BindingParser.parse(
            """
            blocks:
              - { world: world, x: 1, y: 64, z: 1, behaviours: [door.toggle, door.toggle] }
              - { world: world, x: 2, y: 64, z: 2, behaviours: [$many] }
              - { world: world, x: 4294967306, y: 64, z: 3, behaviours: [door.toggle] }
            """.trimIndent(),
            ::kinds,
        )

        assertEquals(emptyMap(), result.bindings.blocks)
        assertEquals(3, result.errors.size)
        assertTrue(result.errors.any { "duplicate" in it })
        assertTrue(result.errors.any { "at most 16" in it })
    }

    /**
     * Verifies that an empty file gives no bindings and that unparsable YAML throws.
     */
    @Test
    fun `empty and broken files`() {
        assertEquals(Bindings.EMPTY, BindingParser.parse("", ::kinds).bindings)
        assertFailsWith<BindingParser.InvalidFileException> { BindingParser.parse("blocks: [ {", ::kinds) }
    }
}
