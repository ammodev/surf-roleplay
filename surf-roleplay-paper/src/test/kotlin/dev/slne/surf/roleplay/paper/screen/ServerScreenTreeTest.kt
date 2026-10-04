package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.ButtonElement
import dev.slne.surf.roleplay.api.client.common.screen.CheckboxElement
import dev.slne.surf.roleplay.api.client.common.screen.ColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.ContainerElement
import dev.slne.surf.roleplay.api.client.common.screen.LabelElement
import dev.slne.surf.roleplay.api.client.common.screen.NumberInputElement
import dev.slne.surf.roleplay.api.client.common.screen.ProgressElement
import dev.slne.surf.roleplay.api.client.common.screen.RowElement
import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoiceGroup
import dev.slne.surf.roleplay.api.client.common.screen.SelectElement
import dev.slne.surf.roleplay.api.client.common.screen.TextInputElement
import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [ServerScreenTree].
 */
class ServerScreenTreeTest {

    /**
     * Creates a tree with a label, the four input kinds, a progress bar and a button in a row.
     *
     * @return the tree
     */
    private fun tree() = ServerScreenTree(
        ColumnElement(
            "root",
            children = listOf(
                LabelElement("title", Component.text("A")),
                RowElement(
                    "inputs",
                    children = listOf(
                        TextInputElement("name"),
                        NumberInputElement("age"),
                        CheckboxElement("agree"),
                        SelectElement("city", listOf(SelectChoiceGroup(null, listOf(SelectChoice("north", Component.text("Nord")))))),
                    ),
                ),
                ProgressElement("load", 0f),
                ButtonElement("ok", Component.text("OK")),
            ),
        ),
    )

    /**
     * Returns the ids of a container's children.
     *
     * @param id the id of the container
     * @return the child ids
     */
    private fun ServerScreenTree.childIds(id: String) = (find(id) as ContainerElement).children.map { it.id }

    /**
     * Verifies that the set changes update their elements.
     */
    @Test
    fun `set changes update their elements`() {
        val tree = tree()

        assertTrue(tree.apply(ScreenChange.SetText("title", Component.text("B"))))
        assertTrue(tree.apply(ScreenChange.SetValue("name", "Erika")))
        assertTrue(tree.apply(ScreenChange.SetValue("age", "42")))
        assertTrue(tree.apply(ScreenChange.SetValue("agree", "true")))
        assertTrue(tree.apply(ScreenChange.SetValue("city", "north")))
        assertTrue(tree.apply(ScreenChange.SetProgress("load", 0.5f)))
        assertTrue(tree.apply(ScreenChange.SetEnabled("ok", false)))

        assertEquals(Component.text("B"), assertIs<LabelElement>(tree.find("title")).text)
        assertEquals("Erika", assertIs<TextInputElement>(tree.find("name")).value)
        assertEquals(42L, assertIs<NumberInputElement>(tree.find("age")).value)
        assertEquals(true, assertIs<CheckboxElement>(tree.find("agree")).checked)
        assertEquals("north", assertIs<SelectElement>(tree.find("city")).selected)
        assertEquals(0.5f, assertIs<ProgressElement>(tree.find("load")).progress)
        assertEquals(false, assertIs<ButtonElement>(tree.find("ok")).enabled)
    }

    /**
     * Verifies that replace, insert and remove change the structure.
     */
    @Test
    fun `structural changes update the tree`() {
        val tree = tree()

        assertTrue(tree.apply(ScreenChange.Replace("title", LabelElement("subtitle", Component.text("C")))))
        assertTrue(tree.apply(ScreenChange.Insert("root", 1, LabelElement("hint", Component.text("D")))))
        assertTrue(tree.apply(ScreenChange.Insert("root", 99, LabelElement("footer", Component.text("E")))))
        assertTrue(tree.apply(ScreenChange.Remove("load")))

        assertEquals(listOf("subtitle", "hint", "inputs", "ok", "footer"), tree.childIds("root"))
        assertNull(tree.find("title"))
    }

    /**
     * Verifies that the root can be replaced but not removed.
     */
    @Test
    fun `the root can be replaced but not removed`() {
        val tree = tree()

        assertFalse(tree.apply(ScreenChange.Remove("root")))
        assertTrue(tree.apply(ScreenChange.Replace("root", LabelElement("new_root", Component.empty()))))

        assertEquals("new_root", tree.root.id)
    }

    /**
     * Verifies that changes for unknown ids or unsuitable elements are refused.
     */
    @Test
    fun `changes for unknown or unsuitable elements are refused`() {
        val tree = tree()

        assertFalse(tree.apply(ScreenChange.SetText("missing", Component.empty())))
        assertFalse(tree.apply(ScreenChange.SetProgress("title", 1f)))
        assertFalse(tree.apply(ScreenChange.Insert("title", 0, LabelElement("x", Component.empty()))))
        assertFalse(tree.apply(ScreenChange.SetValue("age", "keine Zahl")))
    }

    /**
     * Verifies that a change that would duplicate an id is refused.
     */
    @Test
    fun `changes that duplicate ids are refused`() {
        val tree = tree()

        assertFalse(tree.apply(ScreenChange.Insert("root", 0, LabelElement("ok", Component.empty()))))
        assertFalse(tree.apply(ScreenChange.Replace("title", LabelElement("name", Component.empty()))))
    }
}
