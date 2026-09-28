package dev.slne.surf.roleplay.protocol.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * The side of its trigger an overlay opens on.
 */
@Serializable
enum class OverlaySide {
    /**
     * Below the trigger.
     */
    @ProtoNumber(0)
    BOTTOM,

    /**
     * Above the trigger.
     */
    @ProtoNumber(1)
    TOP,

    /**
     * To the right of the trigger.
     */
    @ProtoNumber(2)
    RIGHT,

    /**
     * To the left of the trigger.
     */
    @ProtoNumber(3)
    LEFT,
}

/**
 * Opens or closes an overlay, such as a popover, menu or dialog.
 *
 * @property targetId the id of the overlay
 * @property open whether the overlay is open
 */
@Serializable
@SerialName("set_open")
data class SetOpen(
    @ProtoNumber(1) val targetId: String,
    @ProtoNumber(2) val open: Boolean,
) : PatchOperation

/**
 * A popover: its triggers, and a content that opens next to them when a trigger is clicked.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the triggers and one popover content
 * @property open whether the popover is open
 * @property side the side of the triggers the content opens on
 * @property align how the content is aligned along that side
 * @property notifyChange whether the mod reports every opening and closing at once
 */
@Serializable
@SerialName("popover")
data class PopoverNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val open: Boolean = false,
    @ProtoNumber(6) val side: OverlaySide = OverlaySide.BOTTOM,
    @ProtoNumber(7) val align: Align = Align.CENTER,
    @ProtoNumber(8) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): PopoverNode = copy(children = children)
}

/**
 * The content of a popover, on a bordered surface.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, in order
 */
@Serializable
@SerialName("popover_content")
data class PopoverContentNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): PopoverContentNode = copy(children = children)
}

/**
 * The header of a popover content: its title and description, stacked.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the title and description
 */
@Serializable
@SerialName("popover_header")
data class PopoverHeaderNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): PopoverHeaderNode = copy(children = children)
}

/**
 * A hover card: its triggers, and a content that opens next to them while the mouse rests on them.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the triggers and one hover card content
 * @property open whether the hover card is open
 * @property side the side of the triggers the content opens on
 * @property align how the content is aligned along that side
 * @property openDelay how long the mouse must rest on a trigger before the content opens, in
 *           milliseconds
 * @property closeDelay how long after the mouse left the triggers and the content it closes, in
 *           milliseconds
 * @property notifyChange whether the mod reports every opening and closing at once
 */
@Serializable
@SerialName("hover_card")
data class HoverCardNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val open: Boolean = false,
    @ProtoNumber(6) val side: OverlaySide = OverlaySide.BOTTOM,
    @ProtoNumber(7) val align: Align = Align.CENTER,
    @ProtoNumber(8) val openDelay: Int = 700,
    @ProtoNumber(9) val closeDelay: Int = 300,
    @ProtoNumber(10) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): HoverCardNode = copy(children = children)
}

/**
 * The content of a hover card, on a bordered surface.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the content, in order
 */
@Serializable
@SerialName("hover_card_content")
data class HoverCardContentNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): HoverCardContentNode = copy(children = children)
}

/**
 * A tooltip: its triggers, and a short text shown next to them while a trigger is hovered or
 * focused.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the triggers
 * @property text the text as component JSON
 * @property side the side of the triggers the text is shown on
 */
@Serializable
@SerialName("tooltip")
data class TooltipNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val text: String = "",
    @ProtoNumber(6) val side: OverlaySide = OverlaySide.TOP,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): TooltipNode = copy(children = children)
}

/**
 * A dropdown menu: its triggers, and a menu content that opens next to them when a trigger is
 * clicked.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the triggers and one menu content
 * @property open whether the menu is open
 * @property side the side of the triggers the menu opens on
 * @property align how the menu is aligned along that side
 * @property notifyChange whether the mod reports every opening and closing at once
 */
