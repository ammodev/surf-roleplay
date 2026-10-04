package dev.slne.surf.roleplay.api.client.common.screen.diff

import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.P
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.renderRoot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [ScreenDiff].
 */
class ScreenDiffTest {
    /**
     * Renders a tree with the identity binder.
     *
     * @param content the builder of the root element
     * @return the root element
     */
    private fun tree(content: ComponentScope.() -> Unit) = renderRoot(content = content)

    /**
     * Verifies that two equal trees produce no change.
     */
    @Test
    fun `equal trees give no changes`() =
        assertEquals(emptyList(), ScreenDiff.diff(tree { Column { P("a") } }, tree { Column { P("a") } }))

    /**
     * Verifies that a leaf whose text changed is replaced by its id.
     */
    @Test
    fun `a changed leaf is replaced`() {
        val changes = ScreenDiff.diff(tree { Column { P("a") } }, tree { Column { P("b") } })
        assertEquals(listOf("_0.0"), changes.map { (it as ScreenChange.Replace).targetId })
    }

    /**
     * Verifies that a child missing from the new tree is removed.
     */
    @Test
    fun `a removed child is removed`() =
        assertEquals(
            listOf(ScreenChange.Remove("b")),
            ScreenDiff.diff(tree { Column { P("a", id = "a"); P("b", id = "b") } }, tree { Column { P("a", id = "a") } }),
        )

    /**
     * Verifies that a child new in the new tree is inserted at its index.
     */
    @Test
    fun `an added child is inserted at its index`() {
        val change = ScreenDiff.diff(tree { Column { P("a", id = "a") } }, tree { Column { P("a", id = "a"); P("b", id = "b") } }).single() as ScreenChange.Insert
        assertEquals(1, change.index)
        assertEquals("b", change.element.id)
        assertEquals("_0", change.parentId)
    }

    /**
     * Verifies that children whose relative order changed replace the whole container.
     */
    @Test
    fun `a reorder replaces the container`() {
        val change = ScreenDiff.diff(tree { Column { P("a", id = "a"); P("b", id = "b") } }, tree { Column { P("b", id = "b"); P("a", id = "a") } }).single()
        assertTrue(change is ScreenChange.Replace && change.targetId == "_0")
    }

    /**
     * Verifies that a changed attribute of a container replaces the container.
     */
    @Test
    fun `a changed container attribute replaces the container`() {
        val change = ScreenDiff.diff(tree { Column(gap = 2) { P("a") } }, tree { Column(gap = 4) { P("a") } }).single()
        assertTrue(change is ScreenChange.Replace)
    }

    /**
     * Verifies that an input that did not change produces no change, so typed text stays.
     */
    @Test
    fun `an unchanged input gives no change`() =
        assertEquals(emptyList(), ScreenDiff.diff(tree { Column { Input(id = "q"); P("1") } }, tree { Column { Input(id = "q"); P("1") } }))

    /**
     * Verifies that an element of another class at the same id is replaced.
     */
    @Test
    fun `a different class at the same place is replaced`() {
        val change = assertIs<ScreenChange.Replace>(ScreenDiff.diff(tree { Column { P("a") } }, tree { Row { P("a") } }).single())
        assertEquals("_0", change.targetId)
    }

    /**
     * Verifies that removals, recursive changes and insertions combine in one container, with
     * insertions in ascending index order.
     */
    @Test
    fun `removals, nested changes and insertions combine`() {
        val old = tree { Column { P("a", id = "a"); P("b", id = "b"); P("c", id = "c") } }
        val new = tree { Column { P("x", id = "x"); P("a2", id = "a"); P("c", id = "c"); P("y", id = "y") } }
        val changes = ScreenDiff.diff(old, new)
        assertEquals(4, changes.size)
        assertEquals(ScreenChange.Remove("b"), changes[0])
        assertEquals("a", assertIs<ScreenChange.Replace>(changes[1]).targetId)
        val first = assertIs<ScreenChange.Insert>(changes[2])
        val second = assertIs<ScreenChange.Insert>(changes[3])
        assertEquals(listOf(0 to "x", 3 to "y"), listOf(first.index to first.element.id, second.index to second.element.id))
    }

    /**
     * Verifies that a change deep in the tree replaces only the changed element.
     */
    @Test
    fun `a nested change replaces only the deepest changed element`() {
        val change = ScreenDiff.diff(tree { Column { Row { P("a"); P("b") } } }, tree { Column { Row { P("a"); P("c") } } }).single()
        assertEquals("_0.0.1", assertIs<ScreenChange.Replace>(change).targetId)
        assertEquals("_0.0.1", change.element.id)
    }
}
