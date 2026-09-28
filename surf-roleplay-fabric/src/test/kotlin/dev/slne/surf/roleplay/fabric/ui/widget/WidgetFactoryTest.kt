package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.protocol.screen.Align
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.CheckboxNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.DropdownNode
import dev.slne.surf.roleplay.protocol.screen.DropdownOption
import dev.slne.surf.roleplay.protocol.screen.ImageNode
import dev.slne.surf.roleplay.protocol.screen.InputValue
import dev.slne.surf.roleplay.protocol.screen.Insets
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.NumberInputNode
import dev.slne.surf.roleplay.protocol.screen.ProgressNode
import dev.slne.surf.roleplay.protocol.screen.RowNode
import dev.slne.surf.roleplay.protocol.screen.ScrollListNode
import dev.slne.surf.roleplay.protocol.screen.Sizing
import dev.slne.surf.roleplay.protocol.screen.TextInputNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Tests for [WidgetFactory] and [WidgetTree].
 */
class WidgetFactoryTest {

    /**
     * A tree with every node kind.
     */
    private val node = ColumnNode(
        id = "root",
        width = Sizing.fixed(200),
        gap = 4,
        padding = Insets.all(2),
        crossAlign = Align.STRETCH,
        children = listOf(
            LabelNode("title", text = "\"Hallo\""),
            RowNode(
                id = "inputs",
                children = listOf(
                    TextInputNode("name", value = "Max", maxLength = 8, required = true),
                    NumberInputNode("age", value = 30, min = 18, max = 99),
                    CheckboxNode("agree", checked = true),
                    DropdownNode("city", options = listOf(DropdownOption("north", "\"Nord\""), DropdownOption("south", "\"Süd\"")), selected = "south"),
                ),
            ),
            ScrollListNode("list", children = listOf(ButtonNode("open", text = "\"Öffnen\"", enabled = false))),
            ImageNode("logo", texture = "surf-roleplay:textures/gui/logo.png"),
            ProgressNode("load", progress = 0.5f, label = "\"50%\""),
        ),
    )

    /**
     * Verifies that every node kind becomes its widget with the node's id, sizing and values.
     */
    @Test
    fun `every node kind becomes its widget`() {
        val root = assertIs<ContainerWidget>(WidgetFactory.create(node))

        assertEquals(200, root.width.value)
        assertEquals(4, root.gap)
        assertEquals(Align.STRETCH, root.crossAlign)
        assertEquals("\"Hallo\"", assertIs<LabelWidget>(WidgetTree.find(root, "title")).text)
        assertEquals("Max", assertIs<TextInputWidget>(WidgetTree.find(root, "name")).edit.text)
        assertEquals(8, assertIs<TextInputWidget>(WidgetTree.find(root, "name")).edit.filter.maxLength)
        assertEquals("30", assertIs<NumberInputWidget>(WidgetTree.find(root, "age")).edit.text)
        assertEquals(true, assertIs<CheckboxWidget>(WidgetTree.find(root, "agree")).checked)
        assertEquals("south", assertIs<DropdownWidget>(WidgetTree.find(root, "city")).selected)
        assertEquals(false, assertIs<ButtonWidget>(WidgetTree.find(root, "open")).enabled)
        assertIs<ScrollListWidget>(WidgetTree.find(root, "list"))
        assertEquals("surf-roleplay:textures/gui/logo.png", assertIs<ImageWidget>(WidgetTree.find(root, "logo")).texture)
        assertEquals(0.5f, assertIs<ProgressWidget>(WidgetTree.find(root, "load")).progress)
        assertNull(WidgetTree.find(root, "missing"))
    }

    /**
     * Verifies that the input values of a tree are collected in tree order, in their string form.
     */
    @Test
    fun `input values are collected in tree order`() {
        val root = WidgetFactory.create(node)

        assertEquals(
            listOf(InputValue("name", "Max"), InputValue("age", "30"), InputValue("agree", "true"), InputValue("city", "south")),
            WidgetTree.inputValues(root),
        )
    }

    /**
     * Verifies that a scroll list stretches its rows across its width and keeps room for its
     * scroll bar at the right.
     */
    @Test
    fun `scroll list stretches rows and reserves the scroll bar`() {
        val list = assertIs<ScrollListWidget>(WidgetFactory.create(ScrollListNode("list")))

        assertEquals(Align.STRETCH, list.crossAlign)
        assertEquals(dev.slne.surf.roleplay.fabric.ui.theme.UiMetrics.SCROLL_BAR_WIDTH + 2, list.padding.right)
    }

    /**
     * Verifies that an icon node becomes an icon widget with its name, size and colour.
     */
    @Test
    fun `icon nodes become icon widgets`() {
        val widget = assertIs<IconWidget>(
            WidgetFactory.create(dev.slne.surf.roleplay.protocol.screen.IconNode("i", icon = "shield", size = 24, color = dev.slne.surf.roleplay.protocol.screen.IconColor.PRIMARY)),
        )

        assertEquals("shield", widget.icon)
        assertEquals(24, widget.size)
        assertEquals(dev.slne.surf.roleplay.protocol.screen.IconColor.PRIMARY, widget.color)
    }

    /**
     * Verifies that leading icons of labels, buttons and text inputs reach their widgets.
     */
    @Test
    fun `leading icons reach their widgets`() {
        val root = WidgetFactory.create(
            ColumnNode("root", children = listOf(LabelNode("l", icon = "info"), ButtonNode("b", icon = "trash"), TextInputNode("t", icon = "search"))),
        )

        assertEquals("info", assertIs<LabelWidget>(WidgetTree.find(root, "l")).icon)
        assertEquals("trash", assertIs<ButtonWidget>(WidgetTree.find(root, "b")).icon)
        assertEquals("search", assertIs<TextInputWidget>(WidgetTree.find(root, "t")).icon)
    }
}