@Serializable
@SerialName("dropdown_menu")
data class DropdownMenuNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val open: Boolean = false,
    @ProtoNumber(6) val side: OverlaySide = OverlaySide.BOTTOM,
    @ProtoNumber(7) val align: Align = Align.CENTER,
    @ProtoNumber(8) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): DropdownMenuNode = copy(children = children)
}

/**
 * The content of a menu: a stack of items, labels, separators, groups and sub-menus on a bordered
 * surface.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the items, in order
 */
@Serializable
@SerialName("menu_content")
data class MenuContentNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): MenuContentNode = copy(children = children)
}

/**
 * An item of a menu that fires a widget action and closes the menu when chosen.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text as component JSON
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 * @property shortcut a keyboard shortcut shown at the end of the item as component JSON, or
 *           `null` for none
 * @property destructive whether the item is drawn in the destructive colour
 * @property inset whether the text is indented as if it had an icon
 * @property enabled whether the item can be used
 */
@Serializable
@SerialName("menu_item")
data class MenuItemNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val icon: String? = null,
    @ProtoNumber(6) val shortcut: String? = null,
    @ProtoNumber(7) val destructive: Boolean = false,
    @ProtoNumber(8) val inset: Boolean = false,
    @ProtoNumber(9) val enabled: Boolean = true,
) : ScreenNode

/**
 * An item of a menu with a check mark; choosing it flips the mark and fires a widget action
 * carrying the new state.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text as component JSON
 * @property checked whether the item is checked
 * @property enabled whether the item can be used
 */
@Serializable
@SerialName("menu_checkbox_item")
data class MenuCheckboxItemNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val checked: Boolean = false,
    @ProtoNumber(6) val enabled: Boolean = true,
) : ScreenNode

/**
 * A group of radio items of which one is chosen; choosing an item fires the group's widget action
 * carrying its value.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the radio items, in order
 * @property value the value of the chosen item, or empty for none
 */
@Serializable
@SerialName("menu_radio_group")
data class MenuRadioGroupNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val value: String = "",
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): MenuRadioGroupNode = copy(children = children)
}

/**
 * An item of a radio group, marked with a dot while it is chosen.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text as component JSON
 * @property value the value the item stands for
 * @property enabled whether the item can be used
 */
@Serializable
@SerialName("menu_radio_item")
data class MenuRadioItemNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val value: String = "",
    @ProtoNumber(6) val enabled: Boolean = true,
) : ScreenNode

/**
 * A heading inside a menu.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text as component JSON
 * @property inset whether the text is indented as if it had an icon
 */
@Serializable
@SerialName("menu_label")
data class MenuLabelNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val inset: Boolean = false,
) : ScreenNode

/**
 * A line between parts of a menu.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 */
@Serializable
@SerialName("menu_separator")
data class MenuSeparatorNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
) : ScreenNode

/**
 * A group of related menu items.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the items, in order
 */
@Serializable
@SerialName("menu_group")
data class MenuGroupNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): MenuGroupNode = copy(children = children)
}

/**
 * A sub-menu: its sub trigger, and a menu content that opens beside the trigger.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the sub trigger and one menu content
 * @property open whether the menu is open
 * @property notifyChange whether the mod reports every opening and closing at once
 */
@Serializable
@SerialName("menu_sub")
data class MenuSubNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val open: Boolean = false,
    @ProtoNumber(6) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): MenuSubNode = copy(children = children)
}

/**
 * The item of a menu that opens a sub-menu, marked with an arrow.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text as component JSON
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 * @property inset whether the text is indented as if it had an icon
 * @property enabled whether the item can be used
 */
@Serializable
@SerialName("menu_sub_trigger")
data class MenuSubTriggerNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val icon: String? = null,
    @ProtoNumber(6) val inset: Boolean = false,
    @ProtoNumber(7) val enabled: Boolean = true,
) : ScreenNode

