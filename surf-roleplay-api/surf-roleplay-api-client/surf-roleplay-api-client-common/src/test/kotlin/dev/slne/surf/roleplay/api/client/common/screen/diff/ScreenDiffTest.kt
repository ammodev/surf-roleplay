package dev.slne.surf.roleplay.api.client.common.screen.diff

import dev.slne.surf.roleplay.api.client.common.screen.ContainerElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Checkbox
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NumberInput
import dev.slne.surf.roleplay.api.client.common.screen.dsl.P
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Popover
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Select
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Sidebar
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarHeader
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarInset
import dev.slne.surf.roleplay.api.client.common.screen.dsl.SidebarProvider
import dev.slne.surf.roleplay.api.client.common.screen.dsl.renderRoot
import net.kyori.adventure.text.Component
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

    /**
     * Verifies that an element with an explicit id that moves into a later sibling's subtree
     * replaces the lowest common ancestor of its old and new parents instead of being inserted
     * while it still exists.
     */
    @Test
    fun `an element moved into a later container replaces the common ancestor`() {
        val old = tree { Column { Row(id = "A") {}; Row(id = "B") { P("x", id = "x") } } }
        val new = tree { Column { Row(id = "A") { P("x", id = "x") }; Row(id = "B") {} } }
        val change = assertIs<ScreenChange.Replace>(ScreenDiff.diff(old, new).single())
        assertEquals("_0", change.targetId)
        assertEquals(new, change.element)
    }

    /**
     * Verifies that an element with an explicit id that moves into an earlier sibling's subtree
     * replaces the lowest common ancestor of its old and new parents.
     */
    @Test
    fun `an element moved into an earlier container replaces the common ancestor`() {
        val old = tree { Column { Row(id = "A") { P("x", id = "x") }; Row(id = "B") {} } }
        val new = tree { Column { Row(id = "A") {}; Row(id = "B") { P("x", id = "x") } } }
        val change = assertIs<ScreenChange.Replace>(ScreenDiff.diff(old, new).single())
        assertEquals("_0", change.targetId)
    }

    /**
     * Verifies that a move deep in the tree replaces only the lowest common ancestor, and that
     * unrelated parts of the tree are diffed as usual.
     */
    @Test
    fun `a move replaces only the lowest common ancestor`() {
        val old = tree { Column { P("a", id = "a"); Column(id = "C") { Row(id = "A") { P("x", id = "x") }; Row(id = "B") {} } } }
        val new = tree { Column { P("a2", id = "a"); Column(id = "C") { Row(id = "A") {}; Row(id = "B") { P("x", id = "x") } } } }
        val changes = ScreenDiff.diff(old, new)
        assertEquals(listOf("a", "C"), changes.map { assertIs<ScreenChange.Replace>(it).targetId })
    }

    /**
     * Verifies that a container wrapped around an existing element replaces the common ancestor
     * instead of inserting a subtree that holds an id the old tree still has.
     */
    @Test
    fun `an element wrapped in a new container replaces the common ancestor`() {
        val old = tree { Column { P("x", id = "x"); Row(id = "B") {} } }
        val new = tree { Column { Row(id = "B") { Row(id = "W") { P("x", id = "x") } } } }
        val change = assertIs<ScreenChange.Replace>(ScreenDiff.diff(old, new).single())
        assertEquals("_0", change.targetId)
    }

    /**
     * Verifies that an input whose only change is its value gets a value change in the wire form.
     */
    @Test
    fun `an input whose value changed gets a set value`() {
        val changes = ScreenDiff.diff(tree { Column { Input(value = "a", id = "q") } }, tree { Column { Input(value = "ab", id = "q") } })
        assertEquals(listOf(ScreenChange.SetValue("q", "ab")), changes)
    }

    /**
     * Verifies the wire form of number, checkbox and select values, including empty values.
     */
    @Test
    fun `value changes use the wire form of each input`() {
        val choices = listOf(SelectChoice("a", Component.text("A")), SelectChoice("b", Component.text("B")))
        val old = tree { Column { NumberInput(value = 3, id = "n"); Checkbox(checked = false, id = "c"); Select(options = choices, selected = "a", id = "s") } }
        val new = tree { Column { NumberInput(value = null, id = "n"); Checkbox(checked = true, id = "c"); Select(options = choices, selected = null, id = "s") } }
        assertEquals(
            listOf(ScreenChange.SetValue("n", ""), ScreenChange.SetValue("c", "true"), ScreenChange.SetValue("s", "")),
            ScreenDiff.diff(old, new),
        )
    }

    /**
     * Verifies that an input whose value and another field changed is replaced.
     */
    @Test
    fun `an input with a changed value and placeholder is replaced`() {
        val change = ScreenDiff.diff(
            tree { Column { Input(value = "a", placeholder = Component.text("p"), id = "q") } },
            tree { Column { Input(value = "b", placeholder = Component.text("r"), id = "q") } },
        ).single()
        assertEquals("q", assertIs<ScreenChange.Replace>(change).targetId)
    }

    /**
     * Verifies that an overlay whose only change is its open state gets an open change, and its
     * children are still diffed.
     */
    @Test
    fun `an overlay whose open state changed gets a set open`() {
        val old = tree { Column { Popover(open = false, id = "pop") { P("a", id = "a") } } }
        val new = tree { Column { Popover(open = true, id = "pop") { P("b", id = "a") } } }
        val changes = ScreenDiff.diff(old, new)
        assertEquals(ScreenChange.SetOpen("pop", true), changes[0])
        assertEquals("a", assertIs<ScreenChange.Replace>(changes[1]).targetId)
        assertEquals(2, changes.size)
    }

    /**
     * Verifies that an overlay whose open state changed and whose children were reordered is
     * replaced without a separate open change.
     */
    @Test
    fun `an overlay with a changed open state and reordered children is replaced`() {
        val old = tree { Column { Popover(open = false, id = "pop") { P("a", id = "a"); P("b", id = "b") } } }
        val new = tree { Column { Popover(open = true, id = "pop") { P("b", id = "b"); P("a", id = "a") } } }
        assertEquals(listOf<ScreenChange>(ScreenChange.Replace("pop", (new as ContainerElement).children.single())), ScreenDiff.diff(old, new))
    }

    /**
     * Verifies that an input whose new value equals the value the screen reported sends nothing,
     * although the old tree holds another value.
     */
    @Test
    fun `a reported value equal to the new value gives no change`() {
        val old = tree { Column { Input(value = "", id = "q") } }
        val new = tree { Column { Input(value = "abc", id = "q") } }
        assertEquals(emptyList(), ScreenDiff.diff(old, new, mapOf("q" to "abc")))
    }

    /**
     * Verifies that an input whose rendered value did not change gets no change, whatever value
     * the screen reported, so that the text the player typed stays.
     */
    @Test
    fun `an unchanged rendered value gives no change despite a reported value`() {
        val old = tree { Column { Input(value = "", id = "q"); P("a", id = "a") } }
        val new = tree { Column { Input(value = "", id = "q"); P("b", id = "a") } }
        assertEquals(listOf("a"), ScreenDiff.diff(old, new, mapOf("q" to "abc")).map { (it as ScreenChange.Replace).targetId })
    }

    /**
     * Verifies that a new rendered value that differs from both the old rendered value and the
     * reported value is set.
     */
    @Test
    fun `a new value that differs from the rendered and reported values is set`() {
        val old = tree { Column { Input(value = "y", id = "q") } }
        val new = tree { Column { Input(value = "x", id = "q") } }
        assertEquals(listOf(ScreenChange.SetValue("q", "x")), ScreenDiff.diff(old, new, mapOf("q" to "abc")))
    }

    /**
     * Verifies that a sidebar provider whose only change is its expanded state gets a set value
     * instead of being replaced, that its children are still diffed, and that a reported state
     * counts as shown.
     */
    @Test
    fun `a sidebar provider whose expanded state changed gets a set value`() {
        fun provider(open: Boolean, text: String) = tree {
            SidebarProvider(open = open, id = "sp") {
                Sidebar { SidebarHeader { P(text, id = "t") } }
                SidebarInset { P("x", id = "x") }
            }
        }
        val changes = ScreenDiff.diff(provider(true, "a"), provider(false, "b"))
        assertEquals(ScreenChange.SetValue("sp", "false"), changes[0])
        assertEquals(listOf("t"), changes.drop(1).map { (it as ScreenChange.Replace).targetId })
        assertEquals(listOf("t"), ScreenDiff.diff(provider(true, "a"), provider(false, "b"), mapOf("sp" to "false")).map { (it as ScreenChange.Replace).targetId })
    }

    /**
     * Verifies that a reported open state counts as shown, and that an unchanged rendered open
     * state gives no change.
     */
    @Test
    fun `a reported open state counts as shown`() {
        val old = tree { Column { Popover(open = false, id = "pop") { P("a", id = "a") } } }
        val new = tree { Column { Popover(open = true, id = "pop") { P("a", id = "a") } } }
        assertEquals(emptyList(), ScreenDiff.diff(old, new, mapOf("pop" to "true")))
        assertEquals(listOf(ScreenChange.SetOpen("pop", true)), ScreenDiff.diff(old, new, mapOf("pop" to "false")))
        assertEquals(emptyList(), ScreenDiff.diff(old, old, mapOf("pop" to "true")))
    }

    /**
     * Verifies that an input with a reported value and another changed field is still replaced.
     */
    @Test
    fun `a reported input with another change is replaced`() {
        val old = tree { Column { Input(value = "", placeholder = Component.text("p"), id = "q") } }
        val new = tree { Column { Input(value = "abc", placeholder = Component.text("r"), id = "q") } }
        assertEquals("q", assertIs<ScreenChange.Replace>(ScreenDiff.diff(old, new, mapOf("q" to "abc")).single()).targetId)
    }

    /**
     * Verifies that a reported id that is not in the old tree changes nothing.
     */
    @Test
    fun `a reported id missing from the tree is ignored`() {
        val old = tree { Column { P("a", id = "a") } }
        assertEquals(emptyList(), ScreenDiff.diff(old, old, mapOf("gone" to "x")))
    }
}
