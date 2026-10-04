package dev.slne.surf.roleplay.api.client.common.screen

import dev.slne.surf.roleplay.api.client.common.screen.dsl.Label
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests that patches build their content with the component DSL.
 */
class ScreenPatchBuilderTest {
    /**
     * Verifies that replacing an element with an explicit id keeps the id given to the new root.
     */
    @Test
    fun `replace builds the element with the component dsl`() {
        val changes = ScreenPatchBuilder().apply {
            replace("inputs") { Row(id = "inputs") { Label("Fertig", id = "hint") } }
        }.changes

        val replace = assertIs<ScreenChange.Replace>(changes.single())
        assertEquals("inputs", replace.element.id)
        assertEquals("hint", assertIs<RowElement>(replace.element).children.single().id)
    }

    /**
     * Verifies that replacing a generated element keeps the generated id of the target.
     */
    @Test
    fun `replace keeps the id of a generated target`() {
        val changes = ScreenPatchBuilder().apply {
            replace("_0.2") { Row { Label("Neu") } }
        }.changes

        val element = assertIs<ScreenChange.Replace>(changes.single()).element
        assertEquals("_0.2", element.id)
        assertEquals("_0.2.0", assertIs<RowElement>(element).children.single().id)
    }

    /**
     * Verifies that two appends get distinct generated ids, so that appended content never collides.
     */
    @Test
    fun `appends get distinct ids`() {
        val changes = ScreenPatchBuilder().apply {
            append("chat") { Label("Eins") }
            append("chat") { Label("Zwei") }
        }.changes

        val ids = changes.map { assertIs<ScreenChange.Insert>(it).element.id }
        assertEquals(2, ids.size)
        assertNotEquals(ids[0], ids[1])
        assertTrue(ids.all { it.startsWith("_+") })
        assertTrue(changes.all { assertIs<ScreenChange.Insert>(it).index == Int.MAX_VALUE })
    }

    /**
     * Verifies that several elements of one append get distinct ids below the same counter value.
     */
    @Test
    fun `an append of several elements gets distinct ids`() {
        val changes = ScreenPatchBuilder().apply {
            append("chat") {
                Label("Eins")
                Label("Zwei")
            }
        }.changes

        val ids = changes.map { assertIs<ScreenChange.Insert>(it).element.id }
        assertEquals(2, ids.toSet().size)
    }
}
