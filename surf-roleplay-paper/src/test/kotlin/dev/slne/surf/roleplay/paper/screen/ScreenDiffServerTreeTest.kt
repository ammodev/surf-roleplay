package dev.slne.surf.roleplay.paper.screen

import dev.slne.surf.roleplay.api.client.common.screen.AlertDialogElement
import dev.slne.surf.roleplay.api.client.common.screen.ColumnElement
import dev.slne.surf.roleplay.api.client.common.screen.ContextMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.DialogElement
import dev.slne.surf.roleplay.api.client.common.screen.DrawerElement
import dev.slne.surf.roleplay.api.client.common.screen.DropdownMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.HoverCardElement
import dev.slne.surf.roleplay.api.client.common.screen.MenuSubElement
import dev.slne.surf.roleplay.api.client.common.screen.MenubarMenuElement
import dev.slne.surf.roleplay.api.client.common.screen.NavigationMenuItemElement
import dev.slne.surf.roleplay.api.client.common.screen.PopoverElement
import dev.slne.surf.roleplay.api.client.common.screen.RadioChoice
import dev.slne.surf.roleplay.api.client.common.screen.ScreenChange
import dev.slne.surf.roleplay.api.client.common.screen.ScreenElement
import dev.slne.surf.roleplay.api.client.common.screen.SelectChoice
import dev.slne.surf.roleplay.api.client.common.screen.SheetElement
import dev.slne.surf.roleplay.api.client.common.screen.diff.ScreenDiff
import dev.slne.surf.roleplay.api.client.common.screen.dsl.AlertDialog
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Checkbox
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Column
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ComponentScope
import dev.slne.surf.roleplay.api.client.common.screen.dsl.ContextMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Dialog
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Drawer
import dev.slne.surf.roleplay.api.client.common.screen.dsl.DropdownMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.HoverCard
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Input
import dev.slne.surf.roleplay.api.client.common.screen.dsl.InputOtp
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenuSub
import dev.slne.surf.roleplay.api.client.common.screen.dsl.MenubarMenu
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NativeSelect
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NavigationMenuItem
import dev.slne.surf.roleplay.api.client.common.screen.dsl.NumberInput
import dev.slne.surf.roleplay.api.client.common.screen.dsl.P
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Popover
import dev.slne.surf.roleplay.api.client.common.screen.dsl.RadioGroup
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Row
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Select
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Sheet
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Switch
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Textarea
import dev.slne.surf.roleplay.api.client.common.screen.dsl.Toggle
import dev.slne.surf.roleplay.api.client.common.screen.dsl.renderRoot
import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifies that the changes [ScreenDiff] computes are accepted by [ServerScreenTree] and turn the
 * old tree into the new one.
 */
class ScreenDiffServerTreeTest {
    /**
     * The options of the select inputs.
     */
    private val choices = listOf(SelectChoice("a", Component.text("A")), SelectChoice("b", Component.text("B")))

    /**
     * The options of the radio group.
     */
    private val radios = listOf(RadioChoice("a", Component.text("A")), RadioChoice("b", Component.text("B")))

    /**
     * Renders a tree of every input that takes value changes, with values chosen by a flag.
     *
     * @param second whether to use the second set of values
     * @return the tree
     */
    private fun inputs(second: Boolean) = renderRoot {
        Column {
            Input(value = if (second) "neu" else "alt", id = "text")
            Textarea(value = if (second) "zwei\nZeilen" else "", id = "area")
            InputOtp(value = if (second) "123456" else "", id = "otp")
            NumberInput(value = if (second) null else 5, id = "number")
            Checkbox(checked = second, id = "check")
            Switch(checked = second, id = "switch")
            Toggle(pressed = second, id = "toggle")
            Select(options = choices, selected = if (second) null else "a", id = "select")
            NativeSelect(options = choices, selected = if (second) "b" else null, id = "native")
            RadioGroup(radios, selected = if (second) "b" else "a", id = "radio")
        }
    }

    /**
     * Renders a tree of every overlay that takes open changes, all closed.
     *
     * @return the tree
     */
    private fun overlays() = renderRoot {
        Column {
            val child: ComponentScope.() -> Unit = { P("Inhalt") }
            Popover(id = "popover", children = child)
            HoverCard(id = "hover", children = child)
            DropdownMenu(id = "dropdown", children = child)
            MenuSub(id = "sub", children = child)
            ContextMenu(id = "context", children = child)
            MenubarMenu(id = "menubar", children = child)
            Dialog(id = "dialog", children = child)
            AlertDialog(id = "alert", children = child)
            Sheet(id = "sheet", children = child)
            Drawer(id = "drawer", children = child)
            NavigationMenuItem(id = "nav", children = child)
        }
    }

    /**
     * Returns a tree with every overlay opened.
     *
     * @param element the root of the tree
     * @return the tree
     */
    private fun opened(element: ScreenElement): ScreenElement = when (element) {
        is PopoverElement -> element.copy(open = true)
        is HoverCardElement -> element.copy(open = true)
        is DropdownMenuElement -> element.copy(open = true)
        is MenuSubElement -> element.copy(open = true)
        is ContextMenuElement -> element.copy(open = true)
        is MenubarMenuElement -> element.copy(open = true)
        is DialogElement -> element.copy(open = true)
        is AlertDialogElement -> element.copy(open = true)
        is SheetElement -> element.copy(open = true)
        is DrawerElement -> element.copy(open = true)
        is NavigationMenuItemElement -> element.copy(open = true)
        is ColumnElement -> element.copy(children = element.children.map(::opened))
        else -> element
    }

    /**
     * Applies the diff of two trees to a server tree.
     *
     * @param old the old tree
     * @param new the new tree
     * @return the changes
     */
    private fun applyDiff(old: ScreenElement, new: ScreenElement): List<ScreenChange> {
        val tree = ServerScreenTree(old)
        val changes = ScreenDiff.diff(old, new)
        changes.forEach { assertTrue(tree.apply(it), "Refused $it") }
        assertEquals(new, tree.root)
        return changes
    }

    /**
     * Verifies that value changes of every supported input are accepted in both directions.
     */
    @Test
    fun `value changes are accepted by the server tree`() {
        val forward = applyDiff(inputs(false), inputs(true))
        assertEquals(10, forward.size)
        assertTrue(forward.all { it is ScreenChange.SetValue })
        assertTrue(applyDiff(inputs(true), inputs(false)).all { it is ScreenChange.SetValue })
    }

    /**
     * Verifies that open changes of every supported overlay are accepted in both directions.
     */
    @Test
    fun `open changes are accepted by the server tree`() {
        val closed = overlays()
        val forward = applyDiff(closed, opened(closed))
        assertEquals(11, forward.size)
        assertTrue(forward.all { it is ScreenChange.SetOpen })
        assertTrue(applyDiff(opened(closed), closed).all { it is ScreenChange.SetOpen })
    }

    /**
     * Verifies that an element moving between containers is accepted in both directions.
     */
    @Test
    fun `moves are accepted by the server tree`() {
        val inA = renderRoot { Column { Row(id = "A") { P("x", id = "x") }; Row(id = "B") {} } }
        val inB = renderRoot { Column { Row(id = "A") {}; Row(id = "B") { P("x", id = "x") } } }
        applyDiff(inA, inB)
        applyDiff(inB, inA)
    }
}
