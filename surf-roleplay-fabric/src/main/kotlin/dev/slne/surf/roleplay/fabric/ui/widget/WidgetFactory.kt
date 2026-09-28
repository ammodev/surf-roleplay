package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupNode
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupTextNode
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.ToggleGroupNode
import dev.slne.surf.roleplay.protocol.screen.ToggleNode
import dev.slne.surf.roleplay.protocol.screen.CheckboxNode
import dev.slne.surf.roleplay.protocol.screen.ColumnNode
import dev.slne.surf.roleplay.protocol.screen.ComboboxNode
import dev.slne.surf.roleplay.protocol.screen.IconNode
import dev.slne.surf.roleplay.protocol.screen.NativeSelectNode
import dev.slne.surf.roleplay.protocol.screen.SelectNode
import dev.slne.surf.roleplay.protocol.screen.RadioGroupNode
import dev.slne.surf.roleplay.protocol.screen.SliderNode
import dev.slne.surf.roleplay.protocol.screen.SwitchNode
import dev.slne.surf.roleplay.protocol.screen.InputGroupAddonNode
import dev.slne.surf.roleplay.protocol.screen.InputGroupNode
import dev.slne.surf.roleplay.protocol.screen.InputGroupTextNode
import dev.slne.surf.roleplay.protocol.screen.InputOtpNode
import dev.slne.surf.roleplay.protocol.screen.TextareaNode
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
            is LabelNode -> LabelWidget(node.id, node.text, node.icon, node.forId)
            is ButtonNode -> ButtonWidget(node.id, node.text, node.icon, node.submitsInput, node.variant, node.size).apply { enabled = node.enabled }
            is TextInputNode -> TextInputWidget(
                node.id,
                TextEditState(node.value, TextFilter.maxLength(node.maxLength)),
                node.placeholder,
                node.required,
                node.icon,
                node.inputType,
            ).apply { enabled = node.enabled }

            is NumberInputNode -> NumberInputWidget(node.id, NumberFilter(node.min, node.max), node.value, node.required)
                .apply { enabled = node.enabled }

            is CheckboxNode -> CheckboxWidget(node.id, node.label, node.checked).apply { enabled = node.enabled }
            is SelectNode -> SelectWidget(node.id, node.groups, node.selected, node.placeholder, node.size, node.required).apply { enabled = node.enabled }
            is NativeSelectNode -> NativeSelectWidget(node.id, node.groups, node.selected, node.size, node.required).apply { enabled = node.enabled }
            is ComboboxNode -> ComboboxWidget(
                node.id, node.groups, node.selected, node.multiple, node.placeholder, node.emptyText, node.showClear, node.required,
            ).apply { enabled = node.enabled }
            is ImageNode -> ImageWidget(node.id, node.texture)
            is IconNode -> IconWidget(node.id, node.icon, node.size, node.color)
            is ButtonGroupNode -> container(ButtonGroupWidget(node.id, node.orientation), node.children)
            is ButtonGroupTextNode -> ButtonGroupTextWidget(node.id, node.text, node.icon)
            is ButtonGroupSeparatorNode -> ButtonGroupSeparatorWidget(node.id)
            is ToggleNode -> ToggleWidget(node.id, node.text, node.icon, node.pressed, node.variant, node.size).apply { enabled = node.enabled }
            is ToggleGroupNode -> ToggleGroupWidget(node.id, node.items, node.selected, node.multiple, node.variant, node.size, node.spacing, node.orientation, node.required)
                .apply { enabled = node.enabled }
            is ProgressNode -> ProgressWidget(node.id, node.progress, node.label)
            is SwitchNode -> SwitchWidget(node.id, node.checked, node.size).apply { enabled = node.enabled }
            is RadioGroupNode -> RadioGroupWidget(node.id, node.options, node.selected, node.orientation, node.required).apply { enabled = node.enabled }
            is SliderNode -> SliderWidget(node.id, node.values, node.min, node.max, node.step, node.orientation).apply { enabled = node.enabled }
            is TextareaNode -> TextareaWidget(node.id, TextEditState(node.value, TextFilter.maxLength(node.maxLength)), node.placeholder, node.rows, node.required)
                .apply { enabled = node.enabled }
            is InputGroupNode -> InputGroupWidget(node.id, node.children.map { create(it) })
            is InputGroupAddonNode -> container(InputGroupAddonWidget(node.id, node.align), node.children)
            is InputGroupTextNode -> InputGroupTextWidget(node.id, node.text, node.icon)
            is InputOtpNode -> InputOtpWidget(node.id, node.length, node.groups, node.pattern, node.value, node.required).apply { enabled = node.enabled }
        }
        widget.width = node.width
        widget.height = node.height
        widget.notifyChange = when (node) {
            is TextInputNode -> node.notifyChange
            is NumberInputNode -> node.notifyChange
            is CheckboxNode -> node.notifyChange
            is SelectNode -> node.notifyChange
            is NativeSelectNode -> node.notifyChange
            is ComboboxNode -> node.notifyChange
            is ToggleGroupNode -> node.notifyChange
            is TextareaNode -> node.notifyChange
            is SwitchNode -> node.notifyChange
            is RadioGroupNode -> node.notifyChange
            is SliderNode -> node.notifyChange
            is InputOtpNode -> node.notifyChange
            else -> false
        }
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
     * Marks every widget of a tree as touched, so that invalid inputs show as invalid.
     *
     * @param root the root of the tree
     */
    fun touchAll(root: Widget) = visit(root) { it.touched = true }

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
