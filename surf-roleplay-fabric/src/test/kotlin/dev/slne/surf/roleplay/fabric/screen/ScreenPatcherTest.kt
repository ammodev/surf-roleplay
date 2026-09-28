package dev.slne.surf.roleplay.fabric.screen

import dev.slne.surf.roleplay.fabric.ui.widget.ButtonWidget
import dev.slne.surf.roleplay.fabric.ui.widget.ContainerWidget
import dev.slne.surf.roleplay.fabric.ui.widget.LabelWidget
import dev.slne.surf.roleplay.fabric.ui.widget.ProgressWidget
import dev.slne.surf.roleplay.fabric.ui.widget.TextInputWidget
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetFactory
import dev.slne.surf.roleplay.fabric.ui.widget.WidgetTree
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.InsertNode
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.ProgressNode
import dev.slne.surf.roleplay.protocol.screen.RemoveNode
import dev.slne.surf.roleplay.protocol.screen.ReplaceNode
import dev.slne.surf.roleplay.protocol.screen.SetEnabled
import dev.slne.surf.roleplay.protocol.screen.SetProgress
import dev.slne.surf.roleplay.protocol.screen.SetText
import dev.slne.surf.roleplay.protocol.screen.SetValue
import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Tests for [ScreenPatcher].
 */
class ScreenPatcherTest {

    /**
     * Creates a small tree: a title, a name input, a progress bar and a button in a column.
     *
     * @return the root widget
     */
    private fun tree() = WidgetFactory.create(
        ColumnNode(
            "root",
            children = listOf(
                LabelNode("title", text = "\"A\""),
                TextInputNode("name"),
                ProgressNode("load"),
                ButtonNode("ok"),
            ),
        ),
    )

    /**
     * Returns the ids of a container's children.
     *
     * @return the child ids
     */
    private fun ContainerWidget.ids() = childList.map { it.id }

    /**
     * Verifies that text, value, progress and enabled operations change their widgets.
     */
    @Test
    fun `set operations change their widgets`() {
        val root = tree()

        val result = ScreenPatcher.apply(
            root,
            listOf(SetText("title", "\"B\""), SetValue("name", "Erika"), SetProgress("load", 0.5f), SetEnabled("ok", false)),
        )

        assertSame(root, result.root)
        assertEquals("\"B\"", assertIs<LabelWidget>(WidgetTree.find(root, "title")).text)
        assertEquals("Erika", assertIs<TextInputWidget>(WidgetTree.find(root, "name")).edit.text)
        assertEquals(0.5f, assertIs<ProgressWidget>(WidgetTree.find(root, "load")).progress)
        assertEquals(false, assertIs<ButtonWidget>(WidgetTree.find(root, "ok")).enabled)
    }

    /**
     * Verifies that replacing a node swaps it in place and keeps its siblings, including typed
     * text, as the same widget objects.
     */
    @Test
    fun `replace swaps a node and keeps its siblings`() {
        val root = tree() as ContainerWidget
        val input = WidgetTree.find(root, "name") as TextInputWidget
        input.edit.insert("typed")

        ScreenPatcher.apply(root, listOf(ReplaceNode("title", ButtonNode("title_button"))))

        assertEquals(listOf("title_button", "name", "load", "ok"), root.ids())
        assertSame(input, WidgetTree.find(root, "name"))
        assertEquals("typed", input.edit.text)
    }

    /**
     * Verifies that inserting places a node at its index and appends it for an index beyond the
     * end.
     */
    @Test
    fun `insert places nodes at an index or appends them`() {
        val root = tree() as ContainerWidget

        ScreenPatcher.apply(root, listOf(InsertNode("root", 1, LabelNode("second")), InsertNode("root", 99, LabelNode("last"))))

        assertEquals(listOf("title", "second", "name", "load", "ok", "last"), root.ids())
    }

    /**
     * Verifies that removing takes a node out of its container.
     */
    @Test
    fun `remove takes a node out`() {
        val root = tree() as ContainerWidget

        ScreenPatcher.apply(root, listOf(RemoveNode("load")))

        assertEquals(listOf("title", "name", "ok"), root.ids())
        assertNull(WidgetTree.find(root, "load"))
    }

    /**
     * Verifies that replacing the root returns the new root.
     */
    @Test
    fun `replacing the root returns the new root`() {
        val result = ScreenPatcher.apply(tree(), listOf(ReplaceNode("root", LabelNode("new_root"))))

        assertEquals("new_root", result.root.id)
    }

    /**
     * Verifies that operations on unknown ids are skipped and reported while the other operations
     * still apply.
     */
    @Test
    fun `unknown targets are skipped and reported`() {
        val root = tree()

        val result = ScreenPatcher.apply(root, listOf(SetText("missing", "\"x\""), RemoveNode("root"), SetText("title", "\"C\"")))

        assertEquals(listOf("missing", "root"), result.skipped)
        assertEquals("\"C\"", assertIs<LabelWidget>(WidgetTree.find(root, "title")).text)
    }
}