/**
 * A context menu: an area, and a menu content that opens at the mouse on a right click in the area.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the area and one menu content
 * @property open whether the menu is open
 * @property notifyChange whether the mod reports every opening and closing at once
 */
@Serializable
@SerialName("context_menu")
data class ContextMenuNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val open: Boolean = false,
    @ProtoNumber(6) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): ContextMenuNode = copy(children = children)
}

/**
 * A horizontal bar of menus, as at the top of an application.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the menus, in order
 */
@Serializable
@SerialName("menubar")
data class MenubarNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): MenubarNode = copy(children = children)
}

/**
 * A menu of a menubar: its trigger, and a menu content that opens below it.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the menubar trigger and one menu content
 * @property open whether the menu is open
 * @property notifyChange whether the mod reports every opening and closing at once
 */
@Serializable
@SerialName("menubar_menu")
data class MenubarMenuNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val open: Boolean = false,
    @ProtoNumber(6) val notifyChange: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): MenubarMenuNode = copy(children = children)
}

/**
 * The trigger of a menubar menu, a text that is highlighted while its menu is open.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text as component JSON
 */
@Serializable
@SerialName("menubar_trigger")
data class MenubarTriggerNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
) : ScreenNode

/**
 * A command menu: a search input above a list of items that the input filters.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the input and the list
 * @property notifySearch whether the mod reports the typed query instead of filtering the items
 *           itself
 */
@Serializable
@SerialName("command")
data class CommandNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val notifySearch: Boolean = false,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): CommandNode = copy(children = children)
}

/**
 * The search input of a command menu, with a search icon.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property placeholder the hint shown while the input is empty, as component JSON
 */
@Serializable
@SerialName("command_input")
data class CommandInputNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val placeholder: String = "",
) : ScreenNode

/**
 * The scrolling list of a command menu.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the empty text, groups, items and separators
 */
@Serializable
@SerialName("command_list")
data class CommandListNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): CommandListNode = copy(children = children)
}

/**
 * The text of a command menu shown while no item matches the query.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text as component JSON
 */
@Serializable
@SerialName("command_empty")
data class CommandEmptyNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
) : ScreenNode

/**
 * A group of command items under a heading, hidden while none of its items match the query.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property children the items, in order
 * @property heading the heading as component JSON, or `null` for none
 */
@Serializable
@SerialName("command_group")
data class CommandGroupNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) override val children: List<ScreenNode> = emptyList(),
    @ProtoNumber(5) val heading: String? = null,
) : ContainerNode {
    /**
     * Returns a copy of this node with other children.
     *
     * @param children the new children
     * @return the copy
     */
    override fun withChildren(children: List<ScreenNode>): CommandGroupNode = copy(children = children)
}

/**
 * An item of a command menu that fires a widget action when chosen and is shown while it matches
 * the query.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 * @property text the text as component JSON
 * @property icon the Lucide name of an icon drawn before the text, or `null` for none
 * @property shortcut a keyboard shortcut shown at the end of the item as component JSON, or
 *           `null` for none
 * @property keywords further words the query matches the item by
 * @property enabled whether the item can be chosen
 */
@Serializable
@SerialName("command_item")
data class CommandItemNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
    @ProtoNumber(4) val text: String = "",
    @ProtoNumber(5) val icon: String? = null,
    @ProtoNumber(6) val shortcut: String? = null,
    @ProtoNumber(7) val keywords: List<String> = emptyList(),
    @ProtoNumber(8) val enabled: Boolean = true,
) : ScreenNode

/**
 * A line between parts of a command menu, hidden while a query is typed.
 *
 * @property id the id of this node
 * @property width how wide this node is laid out
 * @property height how tall this node is laid out
 */
@Serializable
@SerialName("command_separator")
data class CommandSeparatorNode(
    @ProtoNumber(1) override val id: String,
    @ProtoNumber(2) override val width: Sizing = Sizing.FIT,
    @ProtoNumber(3) override val height: Sizing = Sizing.FIT,
) : ScreenNode
