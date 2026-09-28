package dev.slne.surf.roleplay.fabric.ui.widget

import dev.slne.surf.roleplay.fabric.ui.layout.Axis
import dev.slne.surf.roleplay.protocol.screen.AspectRatioNode
import dev.slne.surf.roleplay.protocol.screen.AlertNode
import dev.slne.surf.roleplay.protocol.screen.DropdownMenuNode
import dev.slne.surf.roleplay.protocol.screen.MenuContentNode
import dev.slne.surf.roleplay.protocol.screen.MenuItemNode
import dev.slne.surf.roleplay.protocol.screen.MenuCheckboxItemNode
import dev.slne.surf.roleplay.protocol.screen.MenuRadioGroupNode
import dev.slne.surf.roleplay.protocol.screen.MenuRadioItemNode
import dev.slne.surf.roleplay.protocol.screen.MenuLabelNode
import dev.slne.surf.roleplay.protocol.screen.MenuSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.MenuGroupNode
import dev.slne.surf.roleplay.protocol.screen.MenuSubNode
import dev.slne.surf.roleplay.protocol.screen.MenuSubTriggerNode
import dev.slne.surf.roleplay.protocol.screen.ContextMenuNode
import dev.slne.surf.roleplay.protocol.screen.MenubarNode
import dev.slne.surf.roleplay.protocol.screen.MenubarMenuNode
import dev.slne.surf.roleplay.protocol.screen.MenubarTriggerNode
import dev.slne.surf.roleplay.protocol.screen.PopoverNode
import dev.slne.surf.roleplay.protocol.screen.PopoverContentNode
import dev.slne.surf.roleplay.protocol.screen.PopoverHeaderNode
import dev.slne.surf.roleplay.protocol.screen.HoverCardNode
import dev.slne.surf.roleplay.protocol.screen.HoverCardContentNode
import dev.slne.surf.roleplay.protocol.screen.TooltipNode
import dev.slne.surf.roleplay.protocol.screen.EmptyNode
import dev.slne.surf.roleplay.protocol.screen.EmptyHeaderNode
import dev.slne.surf.roleplay.protocol.screen.EmptyMediaNode
import dev.slne.surf.roleplay.protocol.screen.EmptyContentNode
import dev.slne.surf.roleplay.protocol.screen.ItemNode
import dev.slne.surf.roleplay.protocol.screen.ItemMediaNode
import dev.slne.surf.roleplay.protocol.screen.ItemContentNode
import dev.slne.surf.roleplay.protocol.screen.ItemActionsNode
import dev.slne.surf.roleplay.protocol.screen.ItemHeaderNode
import dev.slne.surf.roleplay.protocol.screen.ItemFooterNode
import dev.slne.surf.roleplay.protocol.screen.ItemGroupNode
import dev.slne.surf.roleplay.protocol.screen.AvatarGroupCountNode
import dev.slne.surf.roleplay.protocol.screen.CardActionNode
import dev.slne.surf.roleplay.protocol.screen.CardContentNode
import dev.slne.surf.roleplay.protocol.screen.CardFooterNode
import dev.slne.surf.roleplay.protocol.screen.CardHeaderNode
import dev.slne.surf.roleplay.protocol.screen.CardNode
import dev.slne.surf.roleplay.protocol.screen.AvatarGroupNode
import dev.slne.surf.roleplay.protocol.screen.AvatarNode
import dev.slne.surf.roleplay.protocol.screen.BadgeNode
import dev.slne.surf.roleplay.protocol.screen.SkeletonNode
import dev.slne.surf.roleplay.protocol.screen.SpinnerNode
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupNode
import dev.slne.surf.roleplay.protocol.screen.KbdGroupNode
import dev.slne.surf.roleplay.protocol.screen.KbdNode
import dev.slne.surf.roleplay.protocol.screen.SeparatorNode
import dev.slne.surf.roleplay.protocol.screen.TextListNode
import dev.slne.surf.roleplay.protocol.screen.TextNode
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.ButtonGroupTextNode
import dev.slne.surf.roleplay.protocol.screen.ButtonNode
import dev.slne.surf.roleplay.protocol.screen.FieldContentNode
import dev.slne.surf.roleplay.protocol.screen.FieldGroupNode
import dev.slne.surf.roleplay.protocol.screen.FieldNode
import dev.slne.surf.roleplay.protocol.screen.FieldSeparatorNode
import dev.slne.surf.roleplay.protocol.screen.FieldSetNode
import dev.slne.surf.roleplay.protocol.screen.FieldTextNode
import dev.slne.surf.roleplay.protocol.screen.FormNode
import dev.slne.surf.roleplay.protocol.screen.CalendarNode
import dev.slne.surf.roleplay.protocol.screen.CalendarValues
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
                notifySearch = node.notifySearch,
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
            is FormNode -> container(FormWidget(node.id, node.submitId), node.children)
            is FieldSetNode -> container(FieldStackWidget(node.id, FieldStackWidget.SET_GAP), node.children)
            is FieldGroupNode -> container(FieldStackWidget(node.id, FieldStackWidget.GROUP_GAP), node.children)
            is FieldNode -> container(FieldWidget(node.id, node.orientation), node.children)
            is FieldContentNode -> container(FieldStackWidget(node.id, FieldStackWidget.CONTENT_GAP), node.children)
            is FieldTextNode -> FieldTextWidget(node.id, node.kind, node.text, node.forId)
            is FieldSeparatorNode -> FieldSeparatorWidget(node.id, node.text)
            is CalendarNode -> CalendarWidget(
                node.id, node.mode, CalendarValues.parse(node.mode, node.value).orEmpty(), node.month?.let(CalendarValues::month),
                node.min?.let(CalendarValues::date), node.max?.let(CalendarValues::date), node.disabled.mapNotNull(CalendarValues::date).toSet(),
                node.showOutsideDays, node.captionLayout, node.required,
            ).apply { enabled = node.enabled }
            is SwitchNode -> SwitchWidget(node.id, node.checked, node.size).apply { enabled = node.enabled }
            is RadioGroupNode -> RadioGroupWidget(node.id, node.options, node.selected, node.orientation, node.required).apply { enabled = node.enabled }
            is SliderNode -> SliderWidget(node.id, node.values, node.min, node.max, node.step, node.orientation).apply { enabled = node.enabled }
            is TextareaNode -> TextareaWidget(node.id, TextEditState(node.value, TextFilter.maxLength(node.maxLength)), node.placeholder, node.rows, node.required)
                .apply { enabled = node.enabled }
            is InputGroupNode -> InputGroupWidget(node.id, node.children.map { create(it) })
            is InputGroupAddonNode -> container(InputGroupAddonWidget(node.id, node.align), node.children)
            is InputGroupTextNode -> InputGroupTextWidget(node.id, node.text, node.icon)
            is TextNode -> TextWidget(node.id, node.kind, node.text, node.maxLines, node.align)
            is TextListNode -> TextListWidget(node.id, node.items, node.ordered)
            is SeparatorNode -> SeparatorWidget(node.id, node.orientation)
            is KbdNode -> KbdWidget(node.id, node.text, node.icon)
            is KbdGroupNode -> container(KbdGroupWidget(node.id), node.children)
            is BadgeNode -> BadgeWidget(node.id, node.text, node.icon, node.variant)
            is SkeletonNode -> SkeletonWidget(node.id, node.round)
            is SpinnerNode -> SpinnerWidget(node.id, node.size, node.color)
            is AspectRatioNode -> container(AspectRatioWidget(node.id, node.ratio), node.children)
            is AvatarNode -> AvatarWidget(node.id, AvatarWidget.playerId(node.playerId), node.texture, node.fallback, node.size, node.badge, node.badgeIcon)
            is AvatarGroupNode -> container(AvatarGroupWidget(node.id), node.children)
            is AvatarGroupCountNode -> AvatarGroupCountWidget(node.id, node.text, node.icon)
            is AlertNode -> container(AlertWidget(node.id, node.variant, node.icon), node.children)
            is CardNode -> container(CardWidget(node.id), node.children)
            is CardHeaderNode -> container(CardHeaderWidget(node.id), node.children)
            is CardActionNode -> container(CardActionWidget(node.id), node.children)
            is CardContentNode -> container(CardContentWidget(node.id), node.children)
            is CardFooterNode -> container(CardFooterWidget(node.id), node.children)
            is EmptyNode -> container(EmptyWidget(node.id, node.outline), node.children)
            is EmptyHeaderNode -> container(EmptyHeaderWidget(node.id), node.children)
            is EmptyMediaNode -> container(EmptyMediaWidget(node.id, node.variant, node.icon), node.children)
            is EmptyContentNode -> container(EmptyContentWidget(node.id), node.children)
            is ItemNode -> container(ItemWidget(node.id, node.variant, node.size, node.clickable), node.children)
            is ItemMediaNode -> container(ItemMediaWidget(node.id, node.variant, node.icon), node.children)
            is ItemContentNode -> container(ItemContentWidget(node.id), node.children)
            is ItemActionsNode -> container(ItemActionsWidget(node.id), node.children)
            is ItemHeaderNode -> container(ItemHeaderWidget(node.id), node.children)
            is ItemFooterNode -> container(ItemFooterWidget(node.id), node.children)
            is ItemGroupNode -> container(ItemGroupWidget(node.id), node.children)
            is PopoverNode -> container(PopoverWidget(node.id, node.side, node.align), node.children).apply { requestOpen(node.open) }
            is PopoverContentNode -> container(PopoverContentWidget(node.id), node.children)
            is PopoverHeaderNode -> container(PopoverHeaderWidget(node.id), node.children)
            is HoverCardNode -> container(HoverCardWidget(node.id, node.side, node.align, node.openDelay, node.closeDelay), node.children).apply { requestOpen(node.open) }
            is HoverCardContentNode -> container(PopoverContentWidget(node.id), node.children)
            is TooltipNode -> container(TooltipWidget(node.id, node.text, node.side), node.children)
            is DropdownMenuNode -> container(DropdownMenuWidget(node.id, node.side, node.align), node.children).apply { requestOpen(node.open) }
            is MenuContentNode -> container(MenuContentWidget(node.id), node.children)
            is MenuItemNode -> MenuItemWidget(node.id, node.text, node.icon, node.shortcut, node.destructive, node.inset).apply { enabled = node.enabled }
            is MenuCheckboxItemNode -> MenuCheckboxItemWidget(node.id, node.text, node.checked).apply { enabled = node.enabled }
            is MenuRadioGroupNode -> container(MenuRadioGroupWidget(node.id, node.value), node.children).apply { link() }
            is MenuRadioItemNode -> MenuRadioItemWidget(node.id, node.text, node.value).apply { enabled = node.enabled }
            is MenuLabelNode -> MenuLabelWidget(node.id, node.text, node.inset)
            is MenuSeparatorNode -> MenuSeparatorWidget(node.id)
            is MenuGroupNode -> container(MenuGroupWidget(node.id), node.children)
            is MenuSubNode -> container(MenuSubWidget(node.id), node.children).apply { requestOpen(node.open); link() }
            is MenuSubTriggerNode -> MenuSubTriggerWidget(node.id, node.text, node.icon, node.inset).apply { enabled = node.enabled }
            is ContextMenuNode -> container(ContextMenuWidget(node.id), node.children).apply { requestOpen(node.open) }
            is MenubarNode -> container(MenubarWidget(node.id), node.children)
            is MenubarMenuNode -> container(MenubarMenuWidget(node.id), node.children).apply { requestOpen(node.open) }
            is MenubarTriggerNode -> MenubarTriggerWidget(node.id, node.text)
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
            is CalendarNode -> node.notifyChange
            is RadioGroupNode -> node.notifyChange
            is SliderNode -> node.notifyChange
            is InputOtpNode -> node.notifyChange
            is PopoverNode -> node.notifyChange
            is HoverCardNode -> node.notifyChange
            is DropdownMenuNode -> node.notifyChange
            is MenuSubNode -> node.notifyChange
            is ContextMenuNode -> node.notifyChange
            is MenubarMenuNode -> node.notifyChange
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
