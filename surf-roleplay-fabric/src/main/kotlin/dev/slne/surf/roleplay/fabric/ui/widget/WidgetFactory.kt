package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.CheckboxNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.DropdownNode
import dev.slne.surf.roleplay.protocol.screen.ImageNode
import dev.slne.surf.roleplay.protocol.screen.InputValue
import dev.slne.surf.roleplay.protocol.screen.LabelNode
import dev.slne.surf.roleplay.protocol.screen.NumberInputNode
import dev.slne.surf.roleplay.protocol.screen.ProgressNode
import dev.slne.surf.roleplay.protocol.screen.RowNode
import dev.slne.surf.roleplay.protocol.screen.ScreenNode
import dev.slne.surf.roleplay.protocol.screen.ScrollListNode
import dev.slne.surf.roleplay.protocol.screen.TextInputNode

/**
 * Creates widgets from the screen nodes sent by the server.
 */
object WidgetFactory {

    /**
     * Creates the widget of a node, with the widgets of its children for containers.
     *
     * @param node the node
     * @return the widget, carrying the node's id, sizing and values
     */
    fun create(node: ScreenNode): Widget {
        val widget = when (node) {
            is RowNode -> container(ContainerWidget(node.id, Axis.HORIZONTAL), node.children).apply {
                gap = node.gap
                padding = node.padding
                mainAlign = node.mainAlign
                crossAlign = node.crossAlign
            }

            is ColumnNode -> container(ContainerWidget(node.id, Axis.VERTICAL), node.children).apply {
                gap = node.gap
                padding = node.padding
                mainAlign = node.mainAlign
                crossAlign = node.crossAlign
            }

            is ScrollListNode -> container(ScrollListWidget(node.id), node.children).apply { gap = node.gap }
            is LabelNode -> LabelWidget(node.id, node.text)
            is ButtonNode -> ButtonWidget(node.id, node.text).apply { enabled = node.enabled }
            is TextInputNode -> TextInputWidget(
                node.id,
                TextEditState(node.value, TextFilter.maxLength(node.maxLength)),
                node.placeholder,
                node.required,
            ).apply { enabled = node.enabled }

            is NumberInputNode -> NumberInputWidget(node.id, NumberFilter(node.min, node.max), node.value, node.required)
                .apply { enabled = node.enabled }

            is CheckboxNode -> CheckboxWidget(node.id, node.label, node.checked).apply { enabled = node.enabled }
            is DropdownNode -> DropdownWidget(node.id, node.options, node.selected, node.required).apply { enabled = node.enabled }
            is ImageNode -> ImageWidget(node.id, node.texture)
            is ProgressNode -> ProgressWidget(node.id, node.progress, node.label)
        }
        widget.width = node.width
        widget.height = node.height
        return widget
    }

    /**
     * Fills a container with the widgets of child nodes.
     *
     * @param container the container
     * @param children the child nodes
     * @return the container
     */
    private fun <C : ContainerWidget> container(container: C, children: List<ScreenNode>): C {
        children.mapTo(container.childList) { create(it) }
        return container
    }
}

/**
 * Queries on a tree of widgets.
 */
object WidgetTree {

    /**
     * Finds a widget by id.
     *
     * @param root the root of the tree
     * @param id the id to find
     * @return the widget, or `null` if the tree has no widget with that id
     */
    fun find(root: Widget, id: String): Widget? {
        if (root.id == id) return root
        for (child in root.children) {
            find(child, id)?.let { return it }
        }
        return null
    }

    /**
     * Finds the container that directly holds a widget.
     *
     * @param root the root of the tree
     * @param id the id of the child
     * @return the parent container, or `null` if the widget is the root or not in the tree
     */
    fun parentOf(root: Widget, id: String): ContainerWidget? {
        if (root !is ContainerWidget) return null
        if (root.childList.any { it.id == id }) return root
        for (child in root.childList) {
            parentOf(child, id)?.let { return it }
        }
        return null
    }

    /**
     * Collects the input values of every input widget, in tree order.
     *
     * @param root the root of the tree
     * @return the input values
     */
    fun inputValues(root: Widget): List<InputValue> {
        val values = mutableListOf<InputValue>()
        visit(root) { widget -> widget.inputValue?.let { values += InputValue(widget.id, it) } }
        return values
    }

    /**
     * Calls a function for every widget of a tree, parents before children.
     *
     * @param root the root of the tree
     * @param action the function to call
     */
    fun visit(root: Widget, action: (Widget) -> Unit) {
        action(root)
        root.children.forEach { visit(it, action) }
    }
}
